package com.example.myworldapp2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myworldapp2.data.repository.SearchRepository
import com.example.myworldapp2.data.repository.UserRepository

/**
 * Фабрика для создания SearchViewModel
 */
class SearchViewModelFactory(
    private val searchRepository: SearchRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            return SearchViewModel(searchRepository, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 