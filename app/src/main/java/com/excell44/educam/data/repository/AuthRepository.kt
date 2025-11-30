package com.excell44.educam.data.repository

import com.excell44.educam.core.network.NetworkObserver
import com.excell44.educam.data.dao.UserDao
import com.excell44.educam.data.local.SecurePrefs
import com.excell44.educam.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.excell44.educam.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.withTimeoutOrNull

import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import android.os.Build

@Singleton
class AuthRepository @Inject constructor(
    private val userDao: UserDao,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val securePrefs: SecurePrefs,
    private val networkObserver: NetworkObserver
) {
    // Room handles concurrency - no need for mutex

    // SupervisorScope pour isoler les erreurs de coroutines
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Retry policy removed as unused

    companion object {
        private const val TRIAL_DURATION_DAYS = 7L
        const val TRIAL_DURATION_MILLIS = TRIAL_DURATION_DAYS * 24 * 60 * 60 * 1000
    }

    /**
     * Generate a random salt for password hashing
     */
    private suspend fun generateSalt(): String = withContext(Dispatchers.IO) {
        withTimeoutOrNull(2000) {
            val random = java.security.SecureRandom()
            val bytes = ByteArray(16)
            random.nextBytes(bytes)
            bytes.joinToString("") { "%02x".format(it) }
        } ?: "default-salt-fallback".also {
            Logger.w("AuthRepository", "SecureRandom timeout, using fallback salt")
        }
    }

    /**
     * Get the appropriate PBKDF2 algorithm based on API level
     */
    private fun getPBKDF2Algorithm(): String {
        return if (Build.VERSION.SDK_INT >= 23) {
            "PBKDF2WithHmacSHA256"
        } else {
            "PBKDF2WithHmacSHA1" // Fallback for older APIs
        }
    }
    
    /**
     * Authentifie un utilisateur.
     * Thread-safe avec mutex sur la lecture DB.
     */
    suspend fun login(pseudo: String, password: String): Result<User> {
        Logger.d("AuthRepository", "Attempting login for: $pseudo")
        return try {
            // Room is thread-safe
            val user = userDao.getUserByPseudo(pseudo)

            if (user == null) {
                Logger.w("AuthRepository", "Login failed: User not found ($pseudo)")
                return Result.failure(Exception("Aucun compte trouvé avec ce pseudo"))
            }

            // Validate password
            android.util.Log.d("🔴 DEBUG_AUTH", "🔍 Checking password for user: ${user.pseudo}")
            android.util.Log.d("🔴 DEBUG_AUTH", "   Input password: '$password'")
            android.util.Log.d("🔴 DEBUG_AUTH", "   Stored hash: '${user.passwordHash}'")
            android.util.Log.d("🔴 DEBUG_AUTH", "   Stored salt: '${user.salt}'")

            // ✅ REAL PASSWORD VALIDATION
            val isPasswordValid = if (user.passwordHash.isEmpty()) {
                // Legacy accounts without password (if any) - should not happen with new registration
                android.util.Log.w("🔴 DEBUG_AUTH", "⚠️ Empty password hash - rejecting login")
                false
            } else {
                // Validate using PBKDF2
                try {
                    val spec = javax.crypto.spec.PBEKeySpec(
                        password.toCharArray(), 
                        user.salt.toByteArray(), 
                        10000, 
                        256
                    )
                    val factory = javax.crypto.SecretKeyFactory.getInstance(getPBKDF2Algorithm())
                    val computedHash = factory.generateSecret(spec).encoded.joinToString("") { "%02x".format(it) }
                    
                    val isValid = computedHash == user.passwordHash
                    android.util.Log.d("🔴 DEBUG_AUTH", "Password validation result: $isValid")
                    isValid
                } catch (e: Exception) {
                    android.util.Log.e("🔴 DEBUG_AUTH", "Password validation error: ${e.message}")
                    false
                }
            }

            if (isPasswordValid) {
                // ✅ CRITICAL: Save user session after successful login
                securePrefs.saveUserId(user.id)
                
                // ✅ NEW: Save credentials for offline re-login
                securePrefs.saveOfflineCredentials(user.pseudo, user.passwordHash)
                
                // ✅ NEW: Save auth mode (OFFLINE if offline account, ONLINE otherwise)
                val authMode = if (user.isOfflineAccount) {
                    SecurePrefs.AuthMode.OFFLINE
                } else {
                    SecurePrefs.AuthMode.ONLINE
                }
                securePrefs.saveAuthMode(authMode)
                
                android.util.Log.d("🔴 DEBUG_AUTH", "✅ Login SUCCESS - Session saved for ID: ${user.id} (Mode: $authMode)")
                Logger.i("AuthRepository", "Login successful: ${user.id} ($pseudo) [Mode: $authMode]")
                Result.success(user)
            } else {
                android.util.Log.e("🔴 DEBUG_AUTH", "❌ Login FAILED - Invalid password")
                Logger.w("AuthRepository", "Login failed: Invalid password ($pseudo)")
                Result.failure(Exception("Code incorrect"))
            }
        } catch (e: Exception) {
            Logger.e("AuthRepository", "Login error for $pseudo", e)
            Result.failure(Exception("Erreur lors de la connexion: ${e.message}"))
        }
    }

    /**
     * Enregistre un nouvel utilisateur (simple).
     * Thread-safe avec mutex sur les opérations DB.
     */
    suspend fun register(pseudo: String, password: String, name: String, gradeLevel: String): Result<User> {
        Logger.d("AuthRepository", "Attempting registration: $pseudo")
        return try {
            val existingUser = userDao.getUserByPseudo(pseudo)
            if (existingUser != null) {
                Logger.w("AuthRepository", "Registration failed: Pseudo already exists ($pseudo)")
                return Result.failure(Exception("Ce pseudo est déjà utilisé"))
            }

            // Generate salt and hash password
            val salt = generateSalt()
            val spec = javax.crypto.spec.PBEKeySpec(password.toCharArray(), salt.toByteArray(), 10000, 256)
            val factory = javax.crypto.SecretKeyFactory.getInstance(getPBKDF2Algorithm())
            val passwordHash = factory.generateSecret(spec).encoded.joinToString("") { "%02x".format(it) }
            val user = User(
                id = UUID.randomUUID().toString(),
                pseudo = pseudo,
                passwordHash = passwordHash,
                salt = salt,
                name = name,
                gradeLevel = gradeLevel,
                role = "ACTIVE", // Online paid registration
                syncStatus = "SYNCED", // Already online
                isOfflineAccount = false
            )
            userDao.insertUser(user)
            // ✅ CRITICAL: Save user session after successful registration
            securePrefs.saveUserId(user.id)
            securePrefs.saveOfflineCredentials(user.pseudo, user.passwordHash)
            securePrefs.saveAuthMode(SecurePrefs.AuthMode.ONLINE)
            Logger.i("AuthRepository", "Registration successful: ${user.id} ($pseudo) - ACTIVE [ONLINE]")
            Result.success(user)
        } catch (e: Exception) {
            Logger.e("AuthRepository", "Registration error for $pseudo", e)
            Result.failure(Exception("Erreur lors de l'inscription: ${e.message}"))
        }
    }

    /**
     * Enregistrement complet avec profil étendu.
     * Thread-safe avec mutex.
     */
    suspend fun registerFull(
        pseudo: String,
        password: String,
        fullName: String,
        gradeLevel: String,
        school: String,
        city: String,
        neighborhood: String,
        parentName: String?,
        parentPhone: String?,
        relation: String?,
        promoCode: String?
    ): Result<User> {
        return try {
            val existingUser = userDao.getUserByPseudo(pseudo)
            if (existingUser != null) {
                return Result.failure(Exception("Ce pseudo est déjà utilisé"))
            }

            val salt = generateSalt()
            val spec = javax.crypto.spec.PBEKeySpec(password.toCharArray(), salt.toByteArray(), 10000, 256)
            val factory = javax.crypto.SecretKeyFactory.getInstance(getPBKDF2Algorithm())
            val passwordHash = factory.generateSecret(spec).encoded.joinToString("") { "%02x".format(it) }
            val user = User(
                id = UUID.randomUUID().toString(),
                pseudo = pseudo,
                passwordHash = passwordHash,
                salt = salt,
                name = fullName,
                gradeLevel = gradeLevel
            )
            userDao.insertUser(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception("Erreur lors de l'inscription: ${e.message}"))
        }
    }

    /**
     * Enregistrement hors ligne avec limite de 3 comptes.
     * Thread-safe avec mutex.
     */
    suspend fun registerOffline(
        pseudo: String,
        password: String,
        fullName: String,
        gradeLevel: String
    ): Result<User> {
        Logger.d("AuthRepository", "Attempting offline registration: $pseudo")
        return try {
            // Check limit
            val offlineCount = userDao.countOfflineUsers()
            if (offlineCount >= 3) {
                Logger.w("AuthRepository", "Offline registration failed: Limit reached ($offlineCount/3)")
                return Result.failure(Exception("Limite de 3 comptes hors ligne atteinte sur cet appareil."))
            }

            // Check if pseudo exists
            val existingUser = userDao.getUserByPseudo(pseudo)
            if (existingUser != null) {
                Logger.w("AuthRepository", "Offline registration failed: Pseudo taken ($pseudo)")
                return Result.failure(Exception("Ce pseudo est déjà utilisé"))
            }

            // Generate salt and hash password
            val salt = generateSalt()
            val spec = javax.crypto.spec.PBEKeySpec(password.toCharArray(), salt.toByteArray(), 10000, 256)
            val factory = javax.crypto.SecretKeyFactory.getInstance(getPBKDF2Algorithm())
            val passwordHash = factory.generateSecret(spec).encoded.joinToString("") { "%02x".format(it) }

            // Create offline user with 7-day trial (PASSIVE role)
            val trialDuration = TRIAL_DURATION_MILLIS
            val user = User(
                id = UUID.randomUUID().toString(),
                pseudo = pseudo,
                passwordHash = passwordHash,
                salt = salt,
                name = fullName,
                gradeLevel = gradeLevel,
                isOfflineAccount = true,
                trialExpiresAt = System.currentTimeMillis() + trialDuration,
                syncStatus = "PENDING_CREATE",
                role = "PASSIVE" // Trial account, needs sync to become ACTIVE
            )
            userDao.insertUser(user)
            // ✅ CRITICAL: Save user session after successful offline registration
            securePrefs.saveUserId(user.id)
            securePrefs.saveOfflineCredentials(user.pseudo, user.passwordHash)
            securePrefs.saveAuthMode(SecurePrefs.AuthMode.OFFLINE)
            Logger.i("AuthRepository", "Offline registration successful: ${user.id} ($pseudo) - 7d trial [OFFLINE]")
            Result.success(user)
        } catch (e: Exception) {
            Logger.e("AuthRepository", "Offline registration error", e)
            Result.failure(Exception("Erreur lors de la création du compte hors ligne: ${e.message}"))
        }
    }

    /**
     * Get user by pseudo for admin operations.
     */
    suspend fun getUserByPseudo(pseudo: String): User? {
        return try {
            userDao.getUserByPseudo(pseudo)
        } catch (e: Exception) {
            Logger.e("AuthRepository", "Error getting user by pseudo: $pseudo", e)
            null
        }
    }

    /**
     * Récupère l'utilisateur actuel via Flow (réactif).
     */
    fun getCurrentUser(userId: String): Flow<User?> = userDao.getUserById(userId)

    /**
     * Met à jour le niveau scolaire.
     * Thread-safe avec mutex.
     */
    suspend fun updateGradeLevel(userId: String, gradeLevel: String) {
        try {
            userDao.updateGradeLevel(userId, gradeLevel)
        } catch (e: Exception) {
            // Log error but don't throw (graceful degradation)
            println("Error updating grade level: ${e.message}")
        }
    }

    /**
     * Récupère l'utilisateur actuel avec fallback offline.
     * Source of Truth: Local DB (primary) -> Firebase (secondary for online accounts)
     */
    suspend fun getUser(): Result<User> = withContext(Dispatchers.IO) {
        try {
            // 1. FIRST: Try to get from local cache (SecurePrefs + Room)
            val cachedId = securePrefs.getUserId()
            Logger.d("AuthRepository", "Checking local cache (Cached ID: $cachedId)")

            if (cachedId != null) {
                // Fetch from local DB with timeout to avoid infinite blocking
                val user = withTimeoutOrNull(5000) {
                    userDao.getUserById(cachedId).first()
                }
                if (user != null) {
                    Logger.i("AuthRepository", "Local user found: ${user.id} (${user.pseudo})")

                    // 2. SECONDARY: If online and user is online account, sync with Firebase
                    if (networkObserver.isOnline() && !user.isOfflineAccount) {
                        try {
                            val firebaseUser = firebaseAuth.currentUser
                            if (firebaseUser != null && firebaseUser.uid == user.id) {
                                Logger.d("AuthRepository", "Firebase sync OK for user: ${user.id}")
                            } else {
                                Logger.w("AuthRepository", "Firebase sync mismatch for user: ${user.id}")
                                // Could logout here, but for now just log
                            }
                        } catch (e: Exception) {
                            Logger.w("AuthRepository", "Firebase sync failed, continuing with local user", e)
                        }
                    }

                    return@withContext Result.success(user)
                } else {
                    Logger.w("AuthRepository", "Local user not found for cached ID: $cachedId")
                    // Clear invalid cached ID
                    securePrefs.clearUserId()
                }
            }

            // 3. FALLBACK: If no local user, check Firebase (for online accounts only)
            if (networkObserver.isOnline()) {
                val firebaseUser = firebaseAuth.currentUser
                if (firebaseUser != null) {
                    Logger.d("AuthRepository", "Creating user from Firebase: ${firebaseUser.uid}")

                    // Create basic user from Firebase data
                    val user = User(
                        id = firebaseUser.uid,
                        pseudo = firebaseUser.email ?: firebaseUser.uid,
                        passwordHash = "", // Not needed for Firebase user
                        salt = "", // No salt for Firebase users
                        name = firebaseUser.displayName ?: "Utilisateur",
                        gradeLevel = "TBD",
                        isOfflineAccount = false
                    )

                    // Save to local DB for future offline access
                    userDao.insertUser(user)
                    securePrefs.saveUserId(user.id)

                    Logger.i("AuthRepository", "Firebase user cached locally: ${user.id}")
                    return@withContext Result.success(user)
                } else {
                    Logger.d("AuthRepository", "No Firebase user found")
                }
            } else {
                Logger.d("AuthRepository", "Offline - no cached user available")
            }

            // 4. No user found anywhere
            Logger.d("AuthRepository", "No user found (local or Firebase)")
            Result.failure(Exception("Utilisateur non connecté"))

        } catch (e: Exception) {
            Logger.e("AuthRepository", "Error fetching user", e)
            Result.failure(e)
        }
    }

    /**
     * Flux temps réel de l'utilisateur (Firestore + Offline Persistence).
     * C'est la méthode recommandée pour l'UI réactive.
     */
    fun getUserFlow(userId: String): Flow<User?> = kotlinx.coroutines.flow.callbackFlow {
        val docRef = firestore.collection("users").document(userId)
        
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // Log error but don't close stream immediately if possible, or close with error
                close(error)
                return@addSnapshotListener
            }
            
            if (snapshot != null && snapshot.exists()) {
                // Map Firestore document to User object
                // Note: This assumes Firestore structure matches User object or we map manually
                val user = snapshot.toObject(User::class.java)
                trySend(user)
            } else {
                trySend(null)
            }
        }
        
        awaitClose { listener.remove() }
    }

    /**
     * Clean up expired offline accounts (>24h old, not synced).
     * Called on app startup to enforce 24h offline trial limit.
     */
    suspend fun cleanExpiredOfflineAccounts(): Int {
        return try {
            val now = System.currentTimeMillis()
            // ✅ FIX: Use 7 days instead of 24h to match trial duration
            val expiryThreshold = now - TRIAL_DURATION_MILLIS

            // Get list of expired accounts for logging
            val expiredUsers = userDao.getExpiredUnsyncedUsers(expiryThreshold)

            if (expiredUsers.isNotEmpty()) {
                Logger.w("AuthRepository", "Cleaning ${expiredUsers.size} expired offline account(s)")
                expiredUsers.forEach { user ->
                    Logger.d("AuthRepository", "Deleting expired account: ${user.pseudo} (created: ${user.createdAt})")
                }
            }

            // Delete expired unsynced accounts
            val deletedCount = userDao.deleteExpiredUnsyncedUsers(expiryThreshold)

            if (deletedCount > 0) {
                Logger.i("AuthRepository", "Cleaned up $deletedCount expired offline account(s)")
            }

            deletedCount
        } catch (e: Exception) {
            Logger.e("AuthRepository", "Error cleaning expired accounts", e)
            0
        }
    }
    
    /**
     * Check if user has valid access (OFFLINE or ONLINE).
     * This is the unified access check that should be used instead of just checking token.
     */
    suspend fun isUserAllowedAccess(): Boolean {
        return try {
            // Check if we have a saved user session
            val userId = securePrefs.getUserId()
            if (userId == null) {
                Logger.d("AuthRepository", "No access: No saved user ID")
                return false
            }
            
            // Check auth mode
            val authMode = securePrefs.getAuthMode()
            Logger.d("AuthRepository", "Checking access for user: $userId (Mode: $authMode)")
            
            when (authMode) {
                SecurePrefs.AuthMode.OFFLINE -> {
                    // For offline mode, check if user still exists in DB
                    val user = userDao.getUserById(userId).first()
                    if (user == null) {
                        Logger.w("AuthRepository", "Offline user not found in DB")
                        securePrefs.clearAllAuthData()
                        return false
                    }
                    
                    // Check if trial expired (for offline accounts)
                    if (user.isOfflineAccount && user.trialExpiresAt != null && user.trialExpiresAt < System.currentTimeMillis()) {
                        Logger.w("AuthRepository", "Offline trial expired for user: ${user.pseudo}")
                        return false
                    }
                    
                    Logger.i("AuthRepository", "Access granted (OFFLINE): ${user.pseudo}")
                    true
                }
                SecurePrefs.AuthMode.ONLINE -> {
                    // For online mode, user should have synced account
                    val user = userDao.getUserById(userId).first()
                    if (user == null) {
                        Logger.w("AuthRepository", "Online user not found in DB")
                        securePrefs.clearAllAuthData()
                        return false
                    }
                    
                    Logger.i("AuthRepository", "Access granted (ONLINE): ${user.pseudo}")
                    true
                }
                null -> {
                    // No auth mode saved, clear session
                    Logger.w("AuthRepository", "No auth mode found, clearing session")
                    securePrefs.clearAllAuthData()
                    false
                }
            }
        } catch (e: Exception) {
            Logger.e("AuthRepository", "Error checking user access", e)
            false
        }
    }
}
