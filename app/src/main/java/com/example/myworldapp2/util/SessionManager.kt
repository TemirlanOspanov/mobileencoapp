package com.example.myworldapp2.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * Класс для управления сессией пользователя и хранения данных между запусками приложения
 */
class SessionManager(context: Context) {
    private val TAG = "SessionManager"
    
    // Имя файла SharedPreferences
    private val PREF_NAME = "KidsEncyclopediaSession"
    
    // Режим доступа
    private val PRIVATE_MODE = Context.MODE_PRIVATE
    
    // Ключи для SharedPreferences
    companion object {
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_USER_ID = "userId"
        private const val KEY_USER_EMAIL = "userEmail"
        private const val KEY_USER_NAME = "userName"
    }
    
    // SharedPreferences и его редактор
    private val pref: SharedPreferences = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
    private val editor: SharedPreferences.Editor = pref.edit()
    
    /**
     * Сохраняет данные о входе пользователя
     * @param userId ID пользователя
     * @param email Email пользователя
     * @param name Имя пользователя
     */
    fun saveUserLogin(userId: Long, email: String, name: String) {
        Log.d(TAG, "Сохранение данных пользователя: $userId, $email, $name")
        
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putLong(KEY_USER_ID, userId)
        editor.putString(KEY_USER_EMAIL, email)
        editor.putString(KEY_USER_NAME, name)
        
        // Используем commit() вместо apply() для гарантированного сохранения
        val success = editor.commit()
        Log.d(TAG, "Данные пользователя сохранены: $success")
    }
    
    /**
     * Проверяет, вошел ли пользователь в систему
     * @return true, если пользователь вошел в систему
     */
    fun isLoggedIn(): Boolean {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    /**
     * Возвращает ID пользователя
     * @return ID пользователя или -1, если не найден
     */
    fun getUserId(): Long {
        return pref.getLong(KEY_USER_ID, -1)
    }
    
    /**
     * Возвращает email пользователя
     * @return Email пользователя или пустую строку, если не найден
     */
    fun getUserEmail(): String {
        return pref.getString(KEY_USER_EMAIL, "") ?: ""
    }
    
    /**
     * Возвращает имя пользователя
     * @return Имя пользователя или пустую строку, если не найдено
     */
    fun getUserName(): String {
        return pref.getString(KEY_USER_NAME, "") ?: ""
    }
    
    /**
     * Очищает данные сессии (выход из системы)
     */
    fun logout() {
        Log.d(TAG, "Выход пользователя из системы")
        
        editor.clear()
        val success = editor.commit()
        Log.d(TAG, "Данные сессии очищены: $success")
    }
} 