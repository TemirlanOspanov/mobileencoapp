package com.example.myworldapp2.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myworldapp2.data.entity.Quiz
import com.example.myworldapp2.data.entity.QuizQuestion
import com.example.myworldapp2.data.entity.QuizAnswer
import com.example.myworldapp2.data.repository.QuizRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel для экрана управления вопросами викторины
 */
class QuizQuestionsViewModel(
    private val quizRepository: QuizRepository
) : ViewModel() {

    // LiveData для викторины
    private val _quiz = MutableLiveData<Quiz>()
    val quiz: LiveData<Quiz> = _quiz

    // LiveData для списка вопросов
    private val _questions = MutableLiveData<List<QuizQuestion>>()
    val questions: LiveData<List<QuizQuestion>> = _questions

    // LiveData для отображения ошибок
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // Загрузка викторины по ID
    suspend fun loadQuiz(quizId: Long) {
        try {
            val quiz = withContext(Dispatchers.IO) {
                quizRepository.getQuizByIdSync(quizId)
            }
            _quiz.value = quiz
        } catch (e: Exception) {
            _errorMessage.value = "Ошибка загрузки викторины: ${e.message}"
        }
    }

    // Загрузка вопросов викторины
    suspend fun loadQuestions(quizId: Long) {
        try {
            val questionsLiveData = quizRepository.getQuestionsByQuiz(quizId)
            questionsLiveData.observeForever { questions ->
                _questions.value = questions
            }
        } catch (e: Exception) {
            _errorMessage.value = "Ошибка загрузки вопросов: ${e.message}"
        }
    }

    // Добавление нового вопроса
    fun addQuestion(question: QuizQuestion) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    quizRepository.addQuestion(question)
                }
                // Вопросы обновятся автоматически через LiveData
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка добавления вопроса: ${e.message}"
            }
        }
    }

    // Обновление вопроса
    fun updateQuestion(question: QuizQuestion) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    quizRepository.updateQuestion(question)
                }
                // Вопросы обновятся автоматически через LiveData
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка обновления вопроса: ${e.message}"
            }
        }
    }

    // Удаление вопроса
    fun deleteQuestion(question: QuizQuestion) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    quizRepository.deleteQuestion(question)
                }
                // Вопросы обновятся автоматически через LiveData
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка удаления вопроса: ${e.message}"
            }
        }
    }

    // Удаление викторины
    suspend fun deleteQuiz(quiz: Quiz) {
        try {
            withContext(Dispatchers.IO) {
                quizRepository.deleteQuiz(quiz)
            }
        } catch (e: Exception) {
            _errorMessage.value = "Ошибка удаления викторины: ${e.message}"
            throw e
        }
    }

    // Обновление викторины
    suspend fun updateQuiz(quiz: Quiz) {
        try {
            withContext(Dispatchers.IO) {
                quizRepository.updateQuiz(quiz)
            }
        } catch (e: Exception) {
            _errorMessage.value = "Ошибка обновления викторины: ${e.message}"
            throw e
        }
    }

    /**
     * Возвращает список вариантов ответов для вопроса
     */
    suspend fun getAnswersForQuestion(questionId: Long): List<QuizAnswer> {
        return quizRepository.getAnswersForQuestion(questionId)
    }
    
    /**
     * Обновляет вопрос и его варианты ответов
     */
    fun updateQuestionWithAnswers(question: QuizQuestion, answers: List<QuizAnswer>) {
        viewModelScope.launch {
            try {
                // Обновляем вопрос
                quizRepository.updateQuestion(question)
                
                // Удаляем старые ответы
                quizRepository.deleteAnswersForQuestion(question.id)
                
                // Добавляем новые ответы
                for (answer in answers) {
                    val newAnswer = answer.copy(questionId = question.id)
                    quizRepository.insertAnswer(newAnswer)
                }
                
                // Загружаем вопросы заново
                loadQuestions(question.quizId)
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка обновления вопроса: ${e.message}"
            }
        }
    }

    /**
     * Добавляет новый вопрос и возвращает его ID
     */
    suspend fun addQuestionAndGetId(question: QuizQuestion): Long {
        return quizRepository.addQuestion(question)
    }
    
    /**
     * Добавляет новый вариант ответа и возвращает его ID
     */
    suspend fun addAnswer(answer: QuizAnswer): Long {
        return quizRepository.addAnswer(answer)
    }
} 