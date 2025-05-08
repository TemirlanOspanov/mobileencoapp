package com.example.myworldapp2.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myworldapp2.data.entity.QuizAnswer

/**
 * DAO для работы с ответами на вопросы викторин
 */
@Dao
interface QuizAnswerDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(answer: QuizAnswer): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(answers: List<QuizAnswer>): List<Long>
    
    @Update
    suspend fun update(answer: QuizAnswer)
    
    @Delete
    suspend fun delete(answer: QuizAnswer)
    
    @Query("SELECT * FROM quiz_answers WHERE id = :answerId")
    fun getAnswerById(answerId: Long): LiveData<QuizAnswer>
    
    @Query("SELECT * FROM quiz_answers WHERE questionId = :questionId ORDER BY id ASC")
    fun getAnswersByQuestion(questionId: Long): LiveData<List<QuizAnswer>>
    
    @Query("SELECT * FROM quiz_answers WHERE questionId = :questionId ORDER BY id ASC")
    suspend fun getAnswersByQuestionSync(questionId: Long): List<QuizAnswer>
    
    @Query("SELECT * FROM quiz_answers WHERE questionId = :questionId AND isCorrect = 1")
    fun getCorrectAnswerForQuestion(questionId: Long): LiveData<QuizAnswer>
    
    @Query("DELETE FROM quiz_answers WHERE questionId = :questionId")
    suspend fun deleteAnswersByQuestionId(questionId: Long)
    
    @Query("SELECT COUNT(*) FROM quiz_answers WHERE questionId = :questionId")
    suspend fun getAnswerCountByQuestion(questionId: Long): Int
    
    @Query("DELETE FROM quiz_answers")
    suspend fun deleteAll()
} 