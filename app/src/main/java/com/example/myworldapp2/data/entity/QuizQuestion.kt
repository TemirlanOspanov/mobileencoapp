package com.example.myworldapp2.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Сущность вопроса викторины
 */
@Entity(
    tableName = "quiz_questions",
    foreignKeys = [
        ForeignKey(
            entity = Quiz::class,
            parentColumns = ["id"],
            childColumns = ["quizId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("quizId")]
)
data class QuizQuestion(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val quizId: Long, // Связь с викториной
    val questionText: String,
    val createdAt: Date = Date()
) 