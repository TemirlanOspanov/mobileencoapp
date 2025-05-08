package com.example.myworldapp2.util

import androidx.room.TypeConverter
import java.util.Date

/**
 * Конвертер для преобразования даты в Long и обратно для Room
 */
class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
} 