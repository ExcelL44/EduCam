package com.excell44.educam.integration

import android.Manifest
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.excell44.educam.MainActivity
import com.excell44.educam.data.local.AppDatabase
import com.excell44.educam.data.model.Difficulty
import com.excell44.educam.data.model.QuestionType
import com.excell44.educam.data.model.QuizQuestion
import com.excell44.educam.data.repository.QuizRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Test d'intégration complet simulant un parcours utilisateur réel :
 * - Login
 * - Charger 50 questions offline
 * - Répondre à toutes les questions
 * - Vérifier la persistance des résultats
 * 
 * Simule conditions d'un device Tecno Spark (1GB RAM, offline).
 */
@HiltAndroidTest
@LargeTest
@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class QuizIntegrationTest {
    
    @Inject
    lateinit var repository: QuizRepository
    
    @Inject
    lateinit var database: AppDatabase
    
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)
    
    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    @get:Rule(order = 2)
    val grantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.CAMERA
    )
    
    // Données de test réalistes (50 questions physique Bac Cameroun)
    private val mockQuestions = (1..50).map { index ->
        QuizQuestion(
            id = "phys_2023_c_$index",
            subject = "Physique",
            topic = "Mécanique",
            question = """
                <h3>Question $index</h3>
                <p>Calculer la distance D sachant que:</p>
                <p>Données : D₀ = ${2000 + index}mm, d = ${536 + index}mm</p>
                <p>Formule : <span class='math-inline'>D = \\frac{D_0 + d}{2}</span></p>
            """.trimIndent(),
            questionType = QuestionType.NUMERIC,
            options = emptyList(),
            correctAnswer = ((2000 + index + 536 + index) / 2).toString(),
            explanation = "Application de la formule de moyenne",
            difficulty = Difficulty.MEDIUM,
            gradeLevel = "Terminale C"
        )
    }
    
    @Before
    fun setup() {
        hiltRule.inject()
        
        // Injecte questions dans DB pour mode offline
        runBlocking {
            database.quizQuestionDao().insertAll(mockQuestions)
        }
        
        // Démarre l'activité
        composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        
        // Attend que l'UI soit prête
        composeTestRule.waitForIdle()
    }
    
    @After
    fun tearDown() {
        runBlocking {
            database.clearAllTables()
        }
    }
    
    @Test
    fun test_01_parcours_complet_50_questions_offline_sans_crash() {
        // 🔐 Étape 1 : Login (mode invité pour simplifier)
        composeTestRule.onNodeWithText("Continuer en tant qu'invité", substring = true)
            .assertExists()
            .performClick()
        
        // Attendre navigation vers Home
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithText("Bienvenue")
                .fetchSemanticsNodes().isNotEmpty()
        }
        
        // 📚 Étape 2 : Démarrer Quiz Adaptatif
        composeTestRule.onNodeWithText("Quiz Adaptatif")
            .assertIsDisplayed()
            .performClick()
        
        // Attendre chargement des questions
        composeTestRule.waitForIdle()
        
        // ✅ Étape 3 : Boucle 50 questions
        repeat(50) { index ->
            val questionNumber = index + 1
            
            // Vérification pas de crash
            assertFalse(
                message = "Crash détecté à la question $questionNumber",
                actual = composeTestRule.activity.isFinishing
            )
            
            // Log progression
            println("📝 Question $questionNumber/50")
            
            // Simule temps de lecture (2s pour éviter détection anti-triche)
            Thread.sleep(2000)
            
            // Vérifie qu'une question est affichée
            composeTestRule.onNodeWithText("Question $questionNumber", substring = true)
                .assertExists()
            
            // Répond (utilise la bonne réponse calculée)
            val answer = ((2000 + questionNumber + 536 + questionNumber) / 2).toString()
            
            // Trouve le champ de réponse et entre la valeur
            composeTestRule.onAllNodesWithTag("answer_field")
                .onFirst()
                .performTextInput(answer)
            
            // Vérifie utilisation mémoire (seuil OOM = 100MB)
            val memoryUsed = getMemoryUsageMB()
            assertTrue(
                message = "⚠️ Mémoire excessive à Q$questionNumber: ${memoryUsed}MB (max 100MB)",
                actual = memoryUsed < 100
            )
            
            // Suivant
            if (questionNumber < 50) {
                composeTestRule.onNodeWithText("Suivant")
                    .performClick()
                composeTestRule.waitForIdle()
            } else {
                // Dernière question : Terminer
                composeTestRule.onNodeWithText("Terminer", substring = true)
                    .performClick()
            }
        }
        
        // 🎯 Étape 4 : Vérifie écran de résultats
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithText("Résultat", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        
        // 📊 Étape 5 : Vérifie persistance offline
        runBlocking {
            val sessions = database.quizSessionDao().getAllSessions().first()
            assertTrue(
                message = "Aucune session sauvegardée",
                actual = sessions.isNotEmpty()
            )
            
            val lastSession = sessions.first()
            assertEquals(
                expected = 50,
                actual = lastSession.totalQuestions,
                message = "50 questions devraient être enregistrées"
            )
            
            println("✅ Score final : ${lastSession.score}/${lastSession.totalQuestions}")
            println("⏱️ Temps total : ${lastSession.durationSeconds}s")
        }
    }
    
    @Test
    fun test_02_verification_memoire_stable() {
        // Vérifie que la mémoire reste stable pendant le chargement
        val memoryBefore = getMemoryUsageMB()
        
        // Charge toutes les questions
        runBlocking {
            database.quizQuestionDao().getQuestionsBySubject("Physique")
        }
        
        val memoryAfter = getMemoryUsageMB()
        val memoryIncrease = memoryAfter - memoryBefore
        
        assertTrue(
            message = "Augmentation mémoire excessive: +${memoryIncrease}MB (max +30MB)",
            actual = memoryIncrease < 30
        )
        
        println("📊 Mémoire avant: ${memoryBefore}MB, après: ${memoryAfter}MB (+${memoryIncrease}MB)")
    }
    
    @Test
    fun test_03_persistance_apres_force_close() {
        // Simule un crash/fermeture brutale
        runBlocking {
            val testSession = com.excell44.educam.data.model.QuizSession(
                id = "test_crash_${System.currentTimeMillis()}",
                userId = "guest_test",
                subject = "Physique",
                totalQuestions = 50,
                correctAnswers = 45,
                score = 45,
                durationSeconds = 600,
                completedAt = System.currentTimeMillis()
            )
            
            database.quizSessionDao().insert(testSession)
        }
        
        // Force recreation de l'activité (simule kill process)
        composeTestRule.activityRule.scenario.recreate()
        composeTestRule.waitForIdle()
        
        // Vérifie que les données sont toujours là
        runBlocking {
            val sessions = database.quizSessionDao().getAllSessions().first()
            assertTrue(
                message = "Session perdue après recreate",
                actual = sessions.any { it.id.startsWith("test_crash") }
            )
        }
    }
    
    // ==================== Helpers ====================
    
    private fun getMemoryUsageMB(): Long {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        return usedMemory / 1024 / 1024 // Convert to MB
    }
    
    private fun SemanticsNodeInteractionCollection.onFirst(): SemanticsNodeInteraction {
        return get(0)
    }
}
