package com.example.myworldapp2.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.myworldapp2.data.entity.Quiz

/**
 * DAO для работы с викторинами
 */
@Dao
interface QuizDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(quiz: Quiz): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(quizzes: List<Quiz>): List<Long>
    
    @Update
    suspend fun update(quiz: Quiz)
    
    @Delete
    suspend fun delete(quiz: Quiz)
    
    @Query("SELECT * FROM quizzes WHERE id = :quizId")
    fun getQuizById(quizId: Long): LiveData<Quiz>
    
    @Query("SELECT * FROM quizzes WHERE id = :quizId")
    suspend fun getQuizByIdSync(quizId: Long): Quiz?
    
    @Query("SELECT * FROM quizzes ORDER BY title ASC")
    fun getAllQuizzes(): LiveData<List<Quiz>>
    
    @Query("SELECT DISTINCT * FROM quizzes ORDER BY title ASC")
    fun getDistinctQuizzes(): LiveData<List<Quiz>>
    
    @Query("SELECT * FROM quizzes ORDER BY title ASC")
    suspend fun getAllQuizzesSync(): List<Quiz>
    
    @Query("SELECT * FROM quizzes WHERE entryId = :entryId ORDER BY title ASC")
    fun getQuizzesByEntry(entryId: Long): LiveData<List<Quiz>>
    
    @Query("SELECT * FROM quizzes WHERE title LIKE '%' || :query || '%' ORDER BY title ASC")
    fun searchQuizzes(query: String): LiveData<List<Quiz>>
    
    @Query("SELECT COUNT(*) FROM quizzes")
    suspend fun getQuizCount(): Int
    
    @Query("DELETE FROM quizzes")
    suspend fun deleteAll()
    
    @Query("SELECT COUNT(*) FROM quizzes")
    suspend fun getQuizzesCount(): Int
    
    @Query("SELECT COUNT(*) FROM quizzes WHERE entryId = :entryId")
    suspend fun getQuizCountByEntry(entryId: Long): Int
    
    @Transaction
    @Query("""
        SELECT q.* FROM quizzes q 
        INNER JOIN user_quiz_results uqr ON q.id = uqr.quizId 
        WHERE uqr.userId = :userId
        ORDER BY uqr.completedAt DESC
    """)
    fun getCompletedQuizzes(userId: Long): LiveData<List<Quiz>>
    
    @Transaction
    @Query("""
        SELECT q.* FROM quizzes q 
        LEFT JOIN user_quiz_results uqr ON q.id = uqr.quizId AND uqr.userId = :userId
        WHERE uqr.id IS NULL
        ORDER BY q.title ASC
    """)
    fun getUncompletedQuizzes(userId: Long): LiveData<List<Quiz>>
} 