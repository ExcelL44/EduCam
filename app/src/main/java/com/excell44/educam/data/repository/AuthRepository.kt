package com.excell44.educam.data.repository

import com.excell44.educam.data.dao.UserDao
import com.excell44.educam.data.model.User
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val userDao: UserDao
) {
    suspend fun login(email: String, password: String): Result<User> {
        val user = userDao.getUserByEmail(email)
        return if (user != null && user.passwordHash == password.hashCode().toString()) {
            Result.success(user)
        } else {
            Result.failure(Exception("Email ou mot de passe incorrect"))
        }
    }

    suspend fun register(email: String, password: String, name: String, gradeLevel: String): Result<User> {
        val existingUser = userDao.getUserByEmail(email)
        return if (existingUser != null) {
            Result.failure(Exception("Cet email est déjà utilisé"))
        } else {
            val user = User(
                id = UUID.randomUUID().toString(),
                email = email,
                passwordHash = password.hashCode().toString(), // En production, utiliser bcrypt ou similar
                name = name,
                gradeLevel = gradeLevel
            )
            userDao.insertUser(user)
            Result.success(user)
        }
    }

    // Register with extended profile data (used by app registration flow)
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
        // For local/offline storage, we construct a synthetic email based on pseudo
        val email = "${pseudo.lowercase()}@local.excell"
        val existingUser = userDao.getUserByEmail(email)
        return if (existingUser != null) {
            Result.failure(Exception("Ce pseudo est déjà utilisé"))
        } else {
            val user = User(
                id = UUID.randomUUID().toString(),
                email = email,
                passwordHash = password.hashCode().toString(),
                name = fullName,
                gradeLevel = gradeLevel
            )
            userDao.insertUser(user)
            // Additional profile details are persisted via preferences by AuthStateManager
            Result.success(user)
        }
    }

    suspend fun registerOffline(
        pseudo: String,
        fullName: String,
        gradeLevel: String
    ): Result<User> {
        // 1. Check limit (max 3 offline users)
        val offlineCount = userDao.countOfflineUsers()
        if (offlineCount >= 3) {
            return Result.failure(Exception("Limite de 3 comptes hors ligne atteinte sur cet appareil."))
        }

        // 2. Check if pseudo exists
        val email = "${pseudo.lowercase()}@local.excell"
        val existingUser = userDao.getUserByEmail(email)
        if (existingUser != null) {
            return Result.failure(Exception("Ce pseudo est déjà utilisé"))
        }

        // 3. Create offline user with 7-day trial
        val trialDuration = 7L * 24 * 60 * 60 * 1000 // 7 days in millis
        val user = User(
            id = UUID.randomUUID().toString(),
            email = email,
            passwordHash = "", // No password for offline/guest initially? Or simple one.
            name = fullName,
            gradeLevel = gradeLevel,
            isOfflineAccount = true,
            trialExpiresAt = System.currentTimeMillis() + trialDuration,
            syncStatus = "PENDING_CREATE"
        )
        userDao.insertUser(user)
        return Result.success(user)
    }

    fun getCurrentUser(userId: String): Flow<User?> = userDao.getUserById(userId)

    suspend fun updateGradeLevel(userId: String, gradeLevel: String) {
        userDao.updateGradeLevel(userId, gradeLevel)
    }
}

