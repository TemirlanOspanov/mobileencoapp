package com.example.myworldapp2.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myworldapp2.data.entity.Entry
import com.example.myworldapp2.data.entity.QuizWithDetails
import com.example.myworldapp2.data.repository.EntryRepository
import com.example.myworldapp2.data.repository.QuizRepository
import com.example.myworldapp2.data.repository.UserQuizResultRepository
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана деталей викторины
 */
class QuizDetailViewModel(
    private val quizRepository: QuizRepository,
    private val userQuizResultRepository: UserQuizResultRepository,
    private val entryRepository: EntryRepository
) : ViewModel() {

    // ID текущего пользователя (пока заглушка)
    // В реальном приложении будет получаться из AuthRepository или SessionManager
    private val currentUserId: Long = 1L

    // LiveData для данных викторины с деталями
    private val _quizWithDetails = MutableLiveData<QuizWithDetails>()
    val quizWithDetails: LiveData<QuizWithDetails> = _quizWithDetails

    // LiveData для состояния загрузки
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData для сообщений об ошибках
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // Текущие данные викторины для доступа из фрагмента
    private var currentQuizDetails: QuizWithDetails? = null

    /**
     * Загружает все данные о викторине
     */
    fun loadQuizDetails(quizId: Long) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // 1. Получаем базовую информацию о викторине
                val quizDetails = quizRepository.getQuizWithDetails(quizId)
                
                if (quizDetails != null) {
                    // 2. Загружаем связанную статью
                    val entry = entryRepository.getEntryById(quizDetails.quiz.entryId).value
                    
                    // 3. Загружаем результаты пользователя
                    val userResult = userQuizResultRepository.getUserQuizResult(currentUserId, quizId)
                    
                    // 4. Объединяем всю информацию
                    val fullQuizDetails = quizDetails.copy(
                        entry = entry,
                        userResult = userResult
                    )
                    
                    _quizWithDetails.postValue(fullQuizDetails)
                    currentQuizDetails = fullQuizDetails
                } else {
                    _errorMessage.postValue("Викторина не найдена")
                }
                
                _isLoading.postValue(false)
            } catch (e: Exception) {
                _errorMessage.postValue("Ошибка при загрузке данных: ${e.message}")
                _isLoading.postValue(false)
            }
        }
    }

    /**
     * Возвращает текущие данные викторины
     */
    fun getQuizDetails(): QuizWithDetails? {
        return currentQuizDetails
    }

    /**
     * Получает информацию о статье
     */
    suspend fun getEntryById(entryId: Long): Entry? {
        return entryRepository.getEntryById(entryId).value
    }
} 