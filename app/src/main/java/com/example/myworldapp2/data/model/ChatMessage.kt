package com.example.myworldapp2.data.model

/**
 * Представляет сообщение в чате - либо от пользователя, либо от ассистента
 */
data class ChatMessage(
    val role: String, // "user" или "assistant"
    val content: String
) 