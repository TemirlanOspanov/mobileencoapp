package com.example.myworldapp2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myworldapp2.data.repository.AchievementRepository
import com.example.myworldapp2.data.repository.CategoryRepository
import com.example.myworldapp2.data.repository.EntryRepository
import com.example.myworldapp2.data.repository.UserAchievementRepository
import com.example.myworldapp2.data.repository.UserRepository

/**
 * Factory для создания HomeViewModel с зависимостями
 */
class HomeViewModelFactory(
    private val categoryRepository: CategoryRepository,
    private val entryRepository: EntryRepository,
    private val userRepository: UserRepository,
    private val achievementRepository: AchievementRepository,
    private val userAchievementRepository: UserAchievementRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(
                categoryRepository,
                entryRepository,
                userRepository,
                achievementRepository,
                userAchievementRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 