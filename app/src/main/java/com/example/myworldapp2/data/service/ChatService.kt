package com.example.myworldapp2.data.service

import android.content.Context
import android.util.Log
import com.example.myworldapp2.api.AIManager
import com.example.myworldapp2.data.model.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class ChatService(private val context: Context? = null) {
    private val TAG = "ChatService"
    private val aiManager = AIManager(context)
    
    // Store conversation history
    private val conversationHistory = mutableListOf<Any>()
    
    // For demo purposes, keep some fallback responses in case of network issues
    private val fallbackResponses = mapOf(
        "default_1" to "Это очень интересный вопрос! Давай разберем его вместе. В мире природы много удивительных явлений, и это одно из них.",
        "default_2" to "Знаешь, это довольно сложная тема, но я постараюсь объяснить её просто. В природе всё взаимосвязано, и этот вопрос помогает понять эти связи.",
        "default_3" to "Отличный вопрос! В энциклопедии природы этой теме посвящено немало страниц. Основные моменты, которые стоит знать...",
        "default_4" to "Это очень важная тема в природе. Многие учёные посвятили годы изучению этого вопроса. Вот основные факты...",
        "default_5" to "Знаешь, это очень интересное явление природы. Оно встречается довольно часто, но мало кто обращает на него внимание."
    )
    
    private val defaultFallbacks = listOf("default_1", "default_2", "default_3", "default_4", "default_5")
    
    // Always set to Gemini
    init {
        aiManager.setAIType(AIManager.AI_TYPE_GEMINI)
    }
    
    // Clear conversation history
    fun clearHistory() {
        conversationHistory.clear()
    }
    
    suspend fun getResponse(message: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Getting response for message: $message using Gemini")
            
            // Create a message object for Gemini
            val userMessage = ChatMessage("user", message)
            
            // Add the user message to conversation history
            conversationHistory.add(userMessage)
            
            // Call the AI service using suspendCancellableCoroutine
            val response = suspendCancellableCoroutine<Result<String>> { continuation ->
                aiManager.sendMessage(message, conversationHistory) { content, error ->
                    if (error != null) {
                        Log.e(TAG, "Error from Gemini API: ${error.message}", error)
                        
                        // If we have an error, return a fallback response
                        val fallbackResponse = fallbackResponses[defaultFallbacks.random()] ?: 
                            "Извини, я не смог ответить на твой вопрос. Попробуй спросить что-нибудь другое! Ошибка: ${error.message}"
                        continuation.resume(Result.success(fallbackResponse))
                    } else if (content != null) {
                        Log.d(TAG, "Received response from Gemini: ${content.take(100)}...")
                        
                        // Add the AI response to conversation history
                        val aiMessage = ChatMessage("assistant", content)
                        conversationHistory.add(aiMessage)
                        
                        continuation.resume(Result.success(content))
                    } else {
                        Log.e(TAG, "Unknown error: null content and null error")
                        continuation.resume(Result.failure(Exception("Unknown error occurred - null response")))
                    }
                }
            }
            
            return@withContext response
        } catch (e: Exception) {
            Log.e(TAG, "Exception in getResponse", e)
            Result.failure(e)
        }
    }
} 