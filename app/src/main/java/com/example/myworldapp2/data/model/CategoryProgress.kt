package com.example.myworldapp2.data.model

/**
 * Класс данных для отображения прогресса по категории
 */
data class CategoryProgress(
    val categoryId: Long,
    val categoryName: String,
    val colorHex: String,
    val readEntries: Int,
    val totalEntries: Int
) 