package com.example.myworldapp2.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Сущность ответа на вопрос викторины
 */
@Entity(
    tableName = "quiz_answers",
    foreignKeys = [
        ForeignKey(
            entity = QuizQuestion::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("questionId")]
)
data class QuizAnswer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val questionId: Long, // Связь с вопросом
    val answerText: String,
    val isCorrect: Boolean, // Правильный ли ответ
    val createdAt: Date = Date()
) 