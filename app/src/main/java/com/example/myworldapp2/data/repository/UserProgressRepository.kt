package com.example.myworldapp2.data.repository

import androidx.lifecycle.LiveData
import com.example.myworldapp2.data.dao.UserProgressDao
import com.example.myworldapp2.data.entity.UserProgress
import java.util.Date

/**
 * Репозиторий для работы с прогрессом пользователя
 */
class UserProgressRepository(private val userProgressDao: UserProgressDao) {

    // Получение всего прогресса пользователя
    fun getProgressByUser(userId: Long): LiveData<List<UserProgress>> {
        return userProgressDao.getProgressByUser(userId)
    }
    
    // Отметка статьи как прочитанной
    suspend fun markAsRead(userId: Long, entryId: Long) {
        val currentProgress = userProgressDao.getProgressByUserAndEntry(userId, entryId)
        if (currentProgress == null) {
            // Создаем новую запись о прогрессе
            val progress = UserProgress(
                userId = userId,
                entryId = entryId,
                isRead = true,
                readAt = Date()
            )
            userProgressDao.insert(progress)
        } else if (!currentProgress.isRead) {
            // Обновляем существующую запись
            userProgressDao.updateReadStatus(userId, entryId, true, Date())
        }
    }
    
    // Отметка статьи как непрочитанной
    suspend fun markAsUnread(userId: Long, entryId: Long) {
        val currentProgress = userProgressDao.getProgressByUserAndEntry(userId, entryId)
        if (currentProgress != null && currentProgress.isRead) {
            userProgressDao.updateReadStatus(userId, entryId, false, null)
        }
    }
    
    // Проверка, прочитана ли статья пользователем
    suspend fun isEntryRead(userId: Long, entryId: Long): Boolean {
        val progress = userProgressDao.getProgressByUserAndEntry(userId, entryId)
        return progress?.isRead == true
    }
    
    // Получение количества прочитанных статей пользователем
    suspend fun getReadEntriesCount(userId: Long): Int {
        return userProgressDao.getReadEntriesCount(userId)
    }
    
    // Получение количества прочитанных статей пользователем в определенной категории
    suspend fun getReadEntriesCountByCategory(userId: Long, categoryId: Long): Int {
        return userProgressDao.getReadEntriesCountByCategory(userId, categoryId)
    }
    
    // Получение общего прогресса пользователя (в процентах)
    suspend fun getUserOverallProgress(userId: Long): Float {
        return userProgressDao.getUserOverallProgress(userId)
    }
    
    // Получение прогресса пользователя по категории (в процентах)
    suspend fun getUserCategoryProgress(userId: Long, categoryId: Long): Float {
        return userProgressDao.getUserCategoryProgress(userId, categoryId)
    }
} 