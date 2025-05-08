package com.example.myworldapp2.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myworldapp2.data.entity.Category
import com.example.myworldapp2.data.repository.CategoryRepository
import com.example.myworldapp2.data.repository.EntryRepository
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана категорий
 */
class CategoriesViewModel(
    private val categoryRepository: CategoryRepository,
    private val entryRepository: EntryRepository
) : ViewModel() {

    // LiveData для отображения категорий
    val categories: LiveData<List<Category>> = categoryRepository.getAllCategories()
    
    // LiveData для отображения количества статей в категориях
    private val _entryCounts = MutableLiveData<Map<Long, Int>>()
    val entryCounts: LiveData<Map<Long, Int>> = _entryCounts
    
    // LiveData для состояния загрузки
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        // Загружаем количество статей для категорий
        loadEntryCounts()
    }

    /**
     * Загружает количество статей для каждой категории
     */
    fun loadEntryCounts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val counts = categoryRepository.getCategoriesWithEntriesCount()
                _entryCounts.value = counts
            } catch (e: Exception) {
                // Обработка ошибок
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Загружает список категорий
     */
    fun loadCategories() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Список категорий обновится автоматически через LiveData
            } catch (e: Exception) {
                // Обработка ошибок
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Добавляет новую категорию
     */
    suspend fun addCategory(name: String, description: String, color: String, icon: String): Long {
        return categoryRepository.insertCategory(
            Category(
                name = name,
                description = description,
                color = color,
                icon = icon
            )
        )
    }

    /**
     * Обновляет существующую категорию
     */
    suspend fun updateCategory(category: Category) {
        categoryRepository.updateCategory(category)
    }

    /**
     * Удаляет категорию
     */
    suspend fun deleteCategory(category: Category) {
        categoryRepository.deleteCategory(category)
    }

    /**
     * Обновляет данные о категориях
     */
    fun refreshCategories() {
        loadEntryCounts()
    }

    /**
     * Поиск категорий по названию
     */
    fun searchCategories(query: String): LiveData<List<Category>> {
        return categoryRepository.searchCategories(query)
    }
} 