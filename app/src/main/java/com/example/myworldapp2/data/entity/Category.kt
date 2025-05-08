package com.example.myworldapp2.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.myworldapp2.util.DateConverter
import java.util.Date

/**
 * Сущность категории контента
 */
@Entity(tableName = "categories")
@TypeConverters(DateConverter::class)
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val name: String,
    val description: String,
    val icon: String, // имя ресурса иконки или URL
    val color: String, // цвет в формате HEX (#FFFFFF)
    val createdAt: Date = Date()
) 