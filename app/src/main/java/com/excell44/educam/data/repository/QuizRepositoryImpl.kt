package com.excell44.educam.data.repository

import com.excell44.educam.data.cache.MemoryCache
import com.excell44.educam.data.local.dao.QuestionDao
import com.excell44.educam.data.local.dao.QuizDao
import com.excell44.educam.data.local.dao.QuizResultDao
import com.excell44.educam.data.local.entity.QuizEntity
import com.excell44.educam.data.local.entity.QuizResultEntity
import com.excell44.educam.data.local.entity.QuizWithQuestions
import com.excell44.educam.domain.repository.QuizRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizRepositoryImpl @Inject constructor(
    private val quizDao: QuizDao,
    private val questionDao: QuestionDao,
    private val resultDao: QuizResultDao,
    private val aiRepository: AIRepository
) : QuizRepository {
    
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    
    // Mutex pour les écritures critiques
    private val writeMutex = Mutex()
    
    // Memory Cache pour les quiz avec TTL de 5 minutes
    private val quizCache = MemoryCache<Long, QuizWithQuestions>(
        defaultTtlMs = 5 * 60 * 1000L
    )
    
    // Memory Cache pour la liste des quiz avec TTL de 2 minutes
    private val quizListCache = MemoryCache<String, List<QuizEntity>>(
        defaultTtlMs = 2 * 60 * 1000L
    )

    /**
     * Récupère tous les quiz avec stratégie offline-first.
     * Flow: Memory Cache -> DB -> (API optionnel future)
     */
    override fun getQuizzes(): Flow<List<QuizEntity>> = flow {
        try {
            // 1. Essayer le cache mémoire d'abord
            val cached = quizListCache.get("all_quizzes")
            if (cached != null) {
                emit(cached)
                return@flow
            }
            
            // 2. Charger depuis DB avec timeout de 10s
            val fromDb = withTimeout(10_000L) {
                quizDao.getAllQuizzesSync()
            }
            
            // 3. Mettre en cache
            quizListCache.put("all_quizzes", fromDb)
            emit(fromDb)
            
            // TODO: 4. Sync avec API en arrière-plan si connecté
            
        } catch (e: Exception) {
            // Graceful degradation: retourner une liste vide au lieu de crasher
            emit(emptyList())
        }
    }.flowOn(ioDispatcher)
    
    /**
     * Récupère les détails d'un quiz avec ses questions.
     * Flow: Memory Cache -> DB
     */
    override fun getQuizDetails(id: Long): Flow<QuizWithQuestions> = flow {
        try {
            // 1. Vérifier le cache
            val cached = quizCache.get(id)
            if (cached != null) {
                emit(cached)
                return@flow
            }
            
            // 2. Charger depuis DB avec timeout
            val fromDb = withTimeout(10_000L) {
                quizDao.getQuizWithQuestionsSync(id)
            }
            
            // 3. Mettre en cache
            quizCache.put(id, fromDb)
            emit(fromDb)
            
        } catch (e: Exception) {
            // En cas d'erreur, renvoyer une exception pour que l'UI puisse gérer
            throw Exception("Impossible de charger le quiz: ${e.message}")
        }
    }.flowOn(ioDispatcher)
        .catch { e ->
            // Log l'erreur mais permettre à l'UI de gérer
            println("Error loading quiz details: ${e.message}")
            throw e
        }

    /**
     * Version synchrone pour les détails du quiz.
     * Utilisé pour les opérations où on a besoin du résultat immédiatement.
     */
    override suspend fun getQuizDetailsSync(id: Long): QuizWithQuestions = withContext(ioDispatcher) {
        try {
            // 1. Vérifier le cache
            val cached = quizCache.get(id)
            if (cached != null) {
                return@withContext cached
            }
            
            // 2. Charger depuis DB avec timeout
            val fromDb = withTimeout(10_000L) {
                quizDao.getQuizWithQuestionsSync(id)
            }
            
            // 3. Mettre en cache
            quizCache.put(id, fromDb)
            fromDb
        } catch (e: Exception) {
            throw Exception("Impossible de charger le quiz: ${e.message}")
        }
    }
    
    /**
     * Sauvegarde un résultat de quiz de manière thread-safe.
     */
    override suspend fun saveQuizResult(result: QuizResultEntity) = withContext(ioDispatcher) {
        try {
            writeMutex.withLock {
                withTimeout(5_000L) {
                    resultDao.insertResult(result)
                }
            }
        } catch (e: Exception) {
            println("Error saving quiz result: ${e.message}")
            // Ne pas propager l'erreur pour permettre une dégradation gracieuse
        }
    }

    /**
     * Récupère les résultats pour un quiz donné.
     */
    override fun getResultsForQuiz(quizId: Long): Flow<List<QuizResultEntity>> = flow {
        try {
            val results = withTimeout(10_000L) {
                resultDao.getResultsForQuizSync(quizId)
            }
            emit(results)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }.flowOn(ioDispatcher)
    
    /**
     * Invalide le cache pour un quiz spécifique.
     * Utile après une mise à jour.
     */
    suspend fun invalidateQuizCache(quizId: Long) {
        quizCache.remove(quizId)
    }
    
    /**
     * Invalide tout le cache.
     */
    suspend fun clearCache() {
        quizCache.clear()
        quizListCache.clear()
    }
}
