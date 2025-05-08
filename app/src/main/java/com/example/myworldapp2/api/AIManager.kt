package com.example.myworldapp2.api

import android.content.Context
import android.util.Log
import com.example.myworldapp2.data.model.ChatMessage
import com.example.myworldapp2.data.model.Message

class AIManager(private val context: Context? = null) {
    private val TAG = "AIManager"
    
    companion object {
        const val AI_TYPE_GEMINI = "gemini"
    }
    
    // По умолчанию используем Gemini
    private val geminiClient = GeminiClient(context)
    
    // Current active AI type - всегда Gemini
    private var currentAI = AI_TYPE_GEMINI
    
    fun setAIType(type: String) {
        // Всегда используем Gemini
        Log.d(TAG, "Requested AI type: $type, using Gemini")
        currentAI = AI_TYPE_GEMINI
    }
    
    fun getAIType(): String {
        return currentAI // Всегда возвращаем Gemini
    }
    
    fun sendMessage(message: String, previousMessages: List<Any>? = null, callback: (String?, Exception?) -> Unit) {
        // Всегда используем только Gemini API
        if (previousMessages != null) {
            val geminiMessages = convertToGeminiMessages(previousMessages)
            geminiClient.generateChatResponse(geminiMessages, callback)
        } else {
            geminiClient.generateContent(message, callback)
        }
    }
    
    // Helper function to convert between message formats
    private fun convertToGeminiMessages(messages: List<Any>): List<ChatMessage> {
        return messages.mapNotNull { msg ->
            when (msg) {
                is ChatMessage -> msg
                is Message -> ChatMessage(
                    if (msg.sender == Message.SenderType.USER) "user" else "assistant", 
                    msg.text
                )
                else -> null
            }
        }
    }
} 