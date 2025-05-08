package com.example.myworldapp2.ui.viewmodel

import androidx.lifecycle.*
import com.example.myworldapp2.data.entity.Category
import com.example.myworldapp2.data.repository.CategoryRepository
import kotlinx.coroutines.launch

class CategoryEditorViewModel(private val categoryRepository: CategoryRepository) : ViewModel() {

    private val _category = MutableLiveData<Category?>()
    val category: LiveData<Category?> = _category

    private val _saveResult = MutableLiveData<Result<Long>>()
    val saveResult: LiveData<Result<Long>> = _saveResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadCategory(categoryId: Long) {
        if (categoryId == 0L) {
            _category.value = null // New category
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _category.value = categoryRepository.getCategoryByIdSync(categoryId)
            } catch (e: Exception) {
                _category.value = null // Handle error, e.g., category not found
                 // Optionally, post an error message to another LiveData
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveCategory(id: Long, name: String, description: String, color: String, icon: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val categoryToSave = Category(
                    id = if (id == 0L) 0 else id, // Let Room auto-generate if id is 0
                    name = name,
                    description = description,
                    color = color,
                    icon = icon
                )
                val resultId = if (id == 0L) {
                    categoryRepository.insertCategory(categoryToSave)
                } else {
                    categoryRepository.updateCategory(categoryToSave)
                    id // Return existing id for update
                }
                _saveResult.value = Result.success(resultId)
            } catch (e: Exception) {
                _saveResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}

class CategoryEditorViewModelFactory(private val categoryRepository: CategoryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryEditorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoryEditorViewModel(categoryRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 