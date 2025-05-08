package com.example.myworldapp2.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myworldapp2.data.entity.Category
import com.example.myworldapp2.data.entity.Entry
import com.example.myworldapp2.data.entity.Tag
import com.example.myworldapp2.data.repository.CategoryRepository
import com.example.myworldapp2.data.repository.EntryRepository
import com.example.myworldapp2.data.repository.TagRepository
import kotlinx.coroutines.launch

class EntryEditorViewModel(
    private val entryRepository: EntryRepository,
    private val categoryRepository: CategoryRepository,
    private val tagRepository: TagRepository
) : ViewModel() {

    // LiveData для статьи
    private val _entry = MutableLiveData<Entry>()
    val entry: LiveData<Entry> = _entry

    // LiveData для списка категорий
    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    // LiveData для списка всех тегов
    private val _allTags = MutableLiveData<List<Tag>>()
    val allTags: LiveData<List<Tag>> = _allTags
    
    // LiveData для списка тегов текущей статьи
    private val _entryTags = MutableLiveData<List<Tag>>()
    val entryTags: LiveData<List<Tag>> = _entryTags
    
    // LiveData для выбранных тегов (для сохранения)
    private val _selectedTagIds = MutableLiveData<MutableSet<Long>>(mutableSetOf())
    val selectedTagIds: LiveData<MutableSet<Long>> = _selectedTagIds

    // LiveData для состояния загрузки
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData для успешного сохранения
    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    // LiveData для сообщений об ошибках
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    /**
     * Загружает статью по ID
     */
    fun loadEntry(entryId: Long) {
        if (entryId <= 0) return

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val entry = entryRepository.getEntryByIdSync(entryId)
                _entry.value = entry
                
                // Загружаем теги для статьи
                loadTagsForEntry(entryId)
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при загрузке статьи: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Загружает список категорий
     */
    fun loadCategories() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                categoryRepository.getAllCategories().observeForever { categories ->
                    _categories.value = categories
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при загрузке категорий: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Загружает все теги
     */
    fun loadAllTags() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                tagRepository.allTags.observeForever { tags ->
                    _allTags.value = tags
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при загрузке тегов: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Загружает теги для конкретной статьи
     */
    fun loadTagsForEntry(entryId: Long) {
        if (entryId <= 0) return
        
        viewModelScope.launch {
            try {
                tagRepository.getTagsForEntry(entryId).observeForever { tags ->
                    _entryTags.value = tags
                    
                    // Обновляем список выбранных ID тегов
                    val tagIds = tags.map { it.id }.toMutableSet()
                    _selectedTagIds.value = tagIds
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при загрузке тегов статьи: ${e.message}"
            }
        }
    }
    
    /**
     * Добавляет тег в выбранные
     */
    fun addTagToSelection(tagId: Long) {
        val currentSelection = _selectedTagIds.value ?: mutableSetOf()
        currentSelection.add(tagId)
        _selectedTagIds.value = currentSelection
    }
    
    /**
     * Удаляет тег из выбранных
     */
    fun removeTagFromSelection(tagId: Long) {
        val currentSelection = _selectedTagIds.value ?: mutableSetOf()
        currentSelection.remove(tagId)
        _selectedTagIds.value = currentSelection
    }
    
    /**
     * Создает новый тег и добавляет его в выбранные
     */
    fun createAndSelectTag(tagName: String, tagColor: String = "#4CAF50") {
        if (tagName.isBlank()) return
        
        viewModelScope.launch {
            try {
                // Создаем новый тег с указанным цветом
                val tag = Tag(name = tagName, color = tagColor)
                // Добавляем тег через репозиторий
                val tagId = tagRepository.addTagWithColor(tagName, tagColor)
                
                // Добавляем в выбранные
                addTagToSelection(tagId)
                
                // Обновляем список всех тегов
                loadAllTags()
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при создании тега: ${e.message}"
            }
        }
    }

    /**
     * Сохраняет статью (создает новую или обновляет существующую)
     */
    fun saveEntry(entry: Entry) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val entryId = if (entry.id > 0) {
                    // Обновляем существующую статью
                    entryRepository.updateEntry(entry)
                    entry.id
                } else {
                    // Создаем новую статью
                    entryRepository.insertEntry(entry)
                }
                
                // Сохраняем теги статьи
                val selectedIds = _selectedTagIds.value ?: mutableSetOf()
                
                // Сначала удаляем все существующие теги
                tagRepository.removeAllTagsFromEntry(entryId)
                
                // Затем добавляем выбранные теги
                for (tagId in selectedIds) {
                    tagRepository.addTagToEntry(entryId, tagId)
                }
                
                _saveSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при сохранении статьи: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Возвращает ID категории по позиции в списке
     */
    fun getCategoryIdAtPosition(position: Int): Long {
        return if (position >= 0 && position < (_categories.value?.size ?: 0)) {
            _categories.value!![position].id
        } else {
            0 // Если категория не выбрана или произошла ошибка
        }
    }
    
    /**
     * Проверяет, выбран ли тег
     */
    fun isTagSelected(tagId: Long): Boolean {
        return _selectedTagIds.value?.contains(tagId) == true
    }
}

class EntryEditorViewModelFactory(
    private val entryRepository: EntryRepository,
    private val categoryRepository: CategoryRepository,
    private val tagRepository: TagRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EntryEditorViewModel::class.java)) {
            return EntryEditorViewModel(entryRepository, categoryRepository, tagRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 