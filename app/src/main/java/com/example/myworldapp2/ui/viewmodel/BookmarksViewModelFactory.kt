package com.example.myworldapp2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myworldapp2.data.repository.BookmarkRepository
import com.example.myworldapp2.data.repository.CategoryRepository
import com.example.myworldapp2.data.repository.EntryRepository

/**
 * Factory для создания BookmarksViewModel с необходимыми зависимостями
 */
class BookmarksViewModelFactory(
    private val bookmarkRepository: BookmarkRepository,
    private val entryRepository: EntryRepository,
    private val categoryRepository: CategoryRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookmarksViewModel::class.java)) {
            return BookmarksViewModel(
                bookmarkRepository,
                entryRepository,
                categoryRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 