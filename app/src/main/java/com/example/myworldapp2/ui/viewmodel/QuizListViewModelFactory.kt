package com.example.myworldapp2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myworldapp2.data.repository.EntryRepository
import com.example.myworldapp2.data.repository.QuizRepository
import com.example.myworldapp2.data.repository.UserQuizResultRepository

/**
 * Factory для создания QuizListViewModel с необходимыми зависимостями
 */
class QuizListViewModelFactory(
    private val quizRepository: QuizRepository,
    private val userQuizResultRepository: UserQuizResultRepository,
    private val entryRepository: EntryRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuizListViewModel::class.java)) {
            return QuizListViewModel(
                quizRepository,
                userQuizResultRepository,
                entryRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 