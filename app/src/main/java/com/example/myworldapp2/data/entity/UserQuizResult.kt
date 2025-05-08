package com.example.myworldapp2.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.myworldapp2.util.DateConverter
import java.util.Date

/**
 * Сущность результата прохождения викторины пользователем
 */
@Entity(
    tableName = "user_quiz_results",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Quiz::class,
            parentColumns = ["id"],
            childColumns = ["quizId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("userId"),
        Index("quizId"),
        Index(value = ["userId", "quizId"], unique = true)
    ]
)
@TypeConverters(DateConverter::class)
data class UserQuizResult(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val userId: Long,
    val quizId: Long,
    val score: Int, // количество правильных ответов или процент
    val completedAt: Date = Date(),
    val createdAt: Date = Date()
) 