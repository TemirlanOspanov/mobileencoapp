package com.example.myworldapp2.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myworldapp2.data.entity.Entry
import com.example.myworldapp2.data.repository.EntryRepository
import kotlinx.coroutines.launch

// Placeholder ViewModel
class EntryListViewModel(private val entryRepository: EntryRepository) : ViewModel() {
    
    // LiveData для списка статей
    private val _entries = MutableLiveData<List<Entry>>()
    val entries: LiveData<List<Entry>> = _entries
    
    // LiveData для состояния загрузки
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    // LiveData для сообщений об ошибках
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    init {
        loadEntries()
    }
    
    /**
     * Загружает список всех статей
     */
    fun loadEntries() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Получаем все статьи из репозитория
                entryRepository.allEntries.observeForever { entries ->
                    _entries.value = entries
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при загрузке статей: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Удаляет статью
     */
    fun deleteEntry(entry: Entry) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                entryRepository.deleteEntry(entry)
                loadEntries() // Перезагружаем список статей
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при удалении статьи: ${e.message}"
                _isLoading.value = false
            }
        }
    }
} 