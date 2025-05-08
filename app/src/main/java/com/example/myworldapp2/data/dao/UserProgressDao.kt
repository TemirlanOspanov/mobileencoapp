package com.example.myworldapp2.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myworldapp2.data.entity.UserProgress
import java.util.Date

/**
 * DAO для работы с прогрессом пользователя
 */
@Dao
interface UserProgressDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userProgress: UserProgress): Long
    
    @Update
    suspend fun update(userProgress: UserProgress)
    
    @Delete
    suspend fun delete(userProgress: UserProgress)
    
    @Query("SELECT * FROM user_progress WHERE id = :progressId")
    fun getProgressById(progressId: Long): LiveData<UserProgress>
    
    @Query("SELECT * FROM user_progress WHERE userId = :userId ORDER BY createdAt DESC")
    fun getProgressByUser(userId: Long): LiveData<List<UserProgress>>
    
    @Query("SELECT * FROM user_progress WHERE entryId = :entryId ORDER BY createdAt DESC")
    fun getProgressByEntry(entryId: Long): LiveData<List<UserProgress>>
    
    @Query("""
        SELECT * FROM user_progress 
        WHERE userId = :userId AND entryId = :entryId
        LIMIT 1
    """)
    suspend fun getUserProgressForEntry(userId: Long, entryId: Long): UserProgress?
    
    @Query("""
        SELECT * FROM user_progress 
        WHERE userId = :userId AND entryId = :entryId
        LIMIT 1
    """)
    suspend fun getProgressByUserAndEntry(userId: Long, entryId: Long): UserProgress?
    
    @Query("""
        UPDATE user_progress 
        SET isRead = :isRead, readAt = :readAt 
        WHERE userId = :userId AND entryId = :entryId
    """)
    suspend fun updateReadStatus(userId: Long, entryId: Long, isRead: Boolean, readAt: Date?)
    
    @Query("""
        SELECT COUNT(*) FROM user_progress 
        WHERE userId = :userId AND isRead = 1
    """)
    suspend fun getReadEntriesCount(userId: Long): Int
    
    @Query("""
        SELECT COUNT(*) FROM user_progress up
        INNER JOIN entries e ON up.entryId = e.id
        WHERE up.userId = :userId AND e.categoryId = :categoryId AND up.isRead = 1
    """)
    suspend fun getReadEntriesCountByCategory(userId: Long, categoryId: Long): Int
    
    @Query("""
        SELECT * FROM user_progress
        WHERE userId = :userId AND isRead = 1
        ORDER BY readAt DESC
        LIMIT :limit
    """)
    suspend fun getRecentReadEntries(userId: Long, limit: Int): List<UserProgress>
    
    @Query("""
        SELECT AVG(
            CASE WHEN up.isRead = 1 THEN 1.0 ELSE 0.0 END
        ) * 100
        FROM entries e
        LEFT JOIN user_progress up ON e.id = up.entryId AND up.userId = :userId
    """)
    suspend fun getOverallReadingProgressPercent(userId: Long): Float
    
    @Query("""
        SELECT CASE 
            WHEN (SELECT COUNT(*) FROM entries) > 0 
            THEN (SELECT COUNT(*) FROM user_progress WHERE userId = :userId AND isRead = 1) * 100.0 / (SELECT COUNT(*) FROM entries)
            ELSE 0 
        END
    """)
    suspend fun getUserOverallProgress(userId: Long): Float
    
    @Query("""
        SELECT AVG(
            CASE WHEN up.isRead = 1 THEN 1.0 ELSE 0.0 END
        ) * 100
        FROM entries e
        LEFT JOIN user_progress up ON e.id = up.entryId AND up.userId = :userId
        WHERE e.categoryId = :categoryId
    """)
    suspend fun getCategoryReadingProgressPercent(userId: Long, categoryId: Long): Float
    
    @Query("""
        SELECT CASE 
            WHEN (SELECT COUNT(*) FROM entries WHERE categoryId = :categoryId) > 0 
            THEN (SELECT COUNT(*) FROM user_progress up 
                JOIN entries e ON up.entryId = e.id 
                WHERE up.userId = :userId AND e.categoryId = :categoryId AND up.isRead = 1) * 100.0 / 
                (SELECT COUNT(*) FROM entries WHERE categoryId = :categoryId)
            ELSE 0 
        END
    """)
    suspend fun getUserCategoryProgress(userId: Long, categoryId: Long): Float

    /**
     * Проверяет, была ли статья прочитана пользователем
     * @param userId ID пользователя
     * @param entryId ID статьи
     * @return true, если статья была прочитана, иначе false
     */
    @Query("SELECT COUNT(*) > 0 FROM user_progress WHERE userId = :userId AND entryId = :entryId AND isRead = 1")
    suspend fun isEntryRead(userId: Long, entryId: Long): Boolean

    @Query("DELETE FROM user_progress")
    suspend fun deleteAll()
} 