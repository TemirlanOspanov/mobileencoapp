package com.example.myworldapp2.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.myworldapp2.util.DateConverter
import java.util.Date

/**
 * Сущность прогресса пользователя по статьям
 */
@Entity(
    tableName = "user_progress",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Entry::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("userId"),
        Index("entryId"),
        Index(value = ["userId", "entryId"], unique = true)
    ]
)
@TypeConverters(DateConverter::class)
data class UserProgress(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val userId: Long,
    val entryId: Long,
    val isRead: Boolean = false,
    val readAt: Date? = null,
    val createdAt: Date = Date()
) 