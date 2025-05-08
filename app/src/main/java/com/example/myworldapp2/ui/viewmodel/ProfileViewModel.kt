package com.example.myworldapp2.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myworldapp2.data.entity.User
import com.example.myworldapp2.data.model.CategoryProgress
import com.example.myworldapp2.data.model.UserActivity
import com.example.myworldapp2.data.model.UserStats
import com.example.myworldapp2.data.repository.UserRepository
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана профиля пользователя
 */
class ProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    // ID текущего пользователя
    private val currentUserId: Long
        get() = userRepository.getCurrentUserId()

    // Данные пользователя
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    // Статистика пользователя
    private val _userStats = MutableLiveData<UserStats?>()
    val userStats: LiveData<UserStats?> = _userStats

    // Прогресс по категориям
    private val _categoryProgress = MutableLiveData<List<CategoryProgress>?>()
    val categoryProgress: LiveData<List<CategoryProgress>?> = _categoryProgress

    // Последние активности
    private val _recentActivities = MutableLiveData<List<UserActivity>?>()
    val recentActivities: LiveData<List<UserActivity>?> = _recentActivities

    // Состояние загрузки
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Сообщения об ошибках
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // Флаг успешного обновления профиля
    private val _profileUpdateSuccess = MutableLiveData<Boolean>()
    val profileUpdateSuccess: LiveData<Boolean> = _profileUpdateSuccess

    // Флаг успешной смены пароля
    private val _passwordChangeSuccess = MutableLiveData<Boolean>()
    val passwordChangeSuccess: LiveData<Boolean> = _passwordChangeSuccess

    init {
        loadUserData()
    }

    /**
     * Загружает все данные пользователя
     */
    fun loadUserData() {
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                // Загружаем данные пользователя
                _user.value = userRepository.getUserById(currentUserId)
                
                // Загружаем статистику
                _userStats.value = userRepository.getUserStats(currentUserId)
                
                // Загружаем прогресс по категориям
                _categoryProgress.value = userRepository.getCategoryProgressForUser(currentUserId)
                
                // Загружаем последние активности
                _recentActivities.value = userRepository.getRecentActivities(currentUserId)
                
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при загрузке данных: ${e.message}"
                _user.value = null
                _userStats.value = null
                _categoryProgress.value = null
                _recentActivities.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Вычисляет общий процент прогресса пользователя
     */
    fun calculateOverallProgress(): Int {
        val stats = userStats.value ?: return 0
        
        // Учитываем прочитанные статьи, пройденные викторины и полученные достижения
        val totalItems = stats.totalEntries + stats.totalQuizzes + stats.totalAchievements
        val completedItems = stats.readEntries + stats.completedQuizzes + stats.earnedAchievements
        
        return if (totalItems > 0) {
            (completedItems * 100) / totalItems
        } else {
            0
        }
    }

    /**
     * Обновляет профиль пользователя с новыми данными
     */
    fun updateUserProfile(name: String, email: String, avatarUri: Uri? = null) {
        _isLoading.value = true
        _profileUpdateSuccess.value = false
        
        viewModelScope.launch {
            try {
                val currentUser = _user.value
                if (currentUser != null) {
                    // Проверяем, не занят ли email другим пользователем
                    if (email != currentUser.email && userRepository.isEmailTaken(email)) {
                        _errorMessage.value = "Этот email уже используется другим пользователем"
                        _isLoading.value = false
                        return@launch
                    }
                    
                    // Обновляем данные пользователя
                    val updatedUser = currentUser.copy(
                        name = name,
                        email = email
                        // В реальном приложении здесь также обновляем avatarUrl
                        // avatarUrl = uploadAvatarAndGetUrl(avatarUri)
                    )
                    
                    userRepository.updateUser(updatedUser)
                    _user.value = updatedUser
                    _profileUpdateSuccess.value = true
                } else {
                    _errorMessage.value = "Не удалось получить данные текущего пользователя"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при обновлении профиля: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Изменяет пароль пользователя
     */
    fun changePassword(currentPassword: String, newPassword: String) {
        _isLoading.value = true
        _passwordChangeSuccess.value = false
        
        viewModelScope.launch {
            try {
                val currentUser = _user.value
                if (currentUser != null) {
                    // Вызываем метод репозитория для обновления пароля
                    val success = userRepository.updatePassword(currentUser.id, currentPassword, newPassword)
                    
                    if (success) {
                        _passwordChangeSuccess.value = true
                    } else {
                        _errorMessage.value = "Не удалось изменить пароль. Проверьте текущий пароль."
                    }
                } else {
                     _errorMessage.value = "Не удалось получить данные текущего пользователя"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при смене пароля: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Выход из системы
     */
    fun logout() {
        // TODO: Реализовать выход из системы
        // В реальном приложении здесь будет код для очистки сессии пользователя
        userRepository.logout()
    }
}