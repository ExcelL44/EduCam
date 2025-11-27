package com.excell44.educam.data.chat

import com.excell44.educam.domain.chat.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    val messages: Flow<List<ChatMessage>>
    suspend fun sendMessage(message: String, imageUri: String? = null)
}
