package com.example.myworldapp2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myworldapp2.data.repository.AchievementRepository
import com.example.myworldapp2.data.repository.QuizRepository
import com.example.myworldapp2.data.repository.UserAchievementRepository
import com.example.myworldapp2.data.repository.UserQuizResultRepository

/**
 * Factory для создания QuizResultViewModel с необходимыми зависимостями
 */
class QuizResultViewModelFactory(
    private val quizRepository: QuizRepository,
    private val userQuizResultRepository: UserQuizResultRepository,
    private val achievementRepository: AchievementRepository,
    private val userAchievementRepository: UserAchievementRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuizResultViewModel::class.java)) {
            return QuizResultViewModel(
                quizRepository,
                userQuizResultRepository,
                achievementRepository,
                userAchievementRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 