package com.example.myworldapp2.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myworldapp2.data.entity.Entry
import com.example.myworldapp2.data.entity.Quiz
import com.example.myworldapp2.data.entity.QuizWithDetails
import com.example.myworldapp2.data.repository.EntryRepository
import com.example.myworldapp2.data.repository.QuizRepository
import com.example.myworldapp2.data.repository.UserQuizResultRepository
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана списка викторин
 */
class QuizListViewModel(
    private val quizRepository: QuizRepository,
    private val userQuizResultRepository: UserQuizResultRepository,
    private val entryRepository: EntryRepository
) : ViewModel() {

    // ID текущего пользователя (пока заглушка)
    // В реальном приложении будет получаться из AuthRepository или SessionManager
    private val currentUserId: Long = 1L

    // LiveData для списка викторин
    // Используем distinct() чтобы избежать дублирования
    val quizzes = quizRepository.getAllQuizzesWithDetails()

    // LiveData для состояния загрузки
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData для сообщений об ошибках
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    /**
     * Получение информации о статье, связанной с викториной
     */
    suspend fun getEntryForQuiz(entryId: Long): Entry? {
        return entryRepository.getEntryById(entryId).value
    }

    /**
     * Получение результатов прохождения викторины пользователем
     */
    suspend fun getUserQuizResult(quizId: Long) = 
        userQuizResultRepository.getUserQuizResult(currentUserId, quizId)

    /**
     * Обновление списка викторин
     */
    fun refreshQuizzes() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Для Room это не требуется, так как используется LiveData
                // Но может потребоваться при интеграции с API
                
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при загрузке викторин: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Получение детальной информации о викторине
     */
    suspend fun getQuizWithDetails(quizId: Long): QuizWithDetails? {
        return quizRepository.getQuizWithDetails(quizId)
    }
} 