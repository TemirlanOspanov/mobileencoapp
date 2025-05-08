package com.example.myworldapp2.data.model

import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Junction
import androidx.room.Relation
import com.example.myworldapp2.data.entity.Category
import com.example.myworldapp2.data.entity.Entry
import com.example.myworldapp2.data.entity.EntryTag
import com.example.myworldapp2.data.entity.Tag
import java.util.Date

/**
 * Модель данных для отображения статьи со всеми связанными сущностями
 */
data class EntryWithDetails(
    @Embedded
    val entry: Entry,
    
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: Category,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = EntryTag::class,
            parentColumn = "entryId",
            entityColumn = "tagId"
        )
    )
    val tags: List<Tag>
) {
    /**
     * Флаг, указывающий, добавлена ли статья в закладки текущим пользователем
     */
    @Ignore
    var isBookmarked: Boolean = false
    
    /**
     * Время последнего прочтения статьи текущим пользователем
     */
    @Ignore
    var lastReadAt: Date? = null
    
    /**
     * Количество комментариев к статье
     */
    @Ignore
    var commentsCount: Int = 0
} 