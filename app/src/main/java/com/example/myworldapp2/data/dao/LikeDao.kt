package com.example.myworldapp2.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myworldapp2.data.entity.Like

/**
 * DAO для работы с лайками статей
 */
@Dao
interface LikeDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLike(like: Like): Long
    
    @Delete
    suspend fun delete(like: Like)
    
    @Query("SELECT * FROM likes WHERE id = :likeId")
    fun getLikeById(likeId: Long): LiveData<Like>
    
    @Query("SELECT * FROM likes WHERE entryId = :entryId ORDER BY createdAt DESC")
    fun getLikesByEntryId(entryId: Long): LiveData<List<Like>>
    
    @Query("SELECT * FROM likes WHERE userId = :userId ORDER BY createdAt DESC")
    fun getLikesByUser(userId: Long): LiveData<List<Like>>
    
    @Query("SELECT COUNT(*) FROM likes WHERE entryId = :entryId")
    suspend fun getLikeCountByEntry(entryId: Long): Int
    
    @Query("SELECT COUNT(*) FROM likes")
    suspend fun getTotalLikeCount(): Int
    
    @Query("SELECT EXISTS(SELECT 1 FROM likes WHERE entryId = :entryId AND userId = :userId)")
    suspend fun isLikedByUser(entryId: Long, userId: Long): Boolean
    
    @Query("DELETE FROM likes WHERE entryId = :entryId AND userId = :userId")
    suspend fun deleteLikeByEntryAndUser(entryId: Long, userId: Long)
    
    @Query("DELETE FROM likes")
    suspend fun deleteAll()
} 