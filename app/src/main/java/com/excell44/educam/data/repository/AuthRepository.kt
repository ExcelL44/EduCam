package com.excell44.educam.data.repository

import com.excell44.educam.core.network.NetworkObserver
import com.excell44.educam.data.dao.UserDao
import com.excell44.educam.data.local.SecurePrefs
import com.excell44.educam.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val userDao: UserDao,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val securePrefs: SecurePrefs,
    private val networkObserver: NetworkObserver
) {
    // Mutex pour protéger les écritures critiques en base de données
    private val dbMutex = Mutex()
    
    // SupervisorScope pour isoler les erreurs de coroutines
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // Retry policy removed as unused
    
    /**
     * Authentifie un utilisateur.
     * Thread-safe avec mutex sur la lecture DB.
     */
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            // Lecture DB avec mutex pour éviter les conflits
            val user = dbMutex.withLock {
                userDao.getUserByEmail(email)
            }
            
            if (user == null) {
                return Result.failure(Exception("Aucun compte trouvé avec ce pseudo"))
            }
            
            // Validate password
            val isPasswordValid = if (user.passwordHash.isEmpty()) {
                // Offline account without password - should not be allowed to login this way
                false
            } else {
                // Use SHA-256 for better security than hashCode
                val inputHash = java.security.MessageDigest.getInstance("SHA-256")
                    .digest(password.toByteArray())
                    .joinToString("") { "%02x".format(it) }
                user.passwordHash == inputHash
            }
            
            if (isPasswordValid) {
                Result.success(user)
            } else {
                Result.failure(Exception("Code incorrect"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Erreur lors de la connexion: ${e.message}"))
        }
    }

    /**
     * Enregistre un nouvel utilisateur (simple).
     * Thread-safe avec mutex sur les opérations DB.
     */
    suspend fun register(email: String, password: String, name: String, gradeLevel: String): Result<User> {
        return try {
            dbMutex.withLock {
                val existingUser = userDao.getUserByEmail(email)
                if (existingUser != null) {
                    return Result.failure(Exception("Cet email est déjà utilisé"))
                }
                
                val user = User(
                    id = UUID.randomUUID().toString(),
                    email = email,
                    passwordHash = java.security.MessageDigest.getInstance("SHA-256").digest(password.toByteArray()).joinToString("") { "%02x".format(it) },
                    name = name,
                    gradeLevel = gradeLevel
                )
                userDao.insertUser(user)
                Result.success(user)
            }
        } catch (e: Exception) {
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
            val email = "${pseudo.lowercase()}@local.excell"
            
            dbMutex.withLock {
                val existingUser = userDao.getUserByEmail(email)
                if (existingUser != null) {
                    return Result.failure(Exception("Ce pseudo est déjà utilisé"))
                }
                
                val user = User(
                    id = UUID.randomUUID().toString(),
                    email = email,
                    passwordHash = java.security.MessageDigest.getInstance("SHA-256").digest(password.toByteArray()).joinToString("") { "%02x".format(it) },
                    name = fullName,
                    gradeLevel = gradeLevel
                )
                userDao.insertUser(user)
                Result.success(user)
            }
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
        return try {
            dbMutex.withLock {
                // Check limit
                val offlineCount = userDao.countOfflineUsers()
                if (offlineCount >= 3) {
                    return Result.failure(Exception("Limite de 3 comptes hors ligne atteinte sur cet appareil."))
                }

                // Check if pseudo exists
                val email = "${pseudo.lowercase()}@local.excell"
                val existingUser = userDao.getUserByEmail(email)
                if (existingUser != null) {
                    return Result.failure(Exception("Ce pseudo est déjà utilisé"))
                }

                // Create offline user with 7-day trial and password
                val trialDuration = 7L * 24 * 60 * 60 * 1000 // 7 days in millis
                val user = User(
                    id = UUID.randomUUID().toString(),
                    email = email,
                    passwordHash = java.security.MessageDigest.getInstance("SHA-256").digest(password.toByteArray()).joinToString("") { "%02x".format(it) },
                    name = fullName,
                    gradeLevel = gradeLevel,
                    isOfflineAccount = true,
                    trialExpiresAt = System.currentTimeMillis() + trialDuration,
                    syncStatus = "PENDING_CREATE"
                )
                userDao.insertUser(user)
                Result.success(user)
            }
        } catch (e: Exception) {
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
            dbMutex.withLock {
                userDao.updateGradeLevel(userId, gradeLevel)
            }
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
                    val user = User(
                        id = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        passwordHash = "", // Not needed for Firebase user
                        name = firebaseUser.displayName ?: "Utilisateur",
                        gradeLevel = "TBD", // Should fetch from Firestore
                        isOfflineAccount = false
                    )
                    // ✅ CRITICAL: Cache user to local DB for offline fallback
                    dbMutex.withLock {
                        userDao.insertUser(user)
                    }
                    securePrefs.saveUserId(user.id) // Cache ID
                    Result.success(user)
                } else {
                    Result.failure(Exception("Not logged in"))
                }
            } else {
                // OFFLINE: Récupère du cache
                val cachedId = securePrefs.getUserId()
                if (cachedId != null) {
                    // Fetch from local DB
                    val user = userDao.getUserById(cachedId).first()
                    if (user != null) {
                        Result.success(user)
                    } else {
                        Result.failure(Exception("User not found in local DB"))
                    }
                } else {
                    Result.failure(Exception("No offline user"))
                }
            }
        } catch (e: Exception) {
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
                email = "",
                passwordHash = "",
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
}
