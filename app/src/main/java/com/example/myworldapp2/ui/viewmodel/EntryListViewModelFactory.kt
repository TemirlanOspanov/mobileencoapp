package com.example.myworldapp2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myworldapp2.data.repository.EntryRepository

// Placeholder ViewModelFactory
class EntryListViewModelFactory(
    private val entryRepository: EntryRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EntryListViewModel::class.java)) {
            return EntryListViewModel(entryRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 