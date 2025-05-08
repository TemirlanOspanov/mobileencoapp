package com.example.myworldapp2.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myworldapp2.data.entity.Category
import com.example.myworldapp2.data.entity.Entry
import com.example.myworldapp2.data.repository.CategoryRepository
import com.example.myworldapp2.data.repository.EntryRepository
import com.example.myworldapp2.data.repository.UserRepository
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана деталей категории
 */
class CategoryDetailViewModel(
    private val categoryId: Long,
    private val categoryRepository: CategoryRepository,
    private val entryRepository: EntryRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    // LiveData для категории
    private val _category = MutableLiveData<Category>()
    val category: LiveData<Category> = _category

    // LiveData для статей в категории
    val entries: LiveData<List<Entry>> = entryRepository.getEntriesByCategory(categoryId)

    // LiveData для состояния загрузки
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData для прогресса по категории
    private val _categoryProgress = MutableLiveData<Float>()
    val categoryProgress: LiveData<Float> = _categoryProgress

    // ID текущего пользователя
    private val currentUserId: Long = userRepository.getCurrentUserId()

    init {
        loadCategory()
        loadCategoryProgress()
    }

    /**
     * Загружает информацию о категории
     */
    private fun loadCategory() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Получаем категорию из хранилища
                val category = categoryRepository.getCategoryById(categoryId).value
                _category.value = category
            } catch (e: Exception) {
                // Обработка ошибок
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Загружает прогресс по категории
     */
    private fun loadCategoryProgress() {
        viewModelScope.launch {
            try {
                val progress = userRepository.getUserCategoryProgress(currentUserId, categoryId)
                _categoryProgress.value = progress
            } catch (e: Exception) {
                // В случае ошибки устанавливаем 0
                _categoryProgress.value = 0f
            }
        }
    }

    /**
     * Отмечает статью как прочитанную
     */
    fun markEntryAsRead(entryId: Long) {
        viewModelScope.launch {
            try {
                userRepository.markEntryAsRead(currentUserId, entryId)
                // Обновляем прогресс по категории
                loadCategoryProgress()
            } catch (e: Exception) {
                // Обработка ошибок
            }
        }
    }

    /**
     * Возвращает статус прочтения статьи
     */
    suspend fun isEntryRead(entryId: Long): Boolean {
        return userRepository.isEntryRead(currentUserId, entryId)
    }

    /**
     * Обновляет данные на экране
     */
    fun refreshData() {
        loadCategory()
        loadCategoryProgress()
    }
} 