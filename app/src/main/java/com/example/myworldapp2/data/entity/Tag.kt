package com.example.myworldapp2.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.myworldapp2.util.DateConverter
import java.util.Date

/**
 * Сущность тега для статей
 */
@Entity(tableName = "tags")
@TypeConverters(DateConverter::class)
data class Tag(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val name: String,
    val color: String = "#4CAF50", // Цвет тега в формате HEX, по умолчанию зелёный
    val createdAt: Date = Date()
) 