package com.example.myworldapp2.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.myworldapp2.data.entity.Achievement
import com.example.myworldapp2.data.entity.UserAchievement

/**
 * DAO для работы с достижениями пользователей
 */
@Dao
interface UserAchievementDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userAchievement: UserAchievement): Long
    
    @Update
    suspend fun update(userAchievement: UserAchievement)
    
    @Delete
    suspend fun delete(userAchievement: UserAchievement)
    
    @Query("SELECT * FROM user_achievements WHERE id = :id")
    fun getUserAchievementById(id: Long): LiveData<UserAchievement>
    
    @Query("SELECT * FROM user_achievements WHERE userId = :userId ORDER BY completed_at DESC")
    fun getUserAchievements(userId: Long): LiveData<List<UserAchievement>>
    
    @Query("SELECT * FROM user_achievements WHERE userId = :userId AND achievementId = :achievementId")
    suspend fun getUserAchievement(userId: Long, achievementId: Long): UserAchievement?
    
    @Query("DELETE FROM user_achievements WHERE userId = :userId AND achievementId = :achievementId")
    suspend fun deleteUserAchievement(userId: Long, achievementId: Long)
    
    @Query("SELECT COUNT(*) FROM user_achievements WHERE userId = :userId")
    suspend fun getUserAchievementCount(userId: Long): Int
    
    @Transaction
    @Query("""
        SELECT a.* FROM achievements a
        INNER JOIN user_achievements ua ON a.id = ua.achievementId
        WHERE ua.userId = :userId
        ORDER BY ua.completed_at DESC
    """)
    fun getUserAchievementsWithDetails(userId: Long): LiveData<List<Achievement>>
    
    @Transaction
    @Query("""
        SELECT a.* FROM achievements a
        INNER JOIN user_achievements ua ON a.id = ua.achievementId
        WHERE ua.userId = :userId
        ORDER BY ua.completed_at DESC
    """)
    fun getEarnedAchievements(userId: Long): LiveData<List<Achievement>>
    
    @Transaction
    @Query("""
        SELECT a.* FROM achievements a
        INNER JOIN user_achievements ua ON a.id = ua.achievementId
        WHERE ua.userId = :userId AND a.type = :categoryId
        ORDER BY ua.completed_at DESC
    """)
    fun getUserAchievementsByCategory(userId: Long, categoryId: Long): LiveData<List<Achievement>>
    
    // Получение общего количества достижений в системе
    @Query("SELECT COUNT(*) FROM achievements")
    suspend fun getTotalAchievementsCount(): Int
    
    @Query("DELETE FROM user_achievements")
    suspend fun deleteAll()
} 