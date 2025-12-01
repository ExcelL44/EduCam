package com.excell44.educam.ui.viewmodel

import com.excell44.educam.data.ai.SmartyAI
import com.excell44.educam.data.local.dao.ChatMessageDao
import com.excell44.educam.data.local.entity.MessageType
import com.excell44.educam.util.AuthStateManager
import com.excell44.educam.util.Logger
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After

/**
 * Tests d'intégration pour ChatViewModel
 * Teste l'interaction entre l'UI et la logique IA
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    @Mock
    private lateinit var chatDao: ChatMessageDao

    @Mock
    private lateinit var smartyAI: SmartyAI

    @Mock
    private lateinit var authStateManager: AuthStateManager

    private lateinit var chatViewModel: ChatViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        MockitoAnnotations.openMocks(this)
        // Disable logging for tests
        Logger.enableReleaseMode()

        chatViewModel = ChatViewModel(chatDao, smartyAI, authStateManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `sendMessage should save user message and generate AI response`() = runTest {
        // Given
        val userId = "test_user"
        val userMessage = "Bonjour Smarty"
        val aiResponse = SmartyAI.AIResponse(
            message = "Bonjour ! Comment puis-je vous aider ?",
            confidence = 0.8f,
            subject = null,
            isLearned = false,
            messageType = com.excell44.educam.data.local.entity.MessageType.TEXT
        )

        whenever(authStateManager.getUserId()).thenReturn(userId)
        whenever(smartyAI.generateResponse(any(), any())).thenReturn(aiResponse)

        // When
        chatViewModel.sendMessage(userMessage)

        // Then
        verify(smartyAI, times(2)).saveChatMessage(any(), any(), any(), any(), any(), any(), any())
        verify(smartyAI).generateResponse(any(), any())
        verify(smartyAI).learnFromInteraction(any(), any(), any(), any())
    }

    @Test
    fun `sendMessage should handle empty messages gracefully`() = runTest {
        // Given
        val emptyMessage = ""

        // When
        chatViewModel.sendMessage(emptyMessage)

        // Then
        verify(smartyAI, never()).generateResponse(any(), any())
        verify(smartyAI, never()).saveChatMessage(any(), any(), any(), any(), any(), any(), any())
    }

    @Test
    fun `sendMessage should handle unauthenticated users gracefully`() = runTest {
        // Given
        val message = "Test message"
        whenever(authStateManager.getUserId()).thenReturn(null)

        // When
        chatViewModel.sendMessage(message)

        // Then
        verify(smartyAI, never()).generateResponse(any(), any())
        verify(smartyAI, never()).saveChatMessage(any(), any(), any(), any(), any(), any(), any())
    }

    @Test
    fun `sendMessage should handle AI errors gracefully`() = runTest {
        // Given
        val userId = "test_user"
        val userMessage = "Test message"
        whenever(authStateManager.getUserId()).thenReturn(userId)
        whenever(smartyAI.generateResponse(any(), any()))
            .thenThrow(RuntimeException("AI Error"))

        // When
        chatViewModel.sendMessage(userMessage)

        // Then - vérifie que l'UI est mise à jour malgré l'erreur
        verify(smartyAI, atLeastOnce()).saveChatMessage(any(), any(), any(), any(), any(), any(), any())
        // L'erreur devrait être gérée silencieusement ou loggée
    }

    @Test
    fun `clearError should reset error message`() = runTest {
        // Given - simuler un état d'erreur
        val viewModel = ChatViewModel(chatDao, smartyAI, authStateManager)

        // When
        viewModel.clearError()

        // Then - l'état devrait être nettoyé
        assertNull(viewModel.uiState.value.errorMessage)
    }
}
