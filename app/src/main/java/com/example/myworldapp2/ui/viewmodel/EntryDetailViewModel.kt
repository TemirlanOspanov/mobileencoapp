package com.example.myworldapp2.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myworldapp2.data.entity.Category
import com.example.myworldapp2.data.entity.Comment
import com.example.myworldapp2.data.entity.Entry
import com.example.myworldapp2.data.entity.Tag
import com.example.myworldapp2.data.entity.User
import com.example.myworldapp2.data.repository.BookmarkRepository
import com.example.myworldapp2.data.repository.CategoryRepository
import com.example.myworldapp2.data.repository.CommentRepository
import com.example.myworldapp2.data.repository.EntryRepository
import com.example.myworldapp2.data.repository.LikeRepository
import com.example.myworldapp2.data.repository.TagRepository
import com.example.myworldapp2.data.repository.UserRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.lifecycle.Observer

/**
 * ViewModel для экрана деталей статьи
 */
class EntryDetailViewModel(
    private val entryId: Long,
    private val entryRepository: EntryRepository,
    private val categoryRepository: CategoryRepository,
    private val userRepository: UserRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val tagRepository: TagRepository,
    private val likeRepository: LikeRepository,
    private val commentRepository: CommentRepository
) : ViewModel() {

    // LiveData для статьи
    private val _entry = MutableLiveData<Entry>()
    val entry: LiveData<Entry> = _entry

    // LiveData для категории
    private val _category = MutableLiveData<Category>()
    val category: LiveData<Category> = _category
    
    // LiveData для тегов статьи
    private val _tags = MutableLiveData<List<Tag>>()
    val tags: LiveData<List<Tag>> = _tags

    // LiveData для состояния загрузки
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData для форматированной даты
    private val _formattedDate = MutableLiveData<String>()
    val formattedDate: LiveData<String> = _formattedDate

    // LiveData для статуса закладки
    private val _isBookmarked = MutableLiveData<Boolean>()
    val isBookmarked: LiveData<Boolean> = _isBookmarked
    
    // LiveData для статуса лайка
    private val _isLiked = MutableLiveData<Boolean>()
    val isLiked: LiveData<Boolean> = _isLiked
    
    // LiveData для количества лайков
    private val _likeCount = MutableLiveData<Int>()
    val likeCount: LiveData<Int> = _likeCount
    
    // LiveData для количества комментариев
    private val _commentCount = MutableLiveData<Int>()
    val commentCount: LiveData<Int> = _commentCount
    
    // LiveData для комментариев
    val comments = commentRepository.getCommentsByEntryId(entryId)

    // LiveData для сообщений об ошибках
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // ID текущего пользователя
    private val currentUserId: Long = userRepository.getCurrentUserId()

    init {
        loadEntryData()
    }

    /**
     * Загружает данные статьи
     */
    private fun loadEntryData() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Получаем статью синхронно вместо асинхронно
                val entry = entryRepository.getEntryByIdSync(entryId)
                if (entry != null) {
                    _entry.value = entry
                    
                    // Отмечаем статью как прочитанную
                    userRepository.markEntryAsRead(currentUserId, entryId)
                    
                    // Загружаем категорию синхронно
                    val category = categoryRepository.getCategoryByIdSync(entry.categoryId)
                    _category.value = category
                    
                    // Загружаем теги для статьи
                    loadTags()
                    
                    // Форматируем дату
                    formatDate(entry.updatedAt ?: entry.createdAt)
                    
                    // Проверяем, добавлена ли статья в закладки
                    checkBookmarkStatus()
                    
                    // Проверяем, поставлен ли лайк
                    checkLikeStatus()
                    
                    // Загружаем количество лайков
                    loadLikeCount()
                    
                    // Загружаем количество комментариев
                    loadCommentCount()
                }
            } catch (e: Exception) {
                // Обработка ошибок
                e.printStackTrace() // Добавляем вывод ошибки для отладки
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Загружает теги для статьи
     */
    private fun loadTags() {
        viewModelScope.launch {
            try {
                val tagsLiveData = tagRepository.getTagsForEntry(entryId)
                tagsLiveData.observeForever(object : Observer<List<Tag>> {
                    override fun onChanged(tagsList: List<Tag>) {
                        _tags.value = tagsList
                        // Удаляем наблюдателя после получения данных
                        tagsLiveData.removeObserver(this)
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Форматирует дату для отображения
     */
    private fun formatDate(date: Date?) {
        if (date != null) {
            val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale("ru"))
            _formattedDate.value = dateFormat.format(date)
        } else {
            _formattedDate.value = ""
        }
    }

    /**
     * Проверяет, добавлена ли статья в закладки
     */
    private fun checkBookmarkStatus() {
        viewModelScope.launch {
            val isBookmarked = bookmarkRepository.isBookmarked(currentUserId, entryId)
            _isBookmarked.value = isBookmarked
        }
    }
    
    /**
     * Проверяет, поставлен ли лайк
     */
    private fun checkLikeStatus() {
        viewModelScope.launch {
            try {
                val isLiked = likeRepository.isLikedByUser(entryId, currentUserId)
                _isLiked.value = isLiked
            } catch (e: Exception) {
                e.printStackTrace()
                _isLiked.value = false
            }
        }
    }
    
    /**
     * Загружает количество лайков
     */
    private fun loadLikeCount() {
        viewModelScope.launch {
            try {
                val count = likeRepository.getLikeCountByEntry(entryId)
                _likeCount.value = count
            } catch (e: Exception) {
                e.printStackTrace()
                _likeCount.value = 0
            }
        }
    }
    
    /**
     * Загружает количество комментариев
     */
    private fun loadCommentCount() {
        viewModelScope.launch {
            try {
                val count = commentRepository.getCommentCountForEntry(entryId)
                _commentCount.value = count
            } catch (e: Exception) {
                e.printStackTrace()
                _commentCount.value = 0
            }
        }
    }

    /**
     * Добавляет или удаляет закладку
     */
    fun toggleBookmark() {
        viewModelScope.launch {
            try {
                android.util.Log.d("EntryDetailViewModel", "toggleBookmark: начало, текущее состояние: ${_isBookmarked.value}, userId=$currentUserId, entryId=$entryId")
                
                if (_isBookmarked.value == true) {
                    android.util.Log.d("EntryDetailViewModel", "Удаляем закладку из БД...")
                    bookmarkRepository.removeBookmark(currentUserId, entryId)
                    _isBookmarked.value = false
                    android.util.Log.d("EntryDetailViewModel", "Закладка удалена: userId=$currentUserId, entryId=$entryId")
                } else {
                    android.util.Log.d("EntryDetailViewModel", "Добавляем закладку в БД...")
                    val bookmarkId = bookmarkRepository.addBookmark(currentUserId, entryId)
                    _isBookmarked.value = true
                    android.util.Log.d("EntryDetailViewModel", "Закладка добавлена: userId=$currentUserId, entryId=$entryId, bookmarkId=$bookmarkId")
                }
                
                // Проверим актуальное состояние после операции
                val actualBookmarkState = bookmarkRepository.isBookmarked(currentUserId, entryId)
                android.util.Log.d("EntryDetailViewModel", "Проверка после операции: isBookmarked=$actualBookmarkState")
                
                // Запрашиваем общее количество закладок у пользователя для проверки
                val bookmarkCount = bookmarkRepository.getBookmarkCountByUser(currentUserId)
                android.util.Log.d("EntryDetailViewModel", "Всего закладок у пользователя: $bookmarkCount")
                
                // Принудительно запрашиваем обновление данных из БД для всех живых запросов
                // Room должен автоматически обновить LiveData, но форсируем это для надежности
                kotlinx.coroutines.delay(100) // Небольшая задержка для завершения транзакций
                
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при обновлении закладки: ${e.message}"
                android.util.Log.e("EntryDetailViewModel", "Ошибка при обновлении закладки", e)
            }
        }
    }
    
    /**
     * Добавляет или удаляет лайк
     */
    fun toggleLike() {
        viewModelScope.launch {
            try {
                val newState = likeRepository.toggleLike(entryId, currentUserId)
                _isLiked.value = newState
                loadLikeCount() // обновляем счетчик лайков
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Обновляет данные на экране
     */
    fun refreshData() {
        loadEntryData()
    }

    /**
     * Добавляет новый комментарий от имени текущего пользователя
     */
    fun addComment(content: String) {
        if (content.isBlank()) {
            return
        }
        
        viewModelScope.launch {
            try {
                // Добавляем комментарий в базу данных
                commentRepository.insertComment(currentUserId, entryId, content)
                
                // Обновляем счетчик комментариев
                loadCommentCount()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Возвращает ID текущего пользователя
     */
    fun getCurrentUserId(): Long {
        return currentUserId
    }

    /**
     * Получает пользователя по ID
     */
    suspend fun getUserById(userId: Long): User? {
        return userRepository.getUserById(userId)
    }

    /**
     * Удаляет комментарий (если пользователь имеет на это право)
     */
    fun deleteComment(comment: Comment) {
        // Проверяем, что текущий пользователь является автором комментария
        if (comment.userId != currentUserId) {
            return // У пользователя нет прав на удаление этого комментария
        }
        
        viewModelScope.launch {
            try {
                commentRepository.deleteComment(comment)
                loadCommentCount() // Обновляем счетчик комментариев
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
} 