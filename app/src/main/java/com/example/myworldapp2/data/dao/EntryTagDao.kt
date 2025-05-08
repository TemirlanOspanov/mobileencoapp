package com.example.myworldapp2.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myworldapp2.data.entity.EntryTag

/**
 * DAO для работы со связями статей и тегов
 */
@Dao
interface EntryTagDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entryTag: EntryTag): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entryTags: List<EntryTag>): List<Long>
    
    @Delete
    suspend fun delete(entryTag: EntryTag)
    
    @Query("DELETE FROM entry_tags WHERE entryId = :entryId")
    suspend fun deleteAllForEntry(entryId: Long)
    
    @Query("DELETE FROM entry_tags WHERE entryId = :entryId AND tagId = :tagId")
    suspend fun deleteByEntryAndTag(entryId: Long, tagId: Long)
    
    @Query("SELECT EXISTS(SELECT 1 FROM entry_tags WHERE entryId = :entryId AND tagId = :tagId)")
    suspend fun exists(entryId: Long, tagId: Long): Boolean
    
    @Query("DELETE FROM entry_tags")
    suspend fun deleteAll()
} 