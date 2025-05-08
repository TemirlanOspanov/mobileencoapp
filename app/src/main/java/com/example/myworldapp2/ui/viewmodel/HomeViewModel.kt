package com.example.myworldapp2.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myworldapp2.data.entity.Achievement
import com.example.myworldapp2.data.entity.Category
import com.example.myworldapp2.data.entity.Entry
import com.example.myworldapp2.data.model.AchievementWithProgress
import com.example.myworldapp2.data.repository.AchievementRepository
import com.example.myworldapp2.data.repository.CategoryRepository
import com.example.myworldapp2.data.repository.EntryRepository
import com.example.myworldapp2.data.repository.UserAchievementRepository
import com.example.myworldapp2.data.repository.UserRepository
import kotlinx.coroutines.launch
import java.util.Date

/**
 * ViewModel для главного экрана приложения
 */
class HomeViewModel(
    private val categoryRepository: CategoryRepository,
    private val entryRepository: EntryRepository,
    private val userRepository: UserRepository,
    private val achievementRepository: AchievementRepository,
    private val userAchievementRepository: UserAchievementRepository
) : ViewModel() {

    // LiveData для отображения категорий
    val categories: LiveData<List<Category>> = categoryRepository.getAllCategories()

    // LiveData для отображения недавних статей
    val recentEntries = MediatorLiveData<List<Entry>>()

    // LiveData для отображения достижений пользователя
    val userAchievements = MediatorLiveData<List<AchievementWithProgress>>()

    // LiveData для отображения общего прогресса
    private val _overallProgress = MutableLiveData<Float>()
    val overallProgress: LiveData<Float> = _overallProgress

    private val _welcomeMessage = MutableLiveData<String>()
    val welcomeMessage: LiveData<String> = _welcomeMessage

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // ID текущего пользователя (пока заглушка)
    private val currentUserId: Long = userRepository.getCurrentUserId()

    // Кеш для статуса прочтения статей
    private val readEntriesCache = mutableMapOf<Long, Boolean>()

    init {
        loadWelcomeMessage()
        loadRecentEntries()
        loadUserAchievements()
        loadOverallProgress()
        loadReadEntriesStatus()
    }

    private fun loadWelcomeMessage() {
        viewModelScope.launch {
            val user = userRepository.getUserByIdSync(currentUserId)
            _welcomeMessage.value = "Добро пожаловать, ${user?.name ?: "Гость"}!"
        }
    }

    /**
     * Загружает недавние статьи
     */
    private fun loadRecentEntries() {
        // Здесь можно комбинировать данные из разных источников
        recentEntries.addSource(entryRepository.allEntries) { entries ->
            // Статьи уже отсортированы по дате в порядке от новых к старым
            // Ограничиваем количество для отображения
            val limitedEntries = entries.take(5)
            recentEntries.value = limitedEntries
        }
    }

    /**
     * Загружает достижения пользователя
     */
    private fun loadUserAchievements() {
        userAchievements.addSource(userAchievementRepository.getEarnedAchievements(currentUserId)) { achievements ->
            viewModelScope.launch {
                val achievementsWithProgress = achievements.map { achievement ->
                    val userAchievement = userAchievementRepository.getUserAchievement(currentUserId, achievement.id)
                    val progress = userAchievement?.progress ?: 0
                    val isCompleted = userAchievement?.isCompleted() ?: false
                    val completedDate = userAchievement?.completedAt
                    
                    AchievementWithProgress(
                        achievement = achievement,
                        progress = progress,
                        isCompleted = isCompleted,
                        completedDate = completedDate
                    )
                }
                userAchievements.value = achievementsWithProgress
            }
        }
    }

    /**
     * Загружает общий прогресс пользователя
     */
    private fun loadOverallProgress() {
        viewModelScope.launch {
            try {
                val progress = userRepository.getUserOverallProgress(currentUserId)
                _overallProgress.value = progress
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки прогресса: ${e.message}"
                _overallProgress.value = 0f
            }
        }
    }

    private fun loadReadEntriesStatus() {
        viewModelScope.launch {
            try {
                // Получаем все статьи
                val allEntries = recentEntries.value ?: emptyList()
                
                // Загружаем статус прочтения для каждой статьи
                for (entry in allEntries) {
                    val isRead = userRepository.isEntryRead(currentUserId, entry.id)
                    readEntriesCache[entry.id] = isRead
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки статуса прочтения: ${e.message}"
            }
        }
    }

    /**
     * Обновляет данные на главном экране
     */
    fun refreshData() {
        loadRecentEntries()
        loadUserAchievements()
        loadOverallProgress()
    }

    fun isEntryRead(entryId: Long): Boolean {
        // Возвращаем значение из кеша, если оно есть
        return readEntriesCache[entryId] ?: false
    }

    fun markEntryAsRead(entryId: Long) {
        viewModelScope.launch {
            try {
                userRepository.markEntryAsRead(currentUserId, entryId)
                // Обновляем кеш
                readEntriesCache[entryId] = true
                // Обновляем общий прогресс
                loadOverallProgress()
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка отметки статьи как прочитанной: ${e.message}"
            }
        }
    }

    fun getAchievementProgress(achievementId: Long): Pair<Int, Int> {
        // TODO: Реализовать логику получения прогресса для конкретного достижения
        // Возвращаем пару (текущий прогресс, максимум)
        return Pair(0, 100) // ЗАГЛУШКА
    }
} 