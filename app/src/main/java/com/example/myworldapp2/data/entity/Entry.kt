package com.example.myworldapp2.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.myworldapp2.util.DateConverter
import java.util.Date

/**
 * Сущность статьи/записи в энциклопедии
 */
@Entity(
    tableName = "entries",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoryId")]
)
@TypeConverters(DateConverter::class)
data class Entry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val title: String,
    val content: String,
    val categoryId: Long,
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    
    /**
     * Уровень сложности материала:
     * 1 - начальный (для самых маленьких)
     * 2 - средний (для детей 7-10 лет)
     * 3 - продвинутый (для детей 11+ лет)
     */
    val difficultyLevel: Int = 1,
    
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) 