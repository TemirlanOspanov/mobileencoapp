package com.example.myworldapp2.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.myworldapp2.util.DateConverter
import java.util.Date

/**
 * Сущность связи статьи и тега
 */
@Entity(
    tableName = "entry_tags",
    foreignKeys = [
        ForeignKey(
            entity = Entry::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Tag::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("entryId"),
        Index("tagId"),
        Index(value = ["entryId", "tagId"], unique = true)
    ]
)
@TypeConverters(DateConverter::class)
data class EntryTag(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val entryId: Long,
    val tagId: Long,
    val createdAt: Date = Date()
) 