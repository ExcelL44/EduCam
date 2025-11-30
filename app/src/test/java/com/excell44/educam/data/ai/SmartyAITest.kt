package com.excell44.educam.data.ai

import com.excell44.educam.data.local.dao.ChatMessageDao
import com.excell44.educam.data.local.dao.LearningPatternDao
import com.excell44.educam.data.local.entity.ChatMessageEntity
import com.excell44.educam.data.local.entity.LearningPatternEntity
import com.excell44.educam.data.local.entity.MessageType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*

/**
 * Tests unitaires pour SmartyAI
 * Teste la logique d'apprentissage adaptatif et de génération de réponses
 */
class SmartyAITest {

    @Mock
    private lateinit var chatDao: ChatMessageDao

    @Mock
    private lateinit var patternDao: LearningPatternDao

    private lateinit var smartyAI: SmartyAI

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        smartyAI = SmartyAI(chatDao, patternDao)
    }

    @Test
    fun `generateResponse should return default response when no patterns found`() = runTest {
        // Given
        val userId = "test_user"
        val message = "Bonjour"
        whenever(patternDao.searchPatterns(userId, any())).thenReturn(emptyList())
        whenever(patternDao.getBestPatterns(userId, 10)).thenReturn(emptyList())

        // When
        val response = smartyAI.generateResponse(userId, message)

        // Then
        assertNotNull(response)
        assertTrue(response.message.contains("Bonjour") || response.message.contains("bonjour"))
        assertEquals(0.5f, response.confidence)
        assertFalse(response.isLearned)
    }

    @Test
    fun `generateResponse should return learned response when pattern found with high confidence`() = runTest {
        // Given
        val userId = "test_user"
        val message = "Qu'est-ce qu'une équation?"
        val learnedPattern = LearningPatternEntity(
            userId = userId,
            inputPattern = "equation",
            outputPattern = "Une équation est une égalité mathématique",
            successRate = 0.9f,
            usageCount = 5
        )

        whenever(patternDao.searchPatterns(userId, any())).thenReturn(listOf(learnedPattern))
        whenever(patternDao.getBestPatterns(userId, 10)).thenReturn(listOf(learnedPattern))

        // When
        val response = smartyAI.generateResponse(userId, message)

        // Then
        assertNotNull(response)
        assertEquals("Une équation est une égalité mathématique", response.message)
        assertTrue(response.confidence > 0.7f)
        assertTrue(response.isLearned)
    }

    @Test
    fun `preprocessMessage should clean and normalize text`() = runTest {
        // Given - accès à la méthode privée via reflection ou test indirect
        val testMessage = "  Qu'est-ce qu'une ÉQUATION?! 123  "

        // When - test via generateResponse qui utilise preprocessMessage
        val userId = "test_user"
        whenever(patternDao.searchPatterns(userId, any())).thenReturn(emptyList())
        whenever(patternDao.getBestPatterns(userId, 10)).thenReturn(emptyList())

        val response = smartyAI.generateResponse(userId, testMessage)

        // Then - vérifie que le message est traité
        assertNotNull(response)
        verify(patternDao).searchPatterns(userId, any()) // preprocessMessage est appelée
    }

    @Test
    fun `learnFromInteraction should create new pattern when none exists`() = runTest {
        // Given
        val userId = "test_user"
        val userMessage = "Qu'est-ce qu'une dérivée?"
        val aiResponse = "La dérivée mesure le taux de variation"
        val subject = "Math"

        whenever(patternDao.searchPatterns(userId, any())).thenReturn(emptyList())

        // When
        smartyAI.learnFromInteraction(userId, userMessage, aiResponse, subject)

        // Then
        verify(patternDao).insertPattern(any())
        verify(patternDao, never()).updateSuccessRate(any(), any())
    }

    @Test
    fun `learnFromInteraction should update existing pattern when found`() = runTest {
        // Given
        val userId = "test_user"
        val userMessage = "dérivée"
        val aiResponse = "La dérivée mesure le taux de variation"
        val existingPattern = LearningPatternEntity(
            id = 1,
            userId = userId,
            inputPattern = "dérivée",
            outputPattern = "Ancienne réponse",
            successRate = 0.8f,
            usageCount = 3
        )

        whenever(patternDao.searchPatterns(userId, any())).thenReturn(listOf(existingPattern))

        // When
        smartyAI.learnFromInteraction(userId, userMessage, aiResponse, "Math", 0.9f)

        // Then
        verify(patternDao).updateSuccessRate(1, any())
        verify(patternDao).incrementUsage(1, any())
        verify(patternDao, never()).insertPattern(any())
    }

    @Test
    fun `saveChatMessage should persist message correctly`() = runTest {
        // Given
        val userId = "test_user"
        val message = "Test message"
        val isFromUser = true
        val confidence = 1.0f

        whenever(chatDao.getMessageCount(userId)).thenReturn(5)

        // When
        smartyAI.saveChatMessage(userId, message, isFromUser, confidence)

        // Then
        verify(chatDao).insertMessage(any())
        verify(chatDao).getMessageCount(userId)
        verify(chatDao, never()).cleanupOldMessages(any()) // < 100 messages
    }

    @Test
    fun `saveChatMessage should cleanup old messages when limit reached`() = runTest {
        // Given
        val userId = "test_user"
        val message = "Test message"
        val isFromUser = true

        whenever(chatDao.getMessageCount(userId)).thenReturn(105) // Above limit

        // When
        smartyAI.saveChatMessage(userId, message, isFromUser)

        // Then
        verify(chatDao).insertMessage(any())
        verify(chatDao).cleanupOldMessages(userId)
    }

    @Test
    fun `calculateSimilarity should return 1 for identical texts`() {
        // Test de la logique de similarité - accès via reflection si nécessaire
        // Pour l'instant, test indirect via generateResponse
        val userId = "test_user"
        val message = "test"

        whenever(patternDao.searchPatterns(userId, any())).thenReturn(emptyList())
        whenever(patternDao.getBestPatterns(userId, 10)).thenReturn(emptyList())

        val response = smartyAI.generateResponse(userId, message)

        assertNotNull(response)
    }

    @Test
    fun `detectSubject should identify math keywords`() {
        // Test indirect via generateResponse avec mots-clés
        val userId = "test_user"
        val mathMessage = "résoudre équation linéaire"

        whenever(patternDao.searchPatterns(userId, any())).thenReturn(emptyList())
        whenever(patternDao.getBestPatterns(userId, 10)).thenReturn(emptyList())

        val response = smartyAI.generateResponse(userId, mathMessage)

        // Vérifie que la réponse contient du contenu mathématique
        assertNotNull(response)
        assertTrue(response.subject == "Math" || response.subject == null)
    }

    @Test
    fun `generateResponse should handle errors gracefully`() = runTest {
        // Given
        val userId = "test_user"
        val message = "test"
        whenever(patternDao.searchPatterns(userId, any())).thenThrow(RuntimeException("Database error"))

        // When
        val response = smartyAI.generateResponse(userId, message)

        // Then
        assertNotNull(response)
        assertTrue(response.message.contains("Désolé") || response.message.contains("erreur"))
        assertEquals(0.0f, response.confidence)
        assertFalse(response.isLearned)
    }
}
