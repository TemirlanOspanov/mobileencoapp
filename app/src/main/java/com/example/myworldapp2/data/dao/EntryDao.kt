package com.example.myworldapp2.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.myworldapp2.data.entity.Entry
import com.example.myworldapp2.data.model.EntryWithDetails

/**
 * DAO для работы со статьями
 */
@Dao
interface EntryDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: Entry): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<Entry>): List<Long>
    
    @Update
    suspend fun update(entry: Entry)
    
    @Delete
    suspend fun delete(entry: Entry)
    
    @Query("SELECT * FROM entries WHERE id = :entryId")
    fun getEntryById(entryId: Long): LiveData<Entry>
    
    @Query("SELECT * FROM entries WHERE id = :entryId")
    suspend fun getEntryByIdSync(entryId: Long): Entry?
    
    @Query("SELECT * FROM entries ORDER BY updatedAt DESC, createdAt DESC")
    fun getAllEntries(): LiveData<List<Entry>>
    
    @Query("SELECT * FROM entries ORDER BY updatedAt DESC, createdAt DESC")
    suspend fun getAllEntriesSync(): List<Entry>
    
    @Query("SELECT * FROM entries WHERE categoryId = :categoryId ORDER BY updatedAt DESC, createdAt DESC")
    fun getEntriesByCategory(categoryId: Long): LiveData<List<Entry>>
    
    @Query("SELECT * FROM entries WHERE categoryId = :categoryId ORDER BY updatedAt DESC, createdAt DESC")
    suspend fun getEntriesByCategorySync(categoryId: Long): List<Entry>
    
    @Query("SELECT * FROM entries WHERE title LIKE '%' || :query || '%' ORDER BY updatedAt DESC, createdAt DESC")
    fun searchEntries(query: String): LiveData<List<Entry>>
    
    @Query("SELECT * FROM entries WHERE id IN (:entryIds) ORDER BY title ASC")
    suspend fun getEntriesByIds(entryIds: List<Long>): List<Entry>
    
    @Query("SELECT COUNT(*) FROM entries")
    suspend fun getEntryCount(): Int
    
    @Query("SELECT COUNT(*) FROM entries WHERE categoryId = :categoryId")
    suspend fun getEntryCountByCategory(categoryId: Long): Int
    
    @Query("SELECT COUNT(*) FROM entries WHERE categoryId = :categoryId")
    suspend fun getEntryCountByCategoryId(categoryId: Long): Int
    
    @Transaction
    @Query("""
        SELECT e.* FROM entries e 
        INNER JOIN entry_tags et ON e.id = et.entryId 
        INNER JOIN tags t ON et.tagId = t.id 
        WHERE t.name = :tagName
        ORDER BY e.title ASC
    """)
    fun getEntriesByTag(tagName: String): LiveData<List<Entry>>
    
    @Transaction
    @Query("""
        SELECT e.* FROM entries e 
        INNER JOIN bookmarks b ON e.id = b.entryId 
        WHERE b.userId = :userId
        ORDER BY b.createdAt DESC
    """)
    fun getBookmarkedEntries(userId: Long): LiveData<List<Entry>>
    
    @Transaction
    @Query("""
        SELECT e.* FROM entries e 
        INNER JOIN bookmarks b ON e.id = b.entryId 
        WHERE b.userId = :userId
        ORDER BY b.createdAt DESC
    """)
    suspend fun getBookmarkedEntriesSync(userId: Long): List<Entry>
    
    @Transaction
    @Query("""
        SELECT e.* FROM entries e 
        INNER JOIN user_progress up ON e.id = up.entryId 
        WHERE up.userId = :userId AND up.isRead = 1
        ORDER BY up.readAt DESC
    """)
    fun getReadEntries(userId: Long): LiveData<List<Entry>>
    
    @Transaction
    @Query("""
        SELECT e.* FROM entries e 
        LEFT JOIN user_progress up ON e.id = up.entryId AND up.userId = :userId
        WHERE up.id IS NULL OR up.isRead = 0
        ORDER BY e.title ASC
    """)
    fun getUnreadEntries(userId: Long): LiveData<List<Entry>>

    /**
     * Поиск статей по заголовку
     * @param query Поисковый запрос с символами % для LIKE запроса
     * @return Список статей, соответствующих запросу
     */
    @Query("SELECT * FROM entries WHERE title LIKE :query")
    suspend fun searchByTitle(query: String): List<Entry>

    /**
     * Поиск статей по содержимому
     * @param query Поисковый запрос с символами % для LIKE запроса
     * @return Список статей, соответствующих запросу
     */
    @Query("SELECT * FROM entries WHERE content LIKE :query")
    suspend fun searchByContent(query: String): List<Entry>

    /**
     * Поиск статей по заголовку и содержимому
     * @param query Поисковый запрос с символами % для LIKE запроса
     * @return Список статей, соответствующих запросу
     */
    @Query("SELECT * FROM entries WHERE title LIKE :query OR content LIKE :query")
    suspend fun searchByTitleOrContent(query: String): List<Entry>

    /**
     * Получает статью со всеми связанными данными - категорией и тегами
     */
    @Transaction
    @Query("SELECT * FROM entries WHERE id = :entryId")
    suspend fun getEntryWithDetailsSync(entryId: Long): EntryWithDetails?

    /**
     * Получает список статей по категории со всеми связанными данными
     */
    @Transaction
    @Query("SELECT * FROM entries WHERE categoryId = :categoryId ORDER BY updatedAt DESC")
    fun getEntriesWithDetailsByCategory(categoryId: Long): LiveData<List<EntryWithDetails>>
    
    /**
     * Получает список статей по уровню сложности
     */
    @Transaction
    @Query("SELECT * FROM entries WHERE difficultyLevel = :level ORDER BY updatedAt DESC")
    fun getEntriesWithDetailsByDifficultyLevel(level: Int): LiveData<List<EntryWithDetails>>
    
    /**
     * Поиск статей по заголовку и содержимому с сортировкой по релевантности
     */
    @Transaction
    @Query("SELECT * FROM entries WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY CASE WHEN title LIKE '%' || :query || '%' THEN 1 ELSE 2 END, updatedAt DESC")
    fun searchEntriesWithDetails(query: String): LiveData<List<EntryWithDetails>>

    @Query("DELETE FROM entries")
    suspend fun deleteAll()
} 