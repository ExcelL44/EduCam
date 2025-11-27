package com.excell44.educam.data.repository

import com.excell44.educam.data.dao.UserDao
import com.excell44.educam.data.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val userDao: UserDao
) {
    // Mutex pour protéger les écritures critiques en base de données
    private val dbMutex = Mutex()
    
    // SupervisorScope pour isoler les erreurs de coroutines
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // Retry policy pour les opérations réseau (si on ajoute un backend)
    private val retryPolicy = RetryPolicy(
        maxRetries = 3,
        initialDelayMs = 1000L,
        maxDelayMs = 5000L
    )

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
                user.passwordHash == password.hashCode().toString()
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
                    passwordHash = password.hashCode().toString(),
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
                    passwordHash = password.hashCode().toString(),
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
                    passwordHash = password.hashCode().toString(),
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
}
