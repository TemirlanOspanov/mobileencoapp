package com.example.myworldapp2.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myworldapp2.data.entity.Comment
import com.example.myworldapp2.data.entity.User
import com.example.myworldapp2.data.repository.CommentRepository
import com.example.myworldapp2.data.repository.UserRepository
import kotlinx.coroutines.launch
import java.util.Date

/**
 * ViewModel для экрана комментариев
 */
class CommentsViewModel(
    private val entryId: Long,
    private val commentRepository: CommentRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    // LiveData для списка комментариев
    val comments = commentRepository.getCommentsByEntryId(entryId)

    // LiveData для состояния загрузки
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData для сообщений об ошибках
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // ID текущего пользователя (пока заглушка)
    // В реальном приложении будет получаться из AuthRepository или SessionManager
    private val currentUserId: Long = 1L

    /**
     * Загружает пользователя по ID
     */
    suspend fun getUserById(userId: Long): User? {
        return userRepository.getUserById(userId)
    }

    /**
     * Добавляет новый комментарий
     */
    fun addComment(content: String) {
        if (content.isBlank()) {
            _errorMessage.value = "Комментарий не может быть пустым"
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                commentRepository.insertComment(currentUserId, entryId, content)
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при добавлении комментария: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Удаляет комментарий (доступно только автору комментария или администратору)
     */
    fun deleteComment(comment: Comment) {
        if (comment.userId != currentUserId) {
            _errorMessage.value = "У вас нет прав для удаления этого комментария"
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                commentRepository.deleteComment(comment)
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при удалении комментария: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Обновляет список комментариев
     */
    fun refreshComments() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Для Room это не требуется, так как используется LiveData,
                // но может потребоваться при интеграции с API
                // commentRepository.refreshComments(entryId)
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при обновлении комментариев: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
} 