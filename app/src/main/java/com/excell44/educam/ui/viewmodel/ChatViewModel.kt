package com.excell44.educam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.excell44.educam.data.ai.SmartyAI
import com.excell44.educam.data.local.SecurePrefs
import com.excell44.educam.data.local.dao.ChatMessageDao
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

// UI model for chat messages
data class ChatMessage(
    val id: Long,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long,
    val confidence: Float,
    val isLearned: Boolean,
    val messageType: com.excell44.educam.data.local.entity.MessageType,
    val userFeedback: Float? = null // Feedback utilisateur (null = pas de feedback, 1.0 = positif, 0.0 = négatif)
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatDao: ChatMessageDao,
    private val smartyAI: SmartyAI,
    private val securePrefs: SecurePrefs
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
                val userId = securePrefs.getUserId()
                if (userId != null) {
                    chatDao.getRecentMessages(userId, 50)
                        .collect { entities ->
                            val messages = entities.map { entity ->
                                ChatMessage(
                                    id = entity.id,
                                    content = entity.message,
                                    isFromUser = entity.isFromUser,
                                    timestamp = entity.timestamp,
                                    confidence = entity.confidence,
                                    isLearned = entity.isLearned,
                                    messageType = entity.messageType,
                                    userFeedback = entity.userFeedback
                                )
                            }
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

        val userId = securePrefs.getUserId()
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
     * Soumet le feedback utilisateur pour un message IA.
     */
    fun submitUserFeedback(messageId: Long, isPositive: Boolean) {
        val userId = securePrefs.getUserId()
        if (userId == null) {
            Logger.w(TAG, "Cannot submit feedback: user not authenticated")
            return
        }

        val feedback = if (isPositive) 1.0f else 0.0f

        viewModelScope.launch {
            try {
                val updatedRows = chatDao.updateUserFeedback(messageId, feedback)
                if (updatedRows > 0) {
                    Logger.d(TAG, "Feedback submitted for message $messageId: ${if (isPositive) "positive" else "negative"}")

                    // Mettre à jour l'UI pour refléter le feedback
                    _messages.value = _messages.value.map { message ->
                        if (message.id == messageId && !message.isFromUser) {
                            message.copy(userFeedback = feedback)
                        } else {
                            message
                        }
                    }

                    // Utiliser le feedback pour améliorer l'apprentissage
                    // TODO: Intégrer avec SmartyAI pour apprentissage par renforcement
                } else {
                    Logger.w(TAG, "Failed to update feedback for message $messageId")
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Error submitting feedback", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Erreur lors de l'envoi du feedback"
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
