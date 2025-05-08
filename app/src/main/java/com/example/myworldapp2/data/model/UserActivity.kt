package com.example.myworldapp2.data.model

import java.util.Date

/**
 * Класс, представляющий активность пользователя в приложении
 */
data class UserActivity(
    val id: Long = 0,
    val userId: Long,
    val type: ActivityType,
    val title: String,
    val details: String,
    val timestamp: Date = Date(),
    val relatedId: Long // ID связанной сущности (статьи, викторины, достижения и т.д.)
)

/**
 * Перечисление типов активностей пользователя
 */
enum class ActivityType {
    READ_ENTRY,          // Прочитана статья
    COMPLETED_QUIZ,      // Пройдена викторина
    EARNED_ACHIEVEMENT,  // Получено достижение
    ADDED_BOOKMARK,      // Добавлена закладка
    ADDED_COMMENT        // Оставлен комментарий
} 