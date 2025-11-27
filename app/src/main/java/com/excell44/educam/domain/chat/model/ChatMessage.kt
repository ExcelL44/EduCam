package com.excell44.educam.domain.chat.model

import android.net.Uri
import java.time.LocalDateTime
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val type: MessageType,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val imageUri: Uri? = null,
    val guidanceLevel: GuidanceLevel? = null
)
