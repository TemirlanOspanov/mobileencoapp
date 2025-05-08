package com.example.myworldapp2.api

import android.content.Context
import android.util.Log
import com.example.myworldapp2.data.model.ChatMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiClient(private val context: Context? = null) {
    private val TAG = "GeminiClient"
    
    // API ключ Gemini - замените на свой ключ
    private val apiKey = "AIzaSyBZFPmTCEXHsHX5n2yj3mUTg-H9yydGPjE"
    
    // URL и модель для запроса
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent"
    
    // Максимальная длина ответа - 500 символов
    private val MAX_RESPONSE_LENGTH = 500
    
    // HTTP клиент с увеличенными таймаутами
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
    
    /**
     * Генерирует ответ от Gemini API используя формат, указанный в curl-примере
     */
    fun generateContent(userMessage: String, callback: (String?, Exception?) -> Unit) {
        try {
            // Логируем запрос для отладки
            Log.d(TAG, "Generating content for message: $userMessage")
            
            // Подготавливаем JSON запрос строго по формату из curl-примера
            val json = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", userMessage + " Пожалуйста, ответь не более чем в 500 символов.")
                            })
                        })
                    })
                })
                
                // Добавляем настройки генерации для ограничения ответа
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.4) // Снижаем температуру для более предсказуемых ответов
                    put("maxOutputTokens", 150) // Ограничиваем количество токенов (примерно соответствует 500 символам)
                    put("topP", 0.8)
                })
            }
            
            // Полный URL с API ключом
            val url = "$baseUrl?key=$apiKey"
            
            // Создаем асинхронный запрос
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Выводим запрос в лог для отладки
                    Log.d(TAG, "Request URL: $url")
                    Log.d(TAG, "Request body: ${json.toString()}")
                    
                    val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
                    
                    val request = Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .addHeader("Content-Type", "application/json")
                        .build()
                    
                    Log.d(TAG, "Sending API request to Gemini with model gemini-2.0-flash")
                    
                    // Выполняем запрос
                    httpClient.newCall(request).execute().use { response ->
                        val responseBody = response.body?.string()
                        Log.d(TAG, "Received response: Code ${response.code}")
                        Log.d(TAG, "Response body: ${responseBody?.take(500)}")
                        
                        if (response.isSuccessful && responseBody != null) {
                            try {
                                val jsonResponse = JSONObject(responseBody)
                                
                                // Обрабатываем ошибку если она есть
                                if (jsonResponse.has("error")) {
                                    val errorJson = jsonResponse.getJSONObject("error")
                                    val errorMessage = errorJson.optString("message", "Unknown error")
                                    val errorCode = errorJson.optInt("code", 0)
                                    val error = Exception("API Error (code $errorCode): $errorMessage")
                                    
                                    Log.e(TAG, "Error from Gemini API: $errorMessage")
                                    CoroutineScope(Dispatchers.Main).launch {
                                        callback(null, error)
                                    }
                                    return@use
                                }
                                
                                // Извлекаем текст ответа по обновленному пути
                                if (jsonResponse.has("candidates")) {
                                    val candidates = jsonResponse.getJSONArray("candidates")
                                    if (candidates.length() > 0) {
                                        val candidate = candidates.getJSONObject(0)
                                        if (candidate.has("content")) {
                                            val content = candidate.getJSONObject("content")
                                            if (content.has("parts")) {
                                                val parts = content.getJSONArray("parts")
                                                if (parts.length() > 0) {
                                                    val part = parts.getJSONObject(0)
                                                    var text = part.optString("text")
                                                    
                                                    // Ограничиваем длину ответа
                                                    if (text.length > MAX_RESPONSE_LENGTH) {
                                                        text = text.substring(0, MAX_RESPONSE_LENGTH) + "..."
                                                    }
                                                    
                                                    if (text.isNotEmpty()) {
                                                        Log.d(TAG, "Successfully extracted response text (${text.length} chars): ${text.take(100)}...")
                                                        CoroutineScope(Dispatchers.Main).launch {
                                                            callback(text, null)
                                                        }
                                                        return@use
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                // Если мы здесь, значит не смогли извлечь текст
                                Log.e(TAG, "Could not extract text from response: $responseBody")
                                CoroutineScope(Dispatchers.Main).launch {
                                    callback(null, Exception("Не удалось извлечь текст из ответа API"))
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing JSON response", e)
                                CoroutineScope(Dispatchers.Main).launch {
                                    callback(null, e)
                                }
                            }
                        } else {
                            // Если ответ неуспешный
                            val errorMsg = "HTTP Error ${response.code}: ${response.message}"
                            Log.e(TAG, errorMsg)
                            Log.e(TAG, "Error response body: $responseBody")
                            CoroutineScope(Dispatchers.Main).launch {
                                callback(null, Exception(errorMsg))
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Network or other error", e)
                    CoroutineScope(Dispatchers.Main).launch {
                        callback(null, e)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in generateContent", e)
            callback(null, e)
        }
    }
    
    // Метод для поддержки чата с историей
    fun generateChatResponse(messages: List<ChatMessage>, callback: (String?, Exception?) -> Unit) {
        try {
            if (messages.isEmpty()) {
                callback(null, Exception("Нет сообщений для отправки"))
                return
            }
            
            // Создаем строку из последнего сообщения
            val lastMessage = messages.last()
            var userText = lastMessage.content
            
            // Добавляем инструкцию для краткого ответа
            if (!userText.contains("кратко") && !userText.contains("краткий ответ")) {
                userText += " Пожалуйста, ответь кратко."
            }
            
            // Вызываем обычный метод генерации с последним сообщением
            generateContent(userText, callback)
        } catch (e: Exception) {
            Log.e(TAG, "Error in generateChatResponse", e)
            callback(null, e)
        }
    }
} 