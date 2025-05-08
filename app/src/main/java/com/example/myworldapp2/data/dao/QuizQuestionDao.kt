package com.example.myworldapp2.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.myworldapp2.data.entity.QuestionWithAnswers
import com.example.myworldapp2.data.entity.QuizQuestion

/**
 * DAO для работы с вопросами викторин
 */
@Dao
interface QuizQuestionDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(question: QuizQuestion): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<QuizQuestion>): List<Long>
    
    @Update
    suspend fun update(question: QuizQuestion)
    
    @Delete
    suspend fun delete(question: QuizQuestion)
    
    @Query("SELECT * FROM quiz_questions WHERE id = :questionId")
    fun getQuestionById(questionId: Long): LiveData<QuizQuestion>
    
    @Query("SELECT * FROM quiz_questions WHERE quizId = :quizId ORDER BY id ASC")
    fun getQuestionsByQuiz(quizId: Long): LiveData<List<QuizQuestion>>
    
    @Query("SELECT COUNT(*) FROM quiz_questions WHERE quizId = :quizId")
    suspend fun getQuestionCountByQuiz(quizId: Long): Int
    
    @Transaction
    @Query("SELECT * FROM quiz_questions WHERE quizId = :quizId ORDER BY id ASC")
    fun getQuestionsWithAnswersByQuiz(quizId: Long): LiveData<List<QuestionWithAnswers>>
    
    // Синхронная версия метода для получения вопросов с ответами
    @Transaction
    @Query("SELECT * FROM quiz_questions WHERE quizId = :quizId ORDER BY id ASC")
    suspend fun getQuestionsWithAnswersByQuizSync(quizId: Long): List<QuestionWithAnswers>
    
    @Query("DELETE FROM quiz_questions")
    suspend fun deleteAll()
} 