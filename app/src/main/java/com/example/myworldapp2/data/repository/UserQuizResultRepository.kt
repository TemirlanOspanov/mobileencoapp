package com.example.myworldapp2.data.repository

import androidx.lifecycle.LiveData
import com.example.myworldapp2.data.dao.UserQuizResultDao
import com.example.myworldapp2.data.entity.UserQuizResult
import java.util.Date

/**
 * Репозиторий для работы с результатами прохождения викторин пользователями
 */
class UserQuizResultRepository(
    private val userQuizResultDao: UserQuizResultDao
) {
    
    // Получение результатов пользователя
    fun getResultsByUser(userId: Long): LiveData<List<UserQuizResult>> {
        return userQuizResultDao.getResultsByUser(userId)
    }
    
    // Получение результатов по викторине
    fun getResultsByQuiz(quizId: Long): LiveData<List<UserQuizResult>> {
        return userQuizResultDao.getResultsByQuiz(quizId)
    }
    
    // Получение результата пользователя по конкретной викторине
    suspend fun getUserQuizResult(userId: Long, quizId: Long): UserQuizResult? {
        return userQuizResultDao.getUserQuizResult(userId, quizId)
    }
    
    // Получение последних пройденных викторин пользователя
    suspend fun getRecentQuizResults(userId: Long, limit: Int): List<UserQuizResult> {
        return userQuizResultDao.getRecentQuizResults(userId, limit)
    }
    
    // Сохранение результата прохождения викторины
    suspend fun saveQuizResult(userId: Long, quizId: Long, score: Int): Long {
        // Проверяем, существует ли уже результат
        val existingResult = getUserQuizResult(userId, quizId)
        
        if (existingResult != null) {
            // Если существующий результат хуже нового, обновляем его
            if (existingResult.score < score) {
                val updatedResult = existingResult.copy(
                    score = score,
                    completedAt = Date()
                )
                userQuizResultDao.update(updatedResult)
                return updatedResult.id
            }
            return existingResult.id
        } else {
            // Создаем новый результат
            val newResult = UserQuizResult(
                userId = userId,
                quizId = quizId,
                score = score
            )
            return userQuizResultDao.insert(newResult)
        }
    }
    
    // Удаление результата
    suspend fun deleteResult(result: UserQuizResult) {
        userQuizResultDao.delete(result)
    }
    
    // Получение количества пройденных викторин
    suspend fun getCompletedQuizCount(userId: Long): Int {
        return userQuizResultDao.getCompletedQuizCountByUser(userId)
    }
    
    // Получение среднего балла пользователя
    suspend fun getAverageScore(userId: Long): Float {
        return userQuizResultDao.getAverageScoreByUser(userId)
    }
} 