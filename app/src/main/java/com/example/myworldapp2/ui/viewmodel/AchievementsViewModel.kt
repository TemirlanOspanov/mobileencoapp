package com.example.myworldapp2.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myworldapp2.data.model.AchievementWithProgress
import com.example.myworldapp2.data.repository.AchievementRepository
import com.example.myworldapp2.data.repository.AchievementStats
import kotlinx.coroutines.launch
import android.util.Log

/**
 * ViewModel для экрана достижений
 */
class AchievementsViewModel(
    private val repository: AchievementRepository,
    private val userId: Long
) : ViewModel() {

    // Типы достижений для вкладок
    enum class AchievementType(val key: String) {
        ALL("ALL"),
        READING("READ_ENTRIES"),
        QUIZ("COMPLETE_QUIZ"),
        SOCIAL("ADD_COMMENTS"),
        GENERAL("GENERAL")
    }

    // Текущий выбранный тип достижений
    private val _selectedType = MutableLiveData<AchievementType>(AchievementType.ALL)
    val selectedType: LiveData<AchievementType> = _selectedType

    // Текущий список достижений
    private val _achievements = MutableLiveData<List<AchievementWithProgress>>(emptyList())
    val achievements: LiveData<List<AchievementWithProgress>> = _achievements

    // Статистика по достижениям
    private val _stats = MutableLiveData<AchievementStats>()
    val stats: LiveData<AchievementStats> = _stats

    // Состояние загрузки
    private val _isLoading = MutableLiveData<Boolean>(true)
    val isLoading: LiveData<Boolean> = _isLoading

    // Состояние ошибки
    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    // Отслеживание текущей LiveData для возможности отписки
    private var currentAchievementsSource: LiveData<List<AchievementWithProgress>>? = null

    init {
        // Инициализируем достижения пользователя при создании ViewModel
        viewModelScope.launch {
            try {
                repository.initUserAchievements(userId)
                loadAchievements()
                loadStats()
            } catch (e: Exception) {
                Log.e("AchievementsViewModel", "Ошибка при инициализации: ${e.message}")
                _error.value = "Не удалось загрузить достижения: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Загрузка достижений в соответствии с выбранным типом
     */
    fun loadAchievements() {
        _isLoading.value = true
        _error.value = null

        try {
            val currentType = _selectedType.value ?: AchievementType.ALL

            // Выберем подходящий источник данных
            val source = when (currentType) {
                AchievementType.ALL -> repository.getAchievementsWithProgress(userId)
                else -> repository.getAchievementsByType(userId, currentType.key)
            }

            // Обновим текущие данные когда source изменится
            source.observeForever { achievements ->
                _achievements.value = achievements
                _isLoading.value = false
            }

            // Сохраним источник, чтобы можно было отписаться позже
            currentAchievementsSource = source
        } catch (e: Exception) {
            _error.value = e.message ?: "Неизвестная ошибка"
            _isLoading.value = false
        }
    }

    /**
     * Загрузка статистики по достижениям
     */
    fun loadStats() {
        viewModelScope.launch {
            try {
                val achievementStats = repository.getAchievementStats(userId)
                _stats.value = achievementStats
            } catch (e: Exception) {
                // Ошибки статистики не критичны для работы экрана
            }
        }
    }

    /**
     * Изменение текущего типа достижений (вкладки)
     */
    fun setAchievementType(type: AchievementType) {
        if (_selectedType.value != type) {
            _selectedType.value = type
            loadAchievements()
        }
    }

    /**
     * Повторная загрузка данных
     */
    fun refresh() {
        loadAchievements()
        loadStats()
    }

    /**
     * Увеличить прогресс достижения при клике
     */
    fun incrementAchievement(achievement: AchievementWithProgress) {
        viewModelScope.launch {
            try {
                val achievementId = achievement.achievement.id
                val currentProgress = achievement.progress
                val newProgress = currentProgress + 1
                
                // Обновляем прогресс достижения
                repository.updateAchievementProgress(userId, achievementId, newProgress)
                
                Log.d("AchievementsViewModel", "Прогресс достижения обновлен: ${achievement.achievement.title} -> $newProgress")
                
                // Перезагружаем данные, чтобы отобразить изменения
                refresh()
            } catch (e: Exception) {
                Log.e("AchievementsViewModel", "Ошибка при обновлении прогресса: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Отписываемся от LiveData при уничтожении ViewModel
        currentAchievementsSource = null // Just nullify the reference, observers will be cleaned up automatically
    }
}
 