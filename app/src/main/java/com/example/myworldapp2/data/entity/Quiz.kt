package com.example.myworldapp2.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Сущность викторины
 */
@Entity(
    tableName = "quizzes",
    foreignKeys = [
        ForeignKey(
            entity = Entry::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("entryId")]
)
data class Quiz(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val entryId: Long, // Связь со статьей
    val title: String,
    val description: String,
    val createdAt: Date = Date()
) 