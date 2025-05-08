package com.example.myworldapp2.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myworldapp2.data.model.SearchResult
import com.example.myworldapp2.data.repository.SearchRepository
import com.example.myworldapp2.data.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана поиска
 */
class SearchViewModel(
    private val searchRepository: SearchRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    // Результаты поиска
    private val _searchResults = MutableLiveData<List<SearchResult>>()
    val searchResults: LiveData<List<SearchResult>> = _searchResults

    // Статус загрузки
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Сообщение об ошибке
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // Текущий поисковый запрос
    private val _currentQuery = MutableLiveData<String>()
    val currentQuery: LiveData<String> = _currentQuery

    // Статус фильтров
    private val _filterByCategory = MutableLiveData<Boolean>(false)
    val filterByCategory: LiveData<Boolean> = _filterByCategory

    private val _filterByTag = MutableLiveData<Boolean>(false)
    val filterByTag: LiveData<Boolean> = _filterByTag

    private val _filterUnread = MutableLiveData<Boolean>(false)
    val filterUnread: LiveData<Boolean> = _filterUnread

    // Job для поисковых запросов (для отмены предыдущего при новом поиске)
    private var searchJob: Job? = null

    /**
     * Выполняет поиск по заданному запросу
     * @param query Поисковый запрос
     */
    fun search(query: String) {
        // Сохраняем текущий запрос
        _currentQuery.value = query
        
        // Отменяем предыдущий поиск, если он выполняется
        searchJob?.cancel()
        
        // Очищаем сообщение об ошибке
        _errorMessage.value = null
        
        // Если запрос пустой, очищаем результаты и выходим
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        
        // Показываем индикатор загрузки
        _isLoading.value = true
        
        // Выполняем поиск
        searchJob = viewModelScope.launch {
            searchRepository.search(
                query = query,
                filterByCategory = _filterByCategory.value ?: false,
                filterByTag = _filterByTag.value ?: false,
                filterUnread = _filterUnread.value ?: false,
                userId = userRepository.currentUser.value?.id
            ).catch { exception ->
                // Обрабатываем ошибки
                _errorMessage.value = exception.message
                _isLoading.value = false
            }.collectLatest { results ->
                // Обновляем результаты
                _searchResults.value = results
                _isLoading.value = false
            }
        }
    }

    /**
     * Выполняет повторный поиск с текущими параметрами
     */
    fun reapplySearch() {
        _currentQuery.value?.let { search(it) }
    }

    /**
     * Устанавливает фильтр по категориям
     * @param enabled Включен ли фильтр
     */
    fun setFilterByCategory(enabled: Boolean) {
        _filterByCategory.value = enabled
        reapplySearch()
    }

    /**
     * Устанавливает фильтр по тегам
     * @param enabled Включен ли фильтр
     */
    fun setFilterByTag(enabled: Boolean) {
        _filterByTag.value = enabled
        reapplySearch()
    }

    /**
     * Устанавливает фильтр непрочитанных статей
     * @param enabled Включен ли фильтр
     */
    fun setFilterUnread(enabled: Boolean) {
        _filterUnread.value = enabled
        reapplySearch()
    }

    /**
     * Очищает результаты поиска
     */
    fun clearResults() {
        _searchResults.value = emptyList()
        _currentQuery.value = ""
        _errorMessage.value = null
    }
} 