package com.example.myworldapp2.data.repository

import androidx.lifecycle.LiveData
import com.example.myworldapp2.data.dao.EntryTagDao
import com.example.myworldapp2.data.dao.TagDao
import com.example.myworldapp2.data.entity.EntryTag
import com.example.myworldapp2.data.entity.Tag

/**
 * Репозиторий для работы с тегами и связями с статьями
 */
class TagRepository(
    private val tagDao: TagDao,
    private val entryTagDao: EntryTagDao
) {

    // Получение всех тегов
    val allTags: LiveData<List<Tag>> = tagDao.getAllTags()
    
    // Получение тега по ID
    fun getTagById(tagId: Long): LiveData<Tag> {
        return tagDao.getTagById(tagId)
    }
    
    // Поиск тегов по имени
    fun searchTags(query: String): LiveData<List<Tag>> {
        return tagDao.searchTags(query)
    }
    
    // Получение тегов для статьи
    fun getTagsForEntry(entryId: Long): LiveData<List<Tag>> {
        return tagDao.getTagsForEntry(entryId)
    }
    
    // Добавление нового тега
    suspend fun addTag(tagName: String): Long {
        // Проверяем, существует ли уже такой тег
        val existingTag = tagDao.getTagByName(tagName)
        if (existingTag != null) {
            return existingTag.id
        }
        
        val tag = Tag(name = tagName.trim())
        return tagDao.insert(tag)
    }
    
    // Добавление нового тега с указанием цвета
    suspend fun addTagWithColor(tagName: String, tagColor: String): Long {
        // Проверяем, существует ли уже такой тег
        val existingTag = tagDao.getTagByName(tagName)
        if (existingTag != null) {
            return existingTag.id
        }
        
        val tag = Tag(name = tagName.trim(), color = tagColor)
        return tagDao.insert(tag)
    }
    
    // Обновление тега
    suspend fun updateTag(tag: Tag) {
        tagDao.update(tag)
    }
    
    // Удаление тега
    suspend fun deleteTag(tag: Tag) {
        tagDao.delete(tag)
    }
    
    // Добавление нескольких тегов
    suspend fun addTags(tagNames: List<String>): List<Long> {
        val tags = tagNames.map { Tag(name = it.trim()) }
        return tagDao.insertAll(tags)
    }
    
    // Добавление связи между статьей и тегом
    suspend fun addTagToEntry(entryId: Long, tagId: Long): Long? {
        // Проверяем, существует ли уже такая связь
        val exists = entryTagDao.exists(entryId, tagId)
        if (exists) {
            return null
        }
        
        val entryTag = EntryTag(entryId = entryId, tagId = tagId)
        return entryTagDao.insert(entryTag)
    }
    
    // Добавление тега (по имени) к статье - создает тег, если его нет
    suspend fun addTagToEntryByName(entryId: Long, tagName: String): Long? {
        val tagId = addTag(tagName)
        return addTagToEntry(entryId, tagId)
    }
    
    // Удаление связи между статьей и тегом
    suspend fun removeTagFromEntry(entryId: Long, tagId: Long) {
        entryTagDao.deleteByEntryAndTag(entryId, tagId)
    }
    
    // Удаление всех тегов у статьи
    suspend fun removeAllTagsFromEntry(entryId: Long) {
        entryTagDao.deleteAllForEntry(entryId)
    }
    
    // Обновление тегов для статьи (удаляет все и добавляет новые)
    suspend fun updateEntryTags(entryId: Long, tagNames: List<String>) {
        // Удаляем все текущие теги
        removeAllTagsFromEntry(entryId)
        
        // Добавляем новые теги
        for (tagName in tagNames) {
            addTagToEntryByName(entryId, tagName)
        }
    }
}