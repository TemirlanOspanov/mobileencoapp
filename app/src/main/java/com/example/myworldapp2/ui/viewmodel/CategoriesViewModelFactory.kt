package com.example.myworldapp2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myworldapp2.data.repository.CategoryRepository
import com.example.myworldapp2.data.repository.EntryRepository

/**
 * Factory для создания CategoriesViewModel с зависимостями
 */
class CategoriesViewModelFactory(
    private val categoryRepository: CategoryRepository,
    private val entryRepository: EntryRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoriesViewModel::class.java)) {
            return CategoriesViewModel(categoryRepository, entryRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 