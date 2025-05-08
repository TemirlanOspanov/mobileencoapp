package com.example.myworldapp2.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.myworldapp2.util.DateConverter
import java.util.Date

/**
 * Сущность пользователя
 */
@Entity(tableName = "users")
@TypeConverters(DateConverter::class)
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val email: String,
    val passwordHash: String,
    val name: String,
    val avatarUrl: String? = null,
    val role: String = "user", // "user" или "admin"
    val createdAt: Date = Date()
) 