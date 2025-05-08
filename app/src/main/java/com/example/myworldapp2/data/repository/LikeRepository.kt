package com.example.myworldapp2.data.repository

import androidx.lifecycle.LiveData
import com.example.myworldapp2.data.dao.LikeDao
import com.example.myworldapp2.data.entity.Like

/**
 * Репозиторий для работы с лайками статей
 */
class LikeRepository(private val likeDao: LikeDao) {

    /**
     * Получает все лайки для статьи
     */
    fun getLikesByEntryId(entryId: Long): LiveData<List<Like>> {
        return likeDao.getLikesByEntryId(entryId)
    }
    
    /**
     * Получает количество лайков для статьи
     */
    suspend fun getLikeCountByEntry(entryId: Long): Int {
        return likeDao.getLikeCountByEntry(entryId)
    }
    
    /**
     * Проверяет, лайкнул ли пользователь статью
     */
    suspend fun isLikedByUser(entryId: Long, userId: Long): Boolean {
        return likeDao.isLikedByUser(entryId, userId)
    }
    
    /**
     * Добавляет лайк
     */
    suspend fun addLike(entryId: Long, userId: Long): Long {
        val like = Like(entryId = entryId, userId = userId)
        return likeDao.insertLike(like)
    }
    
    /**
     * Удаляет лайк
     */
    suspend fun removeLike(entryId: Long, userId: Long) {
        likeDao.deleteLikeByEntryAndUser(entryId, userId)
    }
    
    /**
     * Переключает лайк (добавляет, если нет, удаляет, если есть)
     */
    suspend fun toggleLike(entryId: Long, userId: Long): Boolean {
        val isLiked = isLikedByUser(entryId, userId)
        if (isLiked) {
            removeLike(entryId, userId)
        } else {
            addLike(entryId, userId)
        }
        return !isLiked
    }
} 