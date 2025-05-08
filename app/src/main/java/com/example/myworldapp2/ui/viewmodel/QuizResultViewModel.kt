package com.example.myworldapp2.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myworldapp2.data.entity.Achievement
import com.example.myworldapp2.data.entity.QuizWithDetails
import com.example.myworldapp2.data.repository.AchievementRepository
import com.example.myworldapp2.data.repository.QuizRepository
import com.example.myworldapp2.data.repository.UserAchievementRepository
import com.example.myworldapp2.data.repository.UserQuizResultRepository
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана результатов викторины
 */
class QuizResultViewModel(
    private val quizRepository: QuizRepository,
    private val userQuizResultRepository: UserQuizResultRepository,
    private val achievementRepository: AchievementRepository,
    private val userAchievementRepository: UserAchievementRepository
) : ViewModel() {

    // ID текущего пользователя (пока заглушка)
    // В реальном приложении будет получаться из AuthRepository или SessionManager
    private val currentUserId: Long = 1L

    // LiveData для данных викторины
    private val _quizWithDetails = MutableLiveData<QuizWithDetails>()
    val quizWithDetails: LiveData<QuizWithDetails> = _quizWithDetails

    // LiveData для разблокированных достижений
    private val _unlockedAchievements = MutableLiveData<List<Achievement>>()
    val unlockedAchievements: LiveData<List<Achievement>> = _unlockedAchievements

    // LiveData для сообщений об ошибках
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    /**
     * Загружает данные о викторине и проверяет достижения
     */
    fun loadQuizDetails(quizId: Long, score: Int, totalQuestions: Int) {
        viewModelScope.launch {
            try {
                // 1. Загружаем информацию о викторине
                val quizDetails = quizRepository.getQuizWithDetails(quizId)
                
                if (quizDetails != null) {
                    // Дополнительно можно загрузить связанную статью
                    _quizWithDetails.postValue(quizDetails)
                    
                    // 2. Проверяем, разблокированы ли новые достижения
                    checkAchievements(quizId, score, totalQuestions)
                } else {
                    _errorMessage.postValue("Викторина не найдена")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Ошибка при загрузке данных: ${e.message}")
            }
        }
    }

    /**
     * Проверяет, разблокированы ли новые достижения
     */
    private suspend fun checkAchievements(quizId: Long, score: Int, totalQuestions: Int) {
        try {
            val newAchievements = mutableListOf<Achievement>()
            
            // Получаем количество пройденных викторин
            val completedQuizzesCount = userQuizResultRepository.getCompletedQuizCount(currentUserId)
            
            // Проверяем различные достижения
            
            // 1. Достижение за прохождение первой викторины
            if (completedQuizzesCount == 1) {
                val achievement = achievementRepository.getAchievementByType("first_quiz").firstOrNull()
                achievement?.let {
                    if (userAchievementRepository.unlockAchievement(currentUserId, it.id)) {
                        newAchievements.add(it)
                    }
                }
            }
            
            // 2. Достижение за прохождение 5 викторин
            if (completedQuizzesCount == 5) {
                val achievement = achievementRepository.getAchievementByType("five_quizzes").firstOrNull()
                achievement?.let {
                    if (userAchievementRepository.unlockAchievement(currentUserId, it.id)) {
                        newAchievements.add(it)
                    }
                }
            }
            
            // 3. Достижение за идеальный результат (100%)
            if (score == totalQuestions && totalQuestions > 0) {
                val achievement = achievementRepository.getAchievementByType("perfect_score").firstOrNull()
                achievement?.let {
                    if (userAchievementRepository.unlockAchievement(currentUserId, it.id)) {
                        newAchievements.add(it)
                    }
                }
            }
            
            // Устанавливаем список новых достижений
            _unlockedAchievements.postValue(newAchievements)
            
        } catch (e: Exception) {
            _errorMessage.postValue("Ошибка при проверке достижений: ${e.message}")
        }
    }
} 