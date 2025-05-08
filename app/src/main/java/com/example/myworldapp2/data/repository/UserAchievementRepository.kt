package com.example.myworldapp2.data.repository

import androidx.lifecycle.LiveData
import com.example.myworldapp2.data.dao.UserAchievementDao
import com.example.myworldapp2.data.entity.Achievement
import com.example.myworldapp2.data.entity.UserAchievement
import java.util.Date

/**
 * Репозиторий для работы с достижениями пользователей
 */
class UserAchievementRepository(private val userAchievementDao: UserAchievementDao) {

    // Получение всех достижений пользователя
    fun getUserAchievements(userId: Long): LiveData<List<Achievement>> {
        return userAchievementDao.getUserAchievementsWithDetails(userId)
    }
    
    // Получение заработанных достижений пользователя
    fun getEarnedAchievements(userId: Long): LiveData<List<Achievement>> {
        return userAchievementDao.getEarnedAchievements(userId)
    }
    
    // Проверка, получил ли пользователь достижение
    suspend fun hasAchievement(userId: Long, achievementId: Long): Boolean {
        return userAchievementDao.getUserAchievement(userId, achievementId) != null
    }
    
    // Получение информации о конкретном достижении пользователя
    suspend fun getUserAchievement(userId: Long, achievementId: Long): UserAchievement? {
        return userAchievementDao.getUserAchievement(userId, achievementId)
    }
    
    // Разблокировка достижения для пользователя
    suspend fun unlockAchievement(userId: Long, achievementId: Long): Boolean {
        // Проверяем, есть ли уже такое достижение у пользователя
        if (hasAchievement(userId, achievementId)) {
            return false
        }
        
        // Добавляем достижение пользователю
        val userAchievement = UserAchievement(
            userId = userId,
            achievementId = achievementId,
            completedAt = Date()
        )
        userAchievementDao.insert(userAchievement)
        return true
    }
    
    // Получение количества достижений пользователя
    suspend fun getUserAchievementCount(userId: Long): Int {
        return userAchievementDao.getUserAchievementCount(userId)
    }
    
    // Получение общего количества достижений в системе
    suspend fun getTotalAchievementsCount(): Int {
        return userAchievementDao.getTotalAchievementsCount()
    }
    
    // Получение достижений пользователя по категории
    fun getUserAchievementsByCategory(userId: Long, categoryId: Long): LiveData<List<Achievement>> {
        return userAchievementDao.getUserAchievementsByCategory(userId, categoryId)
    }
    
    // Удаление достижения у пользователя (для администраторов или тестирования)
    suspend fun removeAchievement(userId: Long, achievementId: Long) {
        userAchievementDao.deleteUserAchievement(userId, achievementId)
    }
} 