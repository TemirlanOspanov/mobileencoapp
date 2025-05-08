package com.example.myworldapp2.data.model

/**
 * Класс для хранения общей статистики пользователя
 */
data class UserStats(
    val totalEntries: Int,
    val readEntries: Int,
    val totalQuizzes: Int,
    val completedQuizzes: Int,
    val totalAchievements: Int,
    val earnedAchievements: Int
) 