package com.excell44.educam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.excell44.educam.data.ai.SmartyAI
import com.excell44.educam.data.local.dao.ChatMessageDao
import com.excell44.educam.ui.screen.chat.ChatMessage
import com.excell44.educam.util.AuthStateManager
import com.excell44.educam.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val isTyping: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatDao: ChatMessageDao,
    private val smartyAI: SmartyAI,
    private val authStateManager: AuthStateManager
) : ViewModel() {

    private val TAG = "ChatViewModel"

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    init {
        loadChatHistory()
        Logger.d(TAG, "ChatViewModel initialized")
    }

    /**
     * Charge l'historique des messages depuis la base de données.
     */
    private fun loadChatHistory() {
        viewModelScope.launch {
            try {
                val userId = authStateManager.getUserId()
                if (userId != null) {
                    chatDao.getRecentMessages(userId, 50)
                        .map { entities ->
                            entities.map { entity ->
                                ChatMessage(
                                    id = entity.id,
                                    content = entity.message,
                                    isFromUser = entity.isFromUser,
                                    timestamp = entity.timestamp,
                                    confidence = entity.confidence,
                                    isLearned = entity.isLearned,
                                    messageType = entity.messageType
                                )
                            }
                        }
                        .collect { messages ->
                            _messages.value = messages
                            Logger.d(TAG, "Loaded ${messages.size} messages from history")
                        }
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Error loading chat history", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Erreur lors du chargement de l'historique"
                )
            }
        }
    }

    /**
     * Envoie un message utilisateur et obtient une réponse de l'IA.
     */
    fun sendMessage(message: String) {
        if (message.isBlank()) return

        val userId = authStateManager.getUserId()
        if (userId == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Utilisateur non connecté"
            )
            return
        }

        viewModelScope.launch {
            try {
                Logger.d(TAG, "Sending message: $message")

                // 1. Sauvegarder le message utilisateur
                val userMessageId = System.currentTimeMillis()
                val userMessage = ChatMessage(
                    id = userMessageId,
                    content = message,
                    isFromUser = true,
                    timestamp = userMessageId,
                    confidence = 1.0f,
                    isLearned = false,
                    messageType = com.excell44.educam.data.local.entity.MessageType.TEXT
                )

                // Ajouter à l'UI immédiatement
                _messages.value = _messages.value + userMessage

                // Sauvegarder en DB
                smartyAI.saveChatMessage(
                    userId = userId,
                    message = message,
                    isFromUser = true,
                    confidence = 1.0f,
                    messageType = com.excell44.educam.data.local.entity.MessageType.TEXT
                )

                // 2. Activer l'indicateur de frappe
                _uiState.value = _uiState.value.copy(isTyping = true)

                // Simuler délai de frappe réaliste (1-3 secondes)
                delay((1000L..3000L).random())

                // 3. Générer la réponse IA
                val aiResponse = smartyAI.generateResponse(userId, message)

                // 4. Créer le message IA
                val aiMessageId = System.currentTimeMillis()
                val aiMessage = ChatMessage(
                    id = aiMessageId,
                    content = aiResponse.message,
                    isFromUser = false,
                    timestamp = aiMessageId,
                    confidence = aiResponse.confidence,
                    isLearned = aiResponse.isLearned,
                    messageType = aiResponse.messageType
                )

                // 5. Ajouter à l'UI
                _messages.value = _messages.value + aiMessage

                // 6. Sauvegarder en DB
                smartyAI.saveChatMessage(
                    userId = userId,
                    message = aiResponse.message,
                    isFromUser = false,
                    confidence = aiResponse.confidence,
                    messageType = aiResponse.messageType,
                    isLearned = aiResponse.isLearned
                )

                // 7. Apprendre de cette interaction
                smartyAI.learnFromInteraction(
                    userId = userId,
                    userMessage = message,
                    aiResponse = aiResponse.message,
                    subject = aiResponse.subject
                )

                // 8. Désactiver l'indicateur de frappe
                _uiState.value = _uiState.value.copy(isTyping = false)

                Logger.d(TAG, "AI response sent with confidence: ${aiResponse.confidence}")

            } catch (e: Exception) {
                Logger.e(TAG, "Error sending message", e)
                _uiState.value = _uiState.value.copy(
                    isTyping = false,
                    errorMessage = "Erreur lors de l'envoi du message"
                )
            }
        }
    }

    /**
     * Efface l'erreur affichée.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Nettoie les ressources quand le ViewModel est détruit.
     */
    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "ChatViewModel cleared")
    }
}
