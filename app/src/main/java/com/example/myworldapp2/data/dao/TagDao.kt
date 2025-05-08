package com.example.myworldapp2.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myworldapp2.data.entity.Entry
import com.example.myworldapp2.data.entity.Tag

/**
 * DAO для работы с тегами
 */
@Dao
interface TagDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tag: Tag): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tags: List<Tag>): List<Long>
    
    @Update
    suspend fun update(tag: Tag)
    
    @Delete
    suspend fun delete(tag: Tag)
    
    @Query("SELECT * FROM tags WHERE id = :tagId")
    fun getTagById(tagId: Long): LiveData<Tag>
    
    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): LiveData<List<Tag>>
    
    @Query("SELECT * FROM tags WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchTags(query: String): LiveData<List<Tag>>
    
    @Query("""
        SELECT t.* FROM tags t
        INNER JOIN entry_tags et ON t.id = et.tagId
        WHERE et.entryId = :entryId
        ORDER BY t.name ASC
    """)
    fun getTagsForEntry(entryId: Long): LiveData<List<Tag>>
    
    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getTagByName(name: String): Tag?

    /**
     * Поиск тегов по названию
     * @param query Поисковый запрос с символами % для LIKE запроса
     * @return Список тегов, соответствующих запросу
     */
    @Query("SELECT * FROM tags WHERE name LIKE :query")
    suspend fun searchTagsByName(query: String): List<Tag>

    /**
     * Поиск статей по названию тега
     * @param query Поисковый запрос с символами % для LIKE запроса
     * @return Список статей, соответствующих запросу
     */
    @Query("""
        SELECT e.* FROM entries e
        JOIN entry_tags et ON e.id = et.entryId
        JOIN tags t ON et.tagId = t.id
        WHERE t.name LIKE :query
    """)
    suspend fun searchEntriesByTagName(query: String): List<Entry>

    @Query("DELETE FROM tags")
    suspend fun deleteAll()
} 