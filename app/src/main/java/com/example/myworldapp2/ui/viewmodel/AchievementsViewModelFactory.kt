package com.example.myworldapp2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myworldapp2.data.repository.AchievementRepository

/**
 * Фабрика для создания AchievementsViewModel с внедрением зависимостей
 */
class AchievementsViewModelFactory(
    private val repository: AchievementRepository
) : ViewModelProvider.Factory {

    // Пользователь, для которого загружаются достижения
    private var userId: Long = 4L  // По умолчанию используем userId = 4, как в других частях приложения
    
    fun setUserId(id: Long): AchievementsViewModelFactory {
        userId = id
        return this
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AchievementsViewModel::class.java)) {
            return AchievementsViewModel(repository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 