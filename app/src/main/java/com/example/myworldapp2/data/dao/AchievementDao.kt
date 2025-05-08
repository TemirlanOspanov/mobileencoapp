package com.example.myworldapp2.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myworldapp2.data.entity.Achievement

/**
 * DAO для работы с достижениями
 */
@Dao
interface AchievementDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(achievement: Achievement): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(achievements: List<Achievement>): List<Long>
    
    @Update
    suspend fun update(achievement: Achievement)
    
    @Delete
    suspend fun delete(achievement: Achievement)
    
    @Query("SELECT * FROM achievements WHERE id = :achievementId")
    fun getAchievementById(achievementId: Long): LiveData<Achievement>
    
    @Query("SELECT * FROM achievements ORDER BY title ASC")
    fun getAllAchievements(): LiveData<List<Achievement>>
    
    @Query("SELECT * FROM achievements WHERE type = :categoryId ORDER BY title ASC")
    fun getAchievementsByCategory(categoryId: Long): LiveData<List<Achievement>>
    
    @Query("SELECT * FROM achievements WHERE type = 'GLOBAL' ORDER BY title ASC")
    fun getGlobalAchievements(): LiveData<List<Achievement>>
    
    // Alias для getGlobalAchievements
    @Query("SELECT * FROM achievements WHERE type = 'GENERAL' ORDER BY title ASC")
    fun getGeneralAchievements(): LiveData<List<Achievement>>
    
    @Query("SELECT * FROM achievements WHERE type = :type ORDER BY title ASC")
    suspend fun getAchievementsByType(type: String): List<Achievement>
    
    @Query("SELECT COUNT(*) FROM achievements")
    suspend fun getAchievementCount(): Int
    
    @Query("SELECT COUNT(*) FROM achievements")
    suspend fun getTotalAchievementsCount(): Int
    
    @Query("SELECT COUNT(*) FROM achievements WHERE type = :categoryId")
    suspend fun getAchievementCountByCategory(categoryId: Long): Int
    
    @Query("""
        SELECT a.* FROM achievements a
        LEFT JOIN user_achievements ua ON a.id = ua.achievementId AND ua.userId = :userId
        WHERE ua.id IS NULL
        ORDER BY a.title ASC
    """)
    fun getUnlockedAchievements(userId: Long): LiveData<List<Achievement>>
    
    @Query("DELETE FROM achievements")
    suspend fun deleteAll()
} 