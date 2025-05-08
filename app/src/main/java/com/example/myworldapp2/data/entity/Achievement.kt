package com.example.myworldapp2.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.util.Date

/**
 * Сущность достижения в приложении
 */
@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /**
     * Название достижения
     */
    val title: String,
    
    /**
     * Описание достижения
     */
    val description: String,
    
    /**
     * Иконка для достижения (название ресурса в drawable)
     */
    @ColumnInfo(name = "icon_name")
    val iconName: String,
    
    /**
     * Количество баллов, которое дает достижение
     */
    val points: Int,
    
    /**
     * Тип достижения (например: "READING", "QUIZ", "COMMENT")
     */
    val type: String,
    
    /**
     * Текущий прогресс достижения
     */
    @ColumnInfo(name = "current_progress")
    val currentProgress: Int = 0,
    
    /**
     * Целевой прогресс для выполнения достижения
     */
    @ColumnInfo(name = "target_progress")
    val targetProgress: Int,
    
    /**
     * Дата создания достижения
     */
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),
    
    /**
     * Дата обновления достижения
     */
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date()
) {
    /**
     * Проверяет, выполнено ли достижение
     */
    fun isCompleted(): Boolean {
        return currentProgress >= targetProgress
    }
    
    /**
     * Возвращает процент выполнения достижения
     */
    fun getProgressPercentage(): Int {
        return if (targetProgress <= 0) 0 
               else (currentProgress * 100 / targetProgress).coerceIn(0, 100)
    }
} 