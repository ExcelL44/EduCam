@file:OptIn(ExperimentalMaterial3Api::class)

package com.excell44.educam.ui.screen.chat

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.excell44.educam.data.local.entity.MessageType
import com.excell44.educam.util.Logger
import com.excell44.educam.ui.util.screenPadding
import kotlinx.coroutines.launch

/**
 * Écran principal du chat IA Smarty
 */
@Composable
fun ChatScreen(
    onNavigateBack: () -> Unit,
    viewModel: com.excell44.educam.ui.viewmodel.ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val messages by viewModel.messages.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat IA Smarty") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Liste des messages
            LazyColumn(
                modifier = Modifier.weight(1f),
                state = rememberLazyListState(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { message ->
                    MessageBubble(
                        message = message,
                        onFeedbackSubmit = { messageId, isPositive ->
                            viewModel.submitUserFeedback(messageId, isPositive)
                        }
                    )
                }

                // Indicateur de frappe
                if (uiState.isTyping) {
                    item {
                        TypingIndicator()
                    }
                }
            }

            // Zone de saisie
            MessageInput(
                onSendMessage = { message ->
                    viewModel.sendMessage(message)
                },
                enabled = !uiState.isTyping
            )

            // Message d'erreur
            uiState.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/**
 * Bulle de message
 */
@Composable
fun MessageBubble(
    message: com.excell44.educam.ui.viewmodel.ChatMessage,
    onFeedbackSubmit: (Long, Boolean) -> Unit = { _, _ -> } // Nouveau paramètre pour le feedback
) {
    val isUser = message.isFromUser
    val backgroundColor = if (isUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = if (isUser) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.content,
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (message.confidence < 1.0f) {
                    Text(
                        text = "Confiance: ${(message.confidence * 100).toInt()}%",
                        color = textColor.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Boutons de feedback pour les messages IA (non utilisateur)
        if (!isUser && message.userFeedback == null) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { onFeedbackSubmit(message.id, true) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Filled.ThumbUp,
                        contentDescription = "J'aime cette réponse",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = { onFeedbackSubmit(message.id, false) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Filled.ThumbDown,
                        contentDescription = "Je n'aime pas cette réponse",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Indicateur de feedback déjà donné
        if (!isUser && message.userFeedback != null) {
            val feedbackIcon = if (message.userFeedback == 1.0f) Icons.Filled.ThumbUp else Icons.Filled.ThumbDown
            val feedbackColor = if (message.userFeedback == 1.0f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    feedbackIcon,
                    contentDescription = if (message.userFeedback == 1.0f) "Feedback positif" else "Feedback négatif",
                    tint = feedbackColor,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = if (message.userFeedback == 1.0f) "Utile" else "Pas utile",
                    style = MaterialTheme.typography.bodySmall,
                    color = feedbackColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Indicateur de frappe
 */
@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                    )
                }
                Text(
                    text = "Smarty écrit...",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

/**
 * Zone de saisie de message
 */
@Composable
fun MessageInput(
    onSendMessage: (String) -> Unit,
    enabled: Boolean = true
) {
    var message by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Tapez votre message...") },
            enabled = enabled,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(
                onSend = {
                    if (message.isNotBlank() && enabled) {
                        onSendMessage(message)
                        message = ""
                        focusManager.clearFocus()
                    }
                }
            )
        )

        IconButton(
            onClick = {
                if (message.isNotBlank() && enabled) {
                    onSendMessage(message)
                    message = ""
                    focusManager.clearFocus()
                }
            },
            enabled = message.isNotBlank() && enabled
        ) {
            Icon(
                Icons.Filled.Send,
                contentDescription = "Envoyer",
                tint = if (message.isNotBlank() && enabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                }
            )
        }
    }
}
