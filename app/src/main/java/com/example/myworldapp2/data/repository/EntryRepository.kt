package com.example.myworldapp2.data.repository

import androidx.lifecycle.LiveData
import com.example.myworldapp2.data.dao.EntryDao
import com.example.myworldapp2.data.entity.Entry

/**
 * Репозиторий для работы со статьями
 */
class EntryRepository(private val entryDao: EntryDao) {

    // Получение всех статей
    val allEntries: LiveData<List<Entry>> = entryDao.getAllEntries()
    
    // Синхронное получение всех статей
    suspend fun getAllEntriesSync(): List<Entry> {
        return entryDao.getAllEntriesSync()
    }
    
    // Получение статьи по ID
    fun getEntryById(entryId: Long): LiveData<Entry> {
        return entryDao.getEntryById(entryId)
    }
    
    // Синхронное получение статьи по ID
    suspend fun getEntryByIdSync(entryId: Long): Entry? {
        return entryDao.getEntryByIdSync(entryId)
    }
    
    // Получение статей по категории
    fun getEntriesByCategory(categoryId: Long): LiveData<List<Entry>> {
        return entryDao.getEntriesByCategory(categoryId)
    }
    
    // Синхронное получение статей по категории
    suspend fun getEntriesByCategorySync(categoryId: Long): List<Entry> {
        return entryDao.getEntriesByCategorySync(categoryId)
    }
    
    // Поиск статей по названию
    fun searchEntries(query: String): LiveData<List<Entry>> {
        return entryDao.searchEntries(query)
    }
    
    // Получение статей по тегу
    fun getEntriesByTag(tagName: String): LiveData<List<Entry>> {
        return entryDao.getEntriesByTag(tagName)
    }
    
    // Получение закладок пользователя
    fun getBookmarkedEntries(userId: Long): LiveData<List<Entry>> {
        return entryDao.getBookmarkedEntries(userId)
    }
    
    // Получение прочитанных статей пользователя
    fun getReadEntries(userId: Long): LiveData<List<Entry>> {
        return entryDao.getReadEntries(userId)
    }
    
    // Получение непрочитанных статей пользователя
    fun getUnreadEntries(userId: Long): LiveData<List<Entry>> {
        return entryDao.getUnreadEntries(userId)
    }
    
    // Добавление новой статьи
    suspend fun insertEntry(entry: Entry): Long {
        return entryDao.insert(entry)
    }
    
    // Добавление нескольких статей
    suspend fun insertEntries(entries: List<Entry>): List<Long> {
        return entryDao.insertAll(entries)
    }
    
    // Обновление статьи
    suspend fun updateEntry(entry: Entry) {
        entryDao.update(entry)
    }
    
    // Удаление статьи
    suspend fun deleteEntry(entry: Entry) {
        entryDao.delete(entry)
    }
    
    // Получение количества всех статей
    suspend fun getEntryCount(): Int {
        return entryDao.getEntryCount()
    }
    
    // Получение общего количества статей
    suspend fun getTotalEntriesCount(): Int {
        return entryDao.getEntryCount()
    }
    
    // Получение количества статей в категории
    suspend fun getEntryCountByCategory(categoryId: Long): Int {
        return entryDao.getEntryCountByCategory(categoryId)
    }
} 