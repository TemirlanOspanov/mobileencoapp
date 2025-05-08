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
 * ViewModel для управления викторинами в админ-панели
 */
class QuizzesViewModel(
    private val quizRepository: QuizRepository,
    private val entryRepository: EntryRepository
) : ViewModel() {

    // LiveData для списка викторин
    private val _quizzes = MutableLiveData<List<Quiz>>()
    val quizzes: LiveData<List<Quiz>> = _quizzes

    // LiveData для состояния загрузки
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData для количества вопросов в викторинах
    private val _questionCounts = MutableLiveData<Map<Long, Int>>()
    val questionCounts: LiveData<Map<Long, Int>> = _questionCounts

    // Загрузка всех викторин
    fun loadQuizzes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val quizList = withContext(Dispatchers.IO) {
                    quizRepository.getAllQuizzesSync()
                }
                _quizzes.value = quizList
            } catch (e: Exception) {
                _quizzes.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Загрузка количества вопросов для каждой викторины
    fun loadQuestionCounts() {
        viewModelScope.launch {
            try {
                val counts = mutableMapOf<Long, Int>()
                val quizList = _quizzes.value ?: return@launch

                withContext(Dispatchers.IO) {
                    for (quiz in quizList) {
                        // Получаем количество вопросов для каждой викторины
                        val questionCount = quizRepository.getQuestionCountByQuiz(quiz.id)
                        counts[quiz.id] = questionCount
                    }
                }
                _questionCounts.value = counts
            } catch (e: Exception) {
                _questionCounts.value = emptyMap()
            }
        }
    }

    // Получение списка статей для выбора при создании/редактировании викторины
    suspend fun getEntriesForSelection(): List<Entry> {
        return withContext(Dispatchers.IO) {
            entryRepository.getAllEntriesSync()
        }
    }

    // Получение викторины по ID
    suspend fun getQuizById(quizId: Long): Quiz? {
        return withContext(Dispatchers.IO) {
            quizRepository.getQuizByIdSync(quizId)
        }
    }

    // Получение статьи по ID
    suspend fun getEntryById(entryId: Long): Entry? {
        return withContext(Dispatchers.IO) {
            entryRepository.getEntryByIdSync(entryId)
        }
    }

    // Создание или обновление викторины
    suspend fun saveQuiz(quiz: Quiz): Long {
        return withContext(Dispatchers.IO) {
            if (quiz.id == 0L) {
                // Создание новой викторины
                quizRepository.addQuiz(quiz)
            } else {
                // Обновление существующей викторины
                quizRepository.updateQuiz(quiz)
                quiz.id
            }
        }
    }

    // Удаление викторины
    suspend fun deleteQuiz(quiz: Quiz) {
        withContext(Dispatchers.IO) {
            quizRepository.deleteQuiz(quiz)
        }
    }
} 