package com.example.myworldapp2.data.model

import java.util.Date

/**
 * Модель данных для сообщения в чате с AI-ассистентом
 */
data class Message(
    val id: String = System.currentTimeMillis().toString(),
    val text: String,
    val sender: SenderType,
    val timestamp: Date = Date(),
    val isError: Boolean = false
) {
    enum class SenderType {
        USER,
        BOT
    }
} 