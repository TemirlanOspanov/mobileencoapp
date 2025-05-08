package com.example.myworldapp2.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.myworldapp2.util.DateConverter
import java.util.Date

/**
 * Сущность для лайков статей
 */
@Entity(
    tableName = "likes",
    foreignKeys = [
        ForeignKey(
            entity = Entry::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["entryId"]),
        Index(value = ["userId"]),
        Index(value = ["entryId", "userId"], unique = true)
    ]
)
@TypeConverters(DateConverter::class)
data class Like(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val entryId: Long,
    val userId: Long,
    val createdAt: Date = Date()
) 