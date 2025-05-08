package com.example.myworldapp2.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.example.myworldapp2.data.dao.AchievementDao
import com.example.myworldapp2.data.dao.UserAchievementDao
import com.example.myworldapp2.data.entity.Achievement
import com.example.myworldapp2.data.entity.UserAchievement
import com.example.myworldapp2.data.model.AchievementWithProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import android.util.Log

/**
 * Репозиторий для работы с достижениями
 */
class AchievementRepository(
    private val achievementDao: AchievementDao,
    private val userAchievementDao: UserAchievementDao
) {
    // Кеш для хранения списка достижений пользователя
    private val achievementsCache = mutableMapOf<String, LiveData<List<AchievementWithProgress>>>()

    /**
     * Получить все достижения с прогрессом текущего пользователя
     */
    fun getAchievementsWithProgress(userId: Long): LiveData<List<AchievementWithProgress>> {
        val cacheKey = "all_$userId"
        
        if (!achievementsCache.containsKey(cacheKey)) {
            val result = MediatorLiveData<List<AchievementWithProgress>>()
            
            // Получаем все достижения
            val achievementsSource = achievementDao.getAllAchievements()
            
            // Получаем достижения пользователя
            val userAchievementsSource = userAchievementDao.getUserAchievements(userId)
            
            result.addSource(achievementsSource) { achievements ->
                combineLatestData(result, achievements, userAchievementsSource.value, userId)
            }
            
            result.addSource(userAchievementsSource) { userAchievements ->
                combineLatestData(result, achievementsSource.value, userAchievements, userId)
            }
            
            achievementsCache[cacheKey] = result
        }
        
        return achievementsCache[cacheKey]!!
    }

    /**
     * Получить достижения определенного типа с прогрессом пользователя
     */
    fun getAchievementsByType(userId: Long, type: String): LiveData<List<AchievementWithProgress>> {
        val cacheKey = "${type}_$userId"
        
        if (!achievementsCache.containsKey(cacheKey)) {
            val result = MutableLiveData<List<AchievementWithProgress>>()
            
            // Создаем новую LiveData с фильтрацией по типу
            val filteredLiveData = getAchievementsWithProgress(userId).map { achievements ->
                achievements.filter { 
                    if (type == "ALL") true else it.achievement.type == type 
                }
            }
            
            achievementsCache[cacheKey] = filteredLiveData
        }
        
        return achievementsCache[cacheKey]!!
    }

    /**
     * Получить достижение по ID с прогрессом пользователя
     */
    suspend fun getAchievementWithProgress(userId: Long, achievementId: Long): AchievementWithProgress {
        return withContext(Dispatchers.IO) {
            val achievement = achievementDao.getAchievementById(achievementId).value
                ?: throw IllegalArgumentException("Achievement not found")
            
            val userAchievement = userAchievementDao.getUserAchievement(userId, achievementId)
            
            AchievementWithProgress(
                achievement = achievement,
                progress = userAchievement?.progress ?: 0,
                isCompleted = userAchievement?.isCompleted() ?: false,
                completedDate = userAchievement?.completedAt
            )
        }
    }

    /**
     * Обновить прогресс достижения пользователя
     */
    suspend fun updateAchievementProgress(userId: Long, achievementId: Long, newProgress: Int) {
        withContext(Dispatchers.IO) {
            try {
                val achievement = achievementDao.getAchievementById(achievementId).value
                    ?: return@withContext
                
                var userAchievement = userAchievementDao.getUserAchievement(userId, achievementId)
                
                if (userAchievement == null) {
                    // Создаем новую запись достижения пользователя
                    userAchievement = UserAchievement(
                        userId = userId,
                        achievementId = achievementId,
                        progress = 0,
                        createdAt = Date(),
                        updatedAt = Date()
                    )
                }
                
                // Обновляем прогресс
                val isNowCompleted = newProgress >= achievement.targetProgress && userAchievement.completedAt == null
                val updatedUserAchievement = userAchievement.copy(
                    progress = newProgress,
                    completedAt = if (isNowCompleted) Date() else userAchievement.completedAt,
                    updatedAt = Date()
                )
                
                userAchievementDao.insert(updatedUserAchievement)
                
                if (isNowCompleted) {
                    Log.d("AchievementRepository", "Достижение выполнено: ${achievement.title}")
                }
            } catch (e: Exception) {
                Log.e("AchievementRepository", "Ошибка при обновлении прогресса достижения: ${e.message}")
            }
        }
    }

    /**
     * Увеличить прогресс достижения пользователя на указанное значение
     */
    suspend fun incrementAchievementProgress(userId: Long, type: String, increment: Int = 1) {
        withContext(Dispatchers.IO) {
            try {
                // Получаем все достижения данного типа
                val achievements = achievementDao.getAchievementsByType(type)
                
                for (achievement in achievements) {
                    // Получаем текущий прогресс пользователя
                    var userAchievement = userAchievementDao.getUserAchievement(userId, achievement.id)
                    val currentProgress = userAchievement?.progress ?: 0
                    
                    // Увеличиваем прогресс
                    val newProgress = currentProgress + increment
                    
                    // Обновляем запись
                    updateAchievementProgress(userId, achievement.id, newProgress)
                }
            } catch (e: Exception) {
                Log.e("AchievementRepository", "Ошибка при инкременте прогресса: ${e.message}")
            }
        }
    }

    /**
     * Инициализировать достижения пользователя если их нет
     */
    suspend fun initUserAchievements(userId: Long) {
        withContext(Dispatchers.IO) {
            try {
                // Получаем все достижения
                val achievements = achievementDao.getAllAchievements().value ?: return@withContext
                
                for (achievement in achievements) {
                    // Проверяем, есть ли уже запись для пользователя
                    val userAchievement = userAchievementDao.getUserAchievement(userId, achievement.id)
                    
                    if (userAchievement == null) {
                        // Создаем запись с нулевым прогрессом
                        val newUserAchievement = UserAchievement(
                            userId = userId,
                            achievementId = achievement.id,
                            progress = 0,
                            createdAt = Date(),
                            updatedAt = Date()
                        )
                        
                        userAchievementDao.insert(newUserAchievement)
                    }
                }
                
                Log.d("AchievementRepository", "Достижения пользователя инициализированы")
            } catch (e: Exception) {
                Log.e("AchievementRepository", "Ошибка при инициализации достижений: ${e.message}")
            }
        }
    }

    /**
     * Получить статистику по достижениям пользователя
     */
    suspend fun getAchievementStats(userId: Long): AchievementStats {
        return withContext(Dispatchers.IO) {
            val totalAchievements = achievementDao.getTotalAchievementsCount()
            val completedAchievements = userAchievementDao.getUserAchievementCount(userId)
            
            AchievementStats(
                totalCount = totalAchievements,
                completedCount = completedAchievements,
                completionPercentage = if (totalAchievements > 0) 
                    (completedAchievements * 100 / totalAchievements) else 0
            )
        }
    }

    /**
     * Получить достижения по типу
     */
    suspend fun getAchievementByType(type: String): List<Achievement> {
        return achievementDao.getAchievementsByType(type)
    }

    /**
     * Вспомогательный метод для объединения данных из двух источников
     */
    private fun combineLatestData(
        result: MediatorLiveData<List<AchievementWithProgress>>,
        achievements: List<Achievement>?,
        userAchievements: List<UserAchievement>?,
        userId: Long
    ) {
        if (achievements == null) return
        
        val userAchievementsMap = userAchievements?.associateBy { it.achievementId } ?: emptyMap()
        
        // Создаем список с уникальными достижениями
        val achievementsWithProgress = achievements.distinctBy { it.id }.map { achievement ->
            val userAchievement = userAchievementsMap[achievement.id]
            
            AchievementWithProgress(
                achievement = achievement,
                progress = userAchievement?.progress ?: 0,
                isCompleted = userAchievement?.isCompleted() ?: false,
                completedDate = userAchievement?.completedAt
            )
        }
        
        result.value = achievementsWithProgress
    }
    
    companion object {
        @Volatile
        private var INSTANCE: AchievementRepository? = null
        
        fun getInstance(achievementDao: AchievementDao, userAchievementDao: UserAchievementDao): AchievementRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AchievementRepository(achievementDao, userAchievementDao).also { INSTANCE = it }
            }
        }
    }
}

/**
 * Статистика по достижениям пользователя
 */
data class AchievementStats(
    val totalCount: Int,
    val completedCount: Int,
    val completionPercentage: Int
) 