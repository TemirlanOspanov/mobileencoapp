package com.example.myworldapp2.data.repository

import androidx.lifecycle.LiveData
import com.example.myworldapp2.data.dao.QuizAnswerDao
import com.example.myworldapp2.data.dao.QuizDao
import com.example.myworldapp2.data.dao.QuizQuestionDao
import com.example.myworldapp2.data.entity.Quiz
import com.example.myworldapp2.data.entity.QuestionWithAnswers
import com.example.myworldapp2.data.entity.QuizAnswer
import com.example.myworldapp2.data.entity.QuizQuestion
import com.example.myworldapp2.data.entity.QuizWithDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Репозиторий для работы с викторинами, вопросами и ответами
 */
class QuizRepository(
    private val quizDao: QuizDao,
    private val questionDao: QuizQuestionDao,
    private val answerDao: QuizAnswerDao
) {
    
    // Получение всех викторин
    fun getAllQuizzes(): LiveData<List<Quiz>> {
        return quizDao.getAllQuizzes()
    }
    
    // Синхронное получение всех викторин
    suspend fun getAllQuizzesSync(): List<Quiz> {
        return quizDao.getAllQuizzesSync()
    }
    
    // Получение викторины по ID
    fun getQuizById(quizId: Long): LiveData<Quiz> {
        return quizDao.getQuizById(quizId)
    }
    
    // Синхронное получение викторины по ID
    suspend fun getQuizByIdSync(quizId: Long): Quiz? {
        return quizDao.getQuizByIdSync(quizId)
    }
    
    // Получение викторин по статье
    fun getQuizzesByEntry(entryId: Long): LiveData<List<Quiz>> {
        return quizDao.getQuizzesByEntry(entryId)
    }
    
    // Поиск викторин по названию
    fun searchQuizzes(query: String): LiveData<List<Quiz>> {
        return quizDao.searchQuizzes(query)
    }
    
    // Добавление новой викторины
    suspend fun addQuiz(quiz: Quiz): Long {
        return quizDao.insert(quiz)
    }
    
    // Обновление викторины
    suspend fun updateQuiz(quiz: Quiz) {
        quizDao.update(quiz)
    }
    
    // Удаление викторины
    suspend fun deleteQuiz(quiz: Quiz) {
        quizDao.delete(quiz)
    }
    
    // Получение количества викторин
    suspend fun getQuizzesCount(): Int {
        return quizDao.getQuizzesCount()
    }
    
    // Получение вопросов викторины
    fun getQuestionsByQuiz(quizId: Long): LiveData<List<QuizQuestion>> {
        return questionDao.getQuestionsByQuiz(quizId)
    }
    
    // Получение вопросов с вариантами ответов
    fun getQuestionsWithAnswersByQuiz(quizId: Long): LiveData<List<QuestionWithAnswers>> {
        return questionDao.getQuestionsWithAnswersByQuiz(quizId)
    }
    
    // Добавление вопроса
    suspend fun addQuestion(question: QuizQuestion): Long {
        return questionDao.insert(question)
    }
    
    // Обновление вопроса
    suspend fun updateQuestion(question: QuizQuestion) {
        questionDao.update(question)
    }
    
    // Удаление вопроса
    suspend fun deleteQuestion(question: QuizQuestion) {
        questionDao.delete(question)
    }
    
    // Получение ответов на вопрос
    fun getAnswersByQuestion(questionId: Long): LiveData<List<QuizAnswer>> {
        return answerDao.getAnswersByQuestion(questionId)
    }
    
    // Получение правильного ответа на вопрос
    fun getCorrectAnswerForQuestion(questionId: Long): LiveData<QuizAnswer> {
        return answerDao.getCorrectAnswerForQuestion(questionId)
    }
    
    // Добавление ответа
    suspend fun addAnswer(answer: QuizAnswer): Long {
        return answerDao.insert(answer)
    }
    
    // Вставка ответа (синоним для addAnswer для более ясного кода)
    suspend fun insertAnswer(answer: QuizAnswer): Long {
        return answerDao.insert(answer)
    }
    
    // Обновление ответа
    suspend fun updateAnswer(answer: QuizAnswer) {
        answerDao.update(answer)
    }
    
    // Удаление ответа
    suspend fun deleteAnswer(answer: QuizAnswer) {
        answerDao.delete(answer)
    }
    
    // Получение вариантов ответов для вопроса (синхронно)
    suspend fun getAnswersForQuestion(questionId: Long): List<QuizAnswer> {
        return answerDao.getAnswersByQuestionSync(questionId)
    }
    
    // Удаление всех ответов для вопроса
    suspend fun deleteAnswersForQuestion(questionId: Long) {
        answerDao.deleteAnswersByQuestionId(questionId)
    }
    
    // Получение детальной информации о викторине
    suspend fun getQuizWithDetails(quizId: Long): QuizWithDetails? {
        val quiz = quizDao.getQuizByIdSync(quizId) ?: return null
        val questions = questionDao.getQuestionsWithAnswersByQuizSync(quizId) ?: emptyList()
        
        return QuizWithDetails(
            quiz = quiz,
            entry = null, // В этом методе мы не загружаем связанную статью
            questions = questions
        )
    }
    
    // Получение количества вопросов в викторине
    suspend fun getQuestionCountByQuiz(quizId: Long): Int {
        return questionDao.getQuestionCountByQuiz(quizId)
    }
    
    // Получение полного списка викторин с деталями
    fun getAllQuizzesWithDetails(): LiveData<List<Quiz>> {
        // Используем метод для получения уникальных викторин
        return quizDao.getDistinctQuizzes()
    }
}