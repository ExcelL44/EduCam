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
            val isPasswordValid = if (user.passwordHash.isEmpty()) {
                // Offline account without password - should not be allowed to login this way
                Logger.w("AuthRepository", "Login blocked: Offline account without password ($pseudo)")
                false
            } else if (user.salt.isEmpty()) {
                // Legacy user without salt (pre-PBKDF2) - use fallback validation
                Logger.w("AuthRepository", "Legacy user detected without salt ($pseudo) - using SHA-256 fallback")
                // For backward compatibility, try SHA-256 (old method)
                try {
                    val md = java.security.MessageDigest.getInstance("SHA-256")
                    val hash = md.digest(password.toByteArray()).joinToString("") { "%02x".format(it) }
                    val isValid = user.passwordHash == hash
                    if (isValid) {
                        // Migrate to PBKDF2: generate salt and rehash password
                        Logger.i("AuthRepository", "Migrating legacy user to PBKDF2 ($pseudo)")
                        val newSalt = generateSalt()
                        val spec = javax.crypto.spec.PBEKeySpec(password.toCharArray(), newSalt.toByteArray(), 10000, 256)
                        val factory = javax.crypto.SecretKeyFactory.getInstance(getPBKDF2Algorithm())
                        val newHash = factory.generateSecret(spec).encoded.joinToString("") { "%02x".format(it) }

                        // Update user with new salt and hash
                        val updatedUser = user.copy(salt = newSalt, passwordHash = newHash)
                        userDao.insertUser(updatedUser)
                        Logger.i("AuthRepository", "Legacy user migrated to PBKDF2 ($pseudo)")
                    }
                    isValid
                } catch (e: Exception) {
                    Logger.e("AuthRepository", "Legacy password validation failed ($pseudo)", e)
                    false
                }
            } else {
                // Modern PBKDF2 validation with salt
                val spec = javax.crypto.spec.PBEKeySpec(password.toCharArray(), user.salt.toByteArray(), 10000, 256)
                val factory = javax.crypto.SecretKeyFactory.getInstance(getPBKDF2Algorithm())
                val hash = factory.generateSecret(spec).encoded.joinToString("") { "%02x".format(it) }
                user.passwordHash == hash
            }

            if (isPasswordValid) {
                // ✅ CRITICAL: Save user session after successful login
                securePrefs.saveUserId(user.id)
                Logger.i("AuthRepository", "Login successful: ${user.id} ($pseudo)")
                Result.success(user)
            } else {
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
            Logger.i("AuthRepository", "Registration successful: ${user.id} ($pseudo) - ACTIVE")
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

            // Create offline user with 24-hour trial (PASSIVE role)
            val trialDuration = 24L * 60 * 60 * 1000 // 24 hours in millis
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
            Logger.i("AuthRepository", "Offline registration successful: ${user.id} ($pseudo) - 24h trial")
            Result.success(user)
        } catch (e: Exception) {
            Logger.e("AuthRepository", "Offline registration error", e)
            Result.failure(Exception("Erreur lors de la création du compte hors ligne: ${e.message}"))
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
     * Source of Truth: Firebase (Online) -> SecurePrefs + Room (Offline)
     */
    suspend fun getUser(): Result<User> = withContext(Dispatchers.IO) {
        try {
            if (networkObserver.isOnline()) {
                // ONLINE: Vérifie Firebase
                val firebaseUser = firebaseAuth.currentUser
                if (firebaseUser != null) {
                    Logger.d("AuthRepository", "Fetching online user: ${firebaseUser.uid}")
                    val user = User(
                        id = firebaseUser.uid,
                        pseudo = firebaseUser.email ?: firebaseUser.uid, // Use email or UID as pseudo
                        passwordHash = "", // Not needed for Firebase user
                        salt = "", // No salt for Firebase users
                        name = firebaseUser.displayName ?: "Utilisateur",
                        gradeLevel = "TBD", // Should fetch from Firestore
                        isOfflineAccount = false
                    )
                    // ✅ CRITICAL: Cache user to local DB for offline fallback
                    userDao.insertUser(user)
                    securePrefs.saveUserId(user.id) // Cache ID
                    Result.success(user)
                } else {
                    Logger.d("AuthRepository", "No online user found (Firebase)")
                    Result.failure(Exception("Not logged in"))
                }
            } else {
                // OFFLINE: Récupère du cache
                val cachedId = securePrefs.getUserId()
                Logger.d("AuthRepository", "Fetching offline user (Cached ID: $cachedId)")
                if (cachedId != null) {
                    // Fetch from local DB with timeout to avoid infinite blocking
                    val user = withTimeoutOrNull(5000) {
                        userDao.getUserById(cachedId).first()
                    }
                    if (user != null) {
                        Logger.i("AuthRepository", "Offline user restored: ${user.id}")
                        Result.success(user)
                    } else {
                        Logger.w("AuthRepository", "Offline user not found or timeout for ID: $cachedId")
                        Result.failure(Exception("User not found in local DB or timeout"))
                    }
                } else {
                    Result.failure(Exception("No offline user"))
                }
            }
        } catch (e: Exception) {
            Logger.e("AuthRepository", "Error fetching user", e)
            Result.failure(e)
        }
    }

    /**
     * Connexion anonyme via Firebase.
     */
    suspend fun loginAnonymous(): Result<User> = withContext(Dispatchers.IO) {
        try {
            val authResult = firebaseAuth.signInAnonymously().await()
            val firebaseUser = authResult.user!!
            
            val user = User(
                id = firebaseUser.uid,
                pseudo = firebaseUser.uid, // Use UID as pseudo for anonymous users
                passwordHash = "",
                salt = "",
                name = "Invité",
                gradeLevel = "TBD",
                isOfflineAccount = false
            )
            
            securePrefs.saveUserId(user.id)
            Result.success(user)
        } catch (e: Exception) {
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
            val twentyFourHoursAgo = now - (24L * 60 * 60 * 1000)
            
            // Get list of expired accounts for logging
            val expiredUsers = userDao.getExpiredUnsyncedUsers(twentyFourHoursAgo)
            
            if (expiredUsers.isNotEmpty()) {
                Logger.w("AuthRepository", "Cleaning ${expiredUsers.size} expired offline account(s)")
                expiredUsers.forEach { user ->
                    Logger.d("AuthRepository", "Deleting expired account: ${user.pseudo} (created: ${user.createdAt})")
                }
            }
            
            // Delete expired unsynced accounts
            val deletedCount = userDao.deleteExpiredUnsyncedUsers(twentyFourHoursAgo)
            
            if (deletedCount > 0) {
                Logger.i("AuthRepository", "Cleaned up $deletedCount expired offline account(s)")
            }
            
            deletedCount
        } catch (e: Exception) {
            Logger.e("AuthRepository", "Error cleaning expired accounts", e)
            0
        }
    }
}
