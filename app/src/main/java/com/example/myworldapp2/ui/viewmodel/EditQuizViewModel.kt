package com.example.myworldapp2.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myworldapp2.data.entity.Entry
import com.example.myworldapp2.data.entity.Quiz
import com.example.myworldapp2.data.repository.EntryRepository
import com.example.myworldapp2.data.repository.QuizRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel для экрана создания/редактирования викторины
 */
class EditQuizViewModel(
    private val quizRepository: QuizRepository,
    private val entryRepository: EntryRepository
) : ViewModel() {

    // LiveData для отслеживания статуса сохранения
    private val _saveStatus = MutableLiveData<Boolean>()
    val saveStatus: LiveData<Boolean> = _saveStatus

    // LiveData для отображения ошибок
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    // LiveData для ID сохраненной викторины
    private val _savedQuizId = MutableLiveData<Long>()
    val savedQuizId: LiveData<Long> = _savedQuizId

    // Получение викторины по ID
    suspend fun getQuizById(quizId: Long): Quiz? {
        return withContext(Dispatchers.IO) {
            quizRepository.getQuizByIdSync(quizId)
        }
    }

    // Загрузка всех статей для выбора
    suspend fun loadEntries(): List<Entry> {
        return withContext(Dispatchers.IO) {
            entryRepository.getAllEntriesSync()
        }
    }

    // Сохранение викторины (создание новой или обновление существующей)
    fun saveQuiz(quiz: Quiz) {
        viewModelScope.launch {
            try {
                val quizId = withContext(Dispatchers.IO) {
                    if (quiz.id == 0L) {
                        // Создание новой викторины
                        quizRepository.addQuiz(quiz)
                    } else {
                        // Обновление существующей викторины
                        quizRepository.updateQuiz(quiz)
                        quiz.id
                    }
                }
                _savedQuizId.value = quizId
                _saveStatus.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Ошибка сохранения викторины"
                _saveStatus.value = false
            }
        }
    }
} 