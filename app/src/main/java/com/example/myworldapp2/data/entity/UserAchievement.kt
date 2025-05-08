package com.example.myworldapp2.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.util.Date

/**
 * Сущность, представляющая связь между пользователем и достижением
 */
@Entity(
    tableName = "user_achievements",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Achievement::class,
            parentColumns = ["id"],
            childColumns = ["achievementId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["achievementId"]),
        Index(value = ["userId", "achievementId"], unique = true)
    ]
)
data class UserAchievement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "userId")
    val userId: Long,
    
    @ColumnInfo(name = "achievementId")
    val achievementId: Long,
    
    /**
     * Прогресс выполнения достижения пользователем
     */
    val progress: Int = 0,
    
    /**
     * Дата выполнения достижения
     */
    @ColumnInfo(name = "completed_at")
    val completedAt: Date? = null,
    
    /**
     * Флаг, указывающий, что пользователь уже видел уведомление о достижении
     */
    @ColumnInfo(name = "notification_seen")
    val notificationSeen: Boolean = false,
    
    /**
     * Дата создания записи
     */
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),
    
    /**
     * Дата обновления записи
     */
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date()
) {
    /**
     * Проверяет, выполнено ли достижение пользователем
     */
    fun isCompleted(): Boolean {
        return completedAt != null
    }
} 