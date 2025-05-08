package com.example.myworldapp2.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myworldapp2.data.entity.QuestionWithAnswers
import com.example.myworldapp2.data.entity.QuizWithDetails
import com.example.myworldapp2.data.repository.QuizRepository
import com.example.myworldapp2.data.repository.UserQuizResultRepository
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана прохождения викторины
 */
class QuizPlayViewModel(
    private val quizRepository: QuizRepository,
    private val userQuizResultRepository: UserQuizResultRepository
) : ViewModel() {

    // ID текущего пользователя (пока заглушка)
    // В реальном приложении будет получаться из AuthRepository или SessionManager
    private val currentUserId: Long = 1L

    // LiveData для данных викторины
    private val _quizWithDetails = MutableLiveData<QuizWithDetails>()
    val quizWithDetails: LiveData<QuizWithDetails> = _quizWithDetails

    // LiveData для текущего вопроса
    private val _currentQuestion = MutableLiveData<QuestionWithAnswers>()
    val currentQuestion: LiveData<QuestionWithAnswers> = _currentQuestion

    // LiveData для прогресса (процент выполнения)
    private val _questionProgress = MutableLiveData<Int>()
    val questionProgress: LiveData<Int> = _questionProgress

    // LiveData для отображения номера вопроса (текущий/всего)
    private val _questionNumber = MutableLiveData<Pair<Int, Int>>()
    val questionNumber: LiveData<Pair<Int, Int>> = _questionNumber

    // LiveData для состояния загрузки
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData для завершения викторины с результатом (счет, всего вопросов)
    private val _quizCompleted = MutableLiveData<Pair<Int, Int>?>()
    val quizCompleted: LiveData<Pair<Int, Int>?> = _quizCompleted

    // LiveData для сообщений об ошибках
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // Текущие данные для логики викторины
    private var currentQuizId: Long = 0
    private var currentQuestionIndex: Int = 0
    private var totalQuestions: Int = 0
    private var userScore: Int = 0
    private var questions: List<QuestionWithAnswers> = emptyList()

    /**
     * Загружает викторину и начинает ее
     */
    fun loadQuiz(quizId: Long) {
        _isLoading.value = true
        currentQuizId = quizId
        
        viewModelScope.launch {
            try {
                // Получаем данные викторины
                val quizDetails = quizRepository.getQuizWithDetails(quizId)
                
                if (quizDetails != null) {
                    _quizWithDetails.postValue(quizDetails)
                    
                    // Инициализируем вопросы
                    questions = quizDetails.questions
                    totalQuestions = questions.size
                    
                    if (questions.isNotEmpty()) {
                        // Начинаем с первого вопроса
                        currentQuestionIndex = 0
                        userScore = 0
                        showCurrentQuestion()
                    } else {
                        _errorMessage.postValue("В этой викторине нет вопросов")
                    }
                } else {
                    _errorMessage.postValue("Викторина не найдена")
                }
                
                _isLoading.postValue(false)
            } catch (e: Exception) {
                _errorMessage.postValue("Ошибка при загрузке викторины: ${e.message}")
                _isLoading.postValue(false)
            }
        }
    }

    /**
     * Отображает текущий вопрос
     */
    private fun showCurrentQuestion() {
        if (currentQuestionIndex < questions.size) {
            // Обновляем прогресс
            val progress = ((currentQuestionIndex + 1) * 100) / totalQuestions
            _questionProgress.postValue(progress)
            
            // Обновляем номер вопроса
            _questionNumber.postValue(Pair(currentQuestionIndex + 1, totalQuestions))
            
            // Отображаем вопрос
            _currentQuestion.postValue(questions[currentQuestionIndex])
        } else {
            // Если вопросы закончились, завершаем викторину
            completeQuiz()
        }
    }

    /**
     * Обрабатывает ответ пользователя
     */
    fun submitAnswer(answerId: Long) {
        val currentQuestion = questions.getOrNull(currentQuestionIndex)
        
        currentQuestion?.let { question ->
            // Ищем выбранный ответ
            val selectedAnswer = question.answers.find { it.id == answerId }
            
            // Если ответ правильный, увеличиваем счет
            if (selectedAnswer?.isCorrect == true) {
                userScore++
            }
            
            // Переходим к следующему вопросу
            currentQuestionIndex++
            showCurrentQuestion()
        }
    }

    /**
     * Обрабатывает ситуацию, когда время вышло
     */
    fun timeUp() {
        // Переходим к следующему вопросу без начисления очков
        currentQuestionIndex++
        showCurrentQuestion()
    }

    /**
     * Завершает викторину и сохраняет результат
     */
    private fun completeQuiz() {
        viewModelScope.launch {
            try {
                // Сохраняем результат пользователя
                userQuizResultRepository.saveQuizResult(
                    currentUserId,
                    currentQuizId,
                    userScore
                )
                
                // Отображаем результат
                _quizCompleted.postValue(Pair(userScore, totalQuestions))
            } catch (e: Exception) {
                _errorMessage.postValue("Ошибка при сохранении результата: ${e.message}")
            }
        }
    }
} 