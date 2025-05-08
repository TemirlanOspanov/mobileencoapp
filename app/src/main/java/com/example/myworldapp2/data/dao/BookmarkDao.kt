package com.example.myworldapp2.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myworldapp2.data.entity.Bookmark

/**
 * DAO для работы с закладками
 */
@Dao
interface BookmarkDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bookmark: Bookmark): Long
    
    @Delete
    suspend fun delete(bookmark: Bookmark)
    
    @Query("SELECT * FROM bookmarks WHERE userId = :userId ORDER BY createdAt DESC")
    fun getBookmarksByUser(userId: Long): LiveData<List<Bookmark>>
    
    @Query("SELECT entryId FROM bookmarks WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getBookmarkIdsByUserSync(userId: Long): List<Long>
    
    @Query("SELECT * FROM bookmarks WHERE userId = :userId AND entryId = :entryId")
    suspend fun getBookmarkByUserAndEntry(userId: Long, entryId: Long): Bookmark?
    
    @Query("DELETE FROM bookmarks WHERE userId = :userId AND entryId = :entryId")
    suspend fun deleteByUserAndEntry(userId: Long, entryId: Long)
    
    @Query("SELECT COUNT(*) FROM bookmarks WHERE userId = :userId")
    suspend fun getBookmarkCountByUser(userId: Long): Int
    
    @Query("SELECT COUNT(*) FROM bookmarks WHERE entryId = :entryId")
    suspend fun getBookmarkCountByEntry(entryId: Long): Int
    
    @Query("DELETE FROM bookmarks")
    suspend fun deleteAll()
} 