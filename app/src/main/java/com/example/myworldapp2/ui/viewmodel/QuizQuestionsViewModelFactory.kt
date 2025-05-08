package com.example.myworldapp2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myworldapp2.data.repository.QuizRepository

/**
 * Фабрика для создания ViewModel экрана управления вопросами викторины
 */
class QuizQuestionsViewModelFactory(
    private val quizRepository: QuizRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuizQuestionsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuizQuestionsViewModel(quizRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 