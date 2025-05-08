package com.example.myworldapp2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myworldapp2.data.repository.CategoryRepository
import com.example.myworldapp2.data.repository.EntryRepository
import com.example.myworldapp2.data.repository.UserRepository

/**
 * Factory для создания CategoryDetailViewModel с необходимыми зависимостями
 */
class CategoryDetailViewModelFactory(
    private val categoryRepository: CategoryRepository,
    private val entryRepository: EntryRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {

    private var categoryId: Long = 0

    fun setCategoryId(id: Long) {
        categoryId = id
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryDetailViewModel::class.java)) {
            return CategoryDetailViewModel(
                categoryId,
                categoryRepository,
                entryRepository,
                userRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 