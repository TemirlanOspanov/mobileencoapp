package com.example.myworldapp2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myworldapp2.data.repository.QuizRepository
import com.example.myworldapp2.data.repository.UserQuizResultRepository

/**
 * Factory для создания QuizPlayViewModel с необходимыми зависимостями
 */
class QuizPlayViewModelFactory(
    private val quizRepository: QuizRepository,
    private val userQuizResultRepository: UserQuizResultRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuizPlayViewModel::class.java)) {
            return QuizPlayViewModel(
                quizRepository,
                userQuizResultRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 