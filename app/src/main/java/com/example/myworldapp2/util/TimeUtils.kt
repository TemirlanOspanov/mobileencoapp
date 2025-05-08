package com.example.myworldapp2.util

import android.content.Context
import com.example.myworldapp2.R
import java.util.Date

/**
 * Утилитарный класс для форматирования времени
 */
object TimeUtils {

    /**
     * Форматирует время в удобочитаемую строку "N времени назад"
     */
    fun getTimeAgo(context: Context, date: Date): String {
        val now = Date()
        val diff = now.time - date.time

        // Разница в миллисекундах
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            seconds < 60 -> context.getString(R.string.time_ago_just_now)
            minutes == 1L -> context.getString(R.string.time_ago_minute, 1)
            minutes < 60 -> context.getString(R.string.time_ago_minutes, minutes)
            hours == 1L -> context.getString(R.string.time_ago_hour, 1)
            hours < 24 -> context.getString(R.string.time_ago_hours, hours)
            days == 1L -> context.getString(R.string.time_ago_day, 1)
            else -> context.getString(R.string.time_ago_days, days)
        }
    }
} 