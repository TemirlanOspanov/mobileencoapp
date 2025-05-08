package com.example.myworldapp2.data.model

import com.example.myworldapp2.data.entity.Achievement
import java.util.Date

/**
 * Модель, объединяющая Achievement с информацией о прогрессе пользователя
 */
data class AchievementWithProgress(
    /**
     * Достижение
     */
    val achievement: Achievement,
    
    /**
     * Текущий прогресс пользователя
     */
    val progress: Int = 0,
    
    /**
     * Флаг, указывающий, что достижение выполнено
     */
    val isCompleted: Boolean = false,
    
    /**
     * Дата выполнения достижения (null, если не выполнено)
     */
    val completedDate: Date? = null
) {
    /**
     * Получает процент выполнения достижения
     */
    fun getProgressPercent(): Int {
        val targetProgress = achievement.targetProgress
        return if (targetProgress <= 0) 0 
        else (progress * 100 / targetProgress).coerceIn(0, 100)
    }
} 