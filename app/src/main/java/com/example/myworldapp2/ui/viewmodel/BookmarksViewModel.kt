package com.example.myworldapp2.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.example.myworldapp2.data.entity.Category
import com.example.myworldapp2.data.entity.Entry
import com.example.myworldapp2.data.repository.BookmarkRepository
import com.example.myworldapp2.data.repository.CategoryRepository
import com.example.myworldapp2.data.repository.EntryRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

/**
 * ViewModel для экрана закладок
 */
class BookmarksViewModel(
    private val bookmarkRepository: BookmarkRepository,
    private val entryRepository: EntryRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    // ID текущего пользователя (должен соответствовать ID в EntryDetailViewModel)
    // В реальном приложении будет получаться из AuthRepository или SessionManager
    private val currentUserId: Long = 4L  // Изменено с 1 на 4 для соответствия ID в EntryDetailViewModel

    // LiveData для состояния загрузки - инициализируем в начале
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData для сообщений об ошибках - инициализируем в начале
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    // LiveData для списка закладок (статей)
    private val _bookmarkedEntries = MutableLiveData<List<Entry>>()
    val bookmarkedEntries: LiveData<List<Entry>> = _bookmarkedEntries
    
    // При инициализации запрашиваем данные
    init {
        Log.d("BookmarksViewModel", "Инициализация ViewModel, запрос закладок")
        // Загружаем закладки при создании ViewModel
        refreshBookmarks()
    }

    /**
     * Получает информацию о категории
     */
    suspend fun getCategoryById(categoryId: Long): Category? {
        return categoryRepository.getCategoryById(categoryId).value
    }

    /**
     * Удаляет закладку
     */
    fun removeBookmark(entryId: Long) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                bookmarkRepository.removeBookmark(currentUserId, entryId)
                _isLoading.value = false
                
                // Обновляем список закладок
                refreshBookmarks()
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при удалении закладки: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Проверяет, есть ли статья в закладках
     */
    suspend fun isBookmarked(entryId: Long): Boolean {
        return bookmarkRepository.isBookmarked(currentUserId, entryId)
    }

    /**
     * Обновляет список закладок
     */
    fun refreshBookmarks() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                Log.d("BookmarksViewModel", "Обновление списка закладок для пользователя: $currentUserId")
                
                // 1. Получаем данные напрямую из репозитория
                val bookmarkedEntries = bookmarkRepository.getBookmarkedEntriesWithDetails(currentUserId).value ?: emptyList()
                
                // 2. Получаем количество закладок в базе для проверки
                val bookmarkCount = bookmarkRepository.getBookmarkCountByUser(currentUserId)
                Log.d("BookmarksViewModel", "Количество закладок в БД: $bookmarkCount, получено записей: ${bookmarkedEntries.size}")
                
                // 3. Обновляем LiveData с новыми данными
                _bookmarkedEntries.postValue(bookmarkedEntries)
                
                // 4. Логируем текущее состояние
                Log.d("BookmarksViewModel", "После обновления - установлены новые данные, размер: ${bookmarkedEntries.size}")
                
                // 5. Если количество не совпадает, пробуем еще раз с задержкой
                if (bookmarkedEntries.size != bookmarkCount) {
                    delay(500)
                    // Повторная попытка
                    val updatedEntries = bookmarkRepository.getBookmarkedEntriesWithDetails(currentUserId).value ?: emptyList()
                    Log.d("BookmarksViewModel", "Повторная попытка - получено записей: ${updatedEntries.size}")
                    _bookmarkedEntries.postValue(updatedEntries)
                }
            } catch (e: Exception) {
                Log.e("BookmarksViewModel", "Ошибка при обновлении закладок", e)
                _errorMessage.value = "Ошибка при обновлении закладок: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Принудительно загружает закладки напрямую из репозитория
     * Этот метод обходит все кэши и запрашивает информацию из БД
     */
    suspend fun forceLoadBookmarks() {
        Log.d("BookmarksViewModel", "Принудительная загрузка закладок")
        
        try {
            // 1. Запрашиваем количество закладок у пользователя
            val bookmarkCount = bookmarkRepository.getBookmarkCountByUser(currentUserId)
            Log.d("BookmarksViewModel", "Количество закладок в БД: $bookmarkCount")
            
            // 2. Получаем актуальные данные
            val bookmarkedEntries = bookmarkRepository.getBookmarkedEntriesWithDetails(currentUserId).value ?: emptyList()
            Log.d("BookmarksViewModel", "Загружено закладок: ${bookmarkedEntries.size}")
            
            // 3. Обновляем LiveData с данными
            _bookmarkedEntries.postValue(bookmarkedEntries)
            
            // 4. Небольшая задержка и повторная проверка
            delay(500)
            val updatedEntries = bookmarkRepository.getBookmarkedEntriesWithDetails(currentUserId).value ?: emptyList()
            if (updatedEntries.size != bookmarkedEntries.size) {
                Log.d("BookmarksViewModel", "Обновление после задержки: ${updatedEntries.size} записей")
                _bookmarkedEntries.postValue(updatedEntries)
            }
            
            Log.d("BookmarksViewModel", "Принудительная загрузка закончена")
        } catch (e: Exception) {
            Log.e("BookmarksViewModel", "Ошибка при принудительной загрузке закладок", e)
        }
    }

    /**
     * Очищаем все ресурсы при уничтожении ViewModel
     */
    override fun onCleared() {
        super.onCleared()
        Log.d("BookmarksViewModel", "onCleared: ViewModel очищена")
    }
} 