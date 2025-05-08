package com.example.myworldapp2.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myworldapp2.data.entity.UserQuizResult

/**
 * DAO для работы с результатами прохождения викторин пользователями
 */
@Dao
interface UserQuizResultDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(result: UserQuizResult): Long
    
    @Update
    suspend fun update(result: UserQuizResult)
    
    @Delete
    suspend fun delete(result: UserQuizResult)
    
    @Query("SELECT * FROM user_quiz_results WHERE id = :resultId")
    fun getResultById(resultId: Long): LiveData<UserQuizResult>
    
    @Query("SELECT * FROM user_quiz_results WHERE userId = :userId ORDER BY completedAt DESC")
    fun getResultsByUser(userId: Long): LiveData<List<UserQuizResult>>
    
    @Query("SELECT * FROM user_quiz_results WHERE quizId = :quizId ORDER BY score DESC, completedAt ASC")
    fun getResultsByQuiz(quizId: Long): LiveData<List<UserQuizResult>>
    
    @Query("""
        SELECT * FROM user_quiz_results 
        WHERE userId = :userId AND quizId = :quizId
        ORDER BY score DESC, completedAt DESC
        LIMIT 1
    """)
    suspend fun getUserQuizResult(userId: Long, quizId: Long): UserQuizResult?
    
    @Query("SELECT COUNT(*) FROM user_quiz_results WHERE userId = :userId")
    suspend fun getCompletedQuizCountByUser(userId: Long): Int
    
    @Query("SELECT AVG(score) FROM user_quiz_results WHERE userId = :userId")
    suspend fun getAverageScoreByUser(userId: Long): Float
    
    // Получение последних пройденных викторин пользователя
    @Query("""
        SELECT * FROM user_quiz_results 
        WHERE userId = :userId
        ORDER BY completedAt DESC
        LIMIT :limit
    """)
    suspend fun getRecentQuizResults(userId: Long, limit: Int): List<UserQuizResult>
    
    @Query("DELETE FROM user_quiz_results")
    suspend fun deleteAll()
} 