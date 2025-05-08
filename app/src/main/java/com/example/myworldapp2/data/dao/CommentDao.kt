package com.example.myworldapp2.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myworldapp2.data.entity.Comment

/**
 * DAO для работы с комментариями
 */
@Dao
interface CommentDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment): Long
    
    @Update
    suspend fun update(comment: Comment)
    
    @Delete
    suspend fun delete(comment: Comment)
    
    @Query("SELECT * FROM comments WHERE id = :commentId")
    fun getCommentById(commentId: Long): LiveData<Comment>
    
    @Query("SELECT * FROM comments WHERE entryId = :entryId ORDER BY createdAt DESC")
    fun getCommentsByEntryId(entryId: Long): LiveData<List<Comment>>
    
    @Query("SELECT * FROM comments WHERE userId = :userId ORDER BY createdAt DESC")
    fun getCommentsByUser(userId: Long): LiveData<List<Comment>>
    
    @Query("SELECT COUNT(*) FROM comments WHERE entryId = :entryId")
    suspend fun getCommentCountByEntry(entryId: Long): Int
    
    @Query("SELECT COUNT(*) FROM comments WHERE userId = :userId")
    suspend fun getCommentCountByUser(userId: Long): Int
    
    @Query("DELETE FROM comments WHERE entryId = :entryId")
    suspend fun deleteAllCommentsForEntry(entryId: Long)
    
    @Query("DELETE FROM comments")
    suspend fun deleteAll()
} 