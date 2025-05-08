package com.example.myworldapp2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myworldapp2.data.repository.EntryRepository
import com.example.myworldapp2.data.repository.QuizRepository

/**
 * Фабрика для создания QuizzesViewModel
 */
class QuizzesViewModelFactory(
    private val quizRepository: QuizRepository,
    private val entryRepository: EntryRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuizzesViewModel::class.java)) {
            return QuizzesViewModel(quizRepository, entryRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 