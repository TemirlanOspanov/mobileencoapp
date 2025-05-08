package com.example.myworldapp2.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myworldapp2.data.dao.UserDao
import com.example.myworldapp2.data.dao.UserProgressDao
import com.example.myworldapp2.data.entity.User
import com.example.myworldapp2.data.entity.UserProgress
import com.example.myworldapp2.data.model.ActivityType
import com.example.myworldapp2.data.model.CategoryProgress
import com.example.myworldapp2.data.model.UserActivity
import com.example.myworldapp2.data.model.UserStats
import com.example.myworldapp2.util.SessionManager
import java.security.MessageDigest
import java.util.Date
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Репозиторий для работы с пользователями
 */
class UserRepository(
    private val userDao: UserDao,
    private val userProgressDao: UserProgressDao,
    private val categoryRepository: CategoryRepository,
    private val entryRepository: EntryRepository,
    private val quizRepository: QuizRepository,
    private val userQuizResultRepository: UserQuizResultRepository,
    private val userAchievementRepository: UserAchievementRepository,
    private val sessionManager: SessionManager
) {
    private val TAG = "UserRepository"

    // Текущий пользователь
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    // ID пользователя по умолчанию для гостевого режима
    private val DEFAULT_USER_ID = 1L
    
    // Инициализация - проверяем, есть ли сохраненный пользователь
    init {
        tryAutoLogin()
    }
    
    // Попытка автоматического входа при запуске приложения
    private fun tryAutoLogin() {
        if (sessionManager.isLoggedIn()) {
            val userId = sessionManager.getUserId()
            val userEmail = sessionManager.getUserEmail()
            Log.d(TAG, "Попытка автоматического входа для пользователя: $userId ($userEmail)")
            
            // Запускаем корутин для асинхронной загрузки пользователя
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val user = userDao.getUserByIdSync(userId)
                    if (user != null) {
                        Log.d(TAG, "Автоматический вход выполнен успешно для пользователя: ${user.name} (${user.email})")
                        setCurrentUser(user)
                    } else {
                        Log.d(TAG, "Не удалось найти пользователя с ID: $userId. Выполняем выход.")
                        sessionManager.logout()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Ошибка при автоматическом входе: ${e.message}", e)
                    sessionManager.logout()
                }
            }
        } else {
            Log.d(TAG, "Нет сохраненных данных для автоматического входа")
        }
    }
    
    // Получение ID текущего пользователя
    fun getCurrentUserId(): Long {
        return _currentUser.value?.id ?: DEFAULT_USER_ID
    }
    
    // Синхронное получение пользователя по ID
    suspend fun getUserByIdSync(userId: Long): User? {
        return userDao.getUserByIdSync(userId)
    }
    
    // Получение общего прогресса пользователя (в процентах)
    suspend fun getUserOverallProgress(userId: Long): Float {
        return userProgressDao.getUserOverallProgress(userId)
    }
    
    // Получение прогресса пользователя по категории (в процентах)
    suspend fun getUserCategoryProgress(userId: Long, categoryId: Long): Float {
        return userProgressDao.getUserCategoryProgress(userId, categoryId)
    }
    
    // Отметка статьи как прочитанной
    suspend fun markEntryAsRead(userId: Long, entryId: Long) {
        // Получаем запись о прогрессе пользователя для данной статьи
        var progress = userProgressDao.getProgressByUserAndEntry(userId, entryId)
        
        // Если записи нет, создаем новую
        if (progress == null) {
            progress = UserProgress(
                userId = userId,
                entryId = entryId,
                isRead = true,
                readAt = Date()
            )
            userProgressDao.insert(progress)
        } else if (!progress.isRead) {
            // Если статья не была отмечена как прочитанная, обновляем статус
            userProgressDao.updateReadStatus(userId, entryId, true, Date())
        }
    }
    
    // Проверка, прочитана ли статья пользователем
    suspend fun isEntryRead(userId: Long, entryId: Long): Boolean {
        return userProgressDao.isEntryRead(userId, entryId)
    }

    // Получение всех пользователей
    fun getAllUsers(): LiveData<List<User>> {
        return userDao.getAllUsers()
    }
    
    // Получение пользователей по роли
    fun getUsersByRole(role: String): LiveData<List<User>> {
        return userDao.getUsersByRole(role)
    }
    
    // Получение пользователя по ID
    suspend fun getUserById(userId: Long): User? {
        return userDao.getUserByIdSync(userId)
    }
    
    // Получение пользователя по email
    suspend fun getUserByEmail(email: String): User? {
        Log.d(TAG, "Запрос пользователя по email: $email")
        try {
            val user = userDao.getUserByEmail(email)
            Log.d(TAG, "Результат поиска пользователя по email: ${user != null}")
            return user
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при получении пользователя по email: ${e.message}", e)
            throw e
        }
    }
    
    // Регистрация пользователя
    suspend fun registerUser(email: String, password: String, name: String, role: String = "user"): Long {
        val passwordHash = hashPassword(password)
        val newUser = User(
            email = email,
            passwordHash = passwordHash,
            name = name,
            role = role
        )
        return userDao.insert(newUser)
    }
    
    // Аутентификация пользователя
    suspend fun login(email: String, password: String): User? {
        Log.d(TAG, "Запрос на вход пользователя с email: $email")
        val passwordHash = hashPassword(password)
        
        try {
            val user = userDao.login(email, passwordHash)
            Log.d(TAG, "Результат входа для $email: ${user != null}")
            
            if (user != null) {
                Log.d(TAG, "Успешный вход для пользователя ${user.id} (${user.email})")
            }
            
            return user
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при входе пользователя: ${e.message}", e)
            throw e
        }
    }
    
    // Проверка существования пользователя с указанным email
    suspend fun isEmailTaken(email: String): Boolean {
        return userDao.getUserByEmail(email) != null
    }
    
    // Вставка нового пользователя
    suspend fun insertUser(user: User): Long {
        Log.d(TAG, "Вставка нового пользователя: ${user.email}")
        try {
            val userId = userDao.insert(user)
            Log.d(TAG, "Пользователь вставлен, ID: $userId")
            return userId
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при вставке пользователя: ${e.message}", e)
            throw e
        }
    }
    
    // Хеширование пароля
    fun hashPassword(password: String): String {
        Log.d(TAG, "Хеширование пароля")
        try {
            val md = MessageDigest.getInstance("SHA-256")
            val digested = md.digest(password.toByteArray())
            val result = digested.joinToString("") { String.format("%02x", it) }
            Log.d(TAG, "Пароль успешно хеширован")
            return result
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при хешировании пароля: ${e.message}", e)
            throw e
        }
    }
    
    // Проверка пароля
    fun verifyPassword(password: String, hash: String): Boolean {
        return hashPassword(password) == hash
    }
    
    // Установка текущего пользователя
    fun setCurrentUser(user: User?) {
        _currentUser.postValue(user)
        
        // Если пользователь не null, сохраняем данные для автовхода
        if (user != null) {
            sessionManager.saveUserLogin(user.id, user.email, user.name)
        }
    }
    
    // Проверка аутентификации
    fun isAuthenticated(): Boolean {
        return _currentUser.value != null
    }
    
    // Выход из системы
    fun logout() {
        _currentUser.postValue(null)
        sessionManager.logout()
    }
    
    // Обновление пользователя
    suspend fun updateUser(user: User) {
        userDao.update(user)
        
        // Если обновляется текущий пользователь, обновляем и его
        if (_currentUser.value?.id == user.id) {
            _currentUser.postValue(user)
        }
    }
    
    // Обновление пароля пользователя
    suspend fun updatePassword(userId: Long, currentPassword: String, newPassword: String): Boolean {
        val user = userDao.getUserByIdSync(userId) ?: return false
        
        // Проверяем текущий пароль
        if (!verifyPassword(currentPassword, user.passwordHash)) {
            return false
        }
        
        // Обновляем пароль
        val passwordHash = hashPassword(newPassword)
        userDao.update(user.copy(passwordHash = passwordHash))
        return true
    }
    
    // Удаление пользователя
    suspend fun deleteUser(user: User) {
        userDao.delete(user)
        
        // Если удаляется текущий пользователь, выходим из системы
        if (_currentUser.value?.id == user.id) {
            logout()
        }
    }
    
    // Получение количества пользователей в базе данных
    suspend fun getUserCount(): Int {
        try {
            Log.d(TAG, "Запрос количества пользователей в базе данных")
            val count = userDao.getUserCount()
            Log.d(TAG, "Количество пользователей в базе данных: $count")
            return count
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при получении количества пользователей: ${e.message}", e)
            throw e
        }
    }
    
    // Получение прогресса по категориям для пользователя
    suspend fun getCategoryProgressForUser(userId: Long): List<CategoryProgress> {
        // Получаем все категории
        val categories = categoryRepository.getAllCategoriesSync()
        
        // Создаем список для результата
        val result = mutableListOf<CategoryProgress>()
        
        // Для каждой категории получаем статистику
        val categoriesList = categories.toList()  // Явное преобразование в List
        for (category in categoriesList) {
            // Получаем все статьи в категории
            val entries = entryRepository.getEntriesByCategorySync(category.id)
            
            // Получаем количество прочитанных статей в категории
            val readEntries = userProgressDao.getReadEntriesCountByCategory(userId, category.id)
            
            // Добавляем информацию о прогрессе в категории
            result.add(
                CategoryProgress(
                    categoryId = category.id,
                    categoryName = category.name,
                    colorHex = category.color,
                    readEntries = readEntries,
                    totalEntries = entries.size
                )
            )
        }
        
        return result
    }
    
    // Получение общей статистики пользователя
    suspend fun getUserStats(userId: Long): UserStats {
        // Получаем общее количество статей
        val totalEntries = entryRepository.getTotalEntriesCount()
        
        // Получаем количество прочитанных статей
        val readEntries = userProgressDao.getReadEntriesCount(userId)
        
        // Получаем общее количество викторин
        val totalQuizzes = quizRepository.getQuizzesCount()
        
        // Получаем количество пройденных викторин
        val completedQuizzes = userQuizResultRepository.getCompletedQuizCount(userId)
        
        // Получаем общее количество достижений
        val totalAchievements = userAchievementRepository.getTotalAchievementsCount()
        
        // Получаем количество полученных достижений
        val earnedAchievements = userAchievementRepository.getUserAchievementCount(userId)
        
        return UserStats(
            totalEntries = totalEntries,
            readEntries = readEntries,
            totalQuizzes = totalQuizzes,
            completedQuizzes = completedQuizzes,
            totalAchievements = totalAchievements,
            earnedAchievements = earnedAchievements
        )
    }
    
    // Получение недавних активностей пользователя
    suspend fun getRecentActivities(userId: Long, limit: Int = 10): List<UserActivity> {
        // Здесь в реальном приложении нужно получать данные из таблицы активностей
        // Поскольку в текущей БД нет такой таблицы, создадим тестовые данные
        
        // TODO: Заменить на реальные данные из БД
        
        val activities = mutableListOf<UserActivity>()
        
        // Прочитанные статьи (последние несколько)
        val readEntries = userProgressDao.getRecentReadEntries(userId, limit)
        for (progress in readEntries) {
            val entry = entryRepository.getEntryByIdSync(progress.entryId)
            if (entry != null) {
                activities.add(
                    UserActivity(
                        userId = userId,
                        type = ActivityType.READ_ENTRY,
                        title = entry.title,
                        details = "Категория: " + categoryRepository.getCategoryNameById(entry.categoryId),
                        timestamp = progress.readAt ?: Date(),
                        relatedId = entry.id
                    )
                )
            }
        }
        
        // Пройденные викторины
        val quizResults = userQuizResultRepository.getRecentQuizResults(userId, limit)
        for (result in quizResults) {
            val quiz = quizRepository.getQuizByIdSync(result.quizId)
            if (quiz != null) {
                activities.add(
                    UserActivity(
                        userId = userId,
                        type = ActivityType.COMPLETED_QUIZ,
                        title = "Викторина: ${quiz.title}",
                        details = "Результат: ${result.score}",
                        timestamp = result.completedAt,
                        relatedId = quiz.id
                    )
                )
            }
        }
        
        // Сортируем по времени (от новых к старым)
        return activities.sortedByDescending { it.timestamp }.take(limit)
    }
}

/**
 * Класс для хранения общей статистики пользователя
 */
data class UserStats(
    val totalEntries: Int,
    val readEntries: Int,
    val totalQuizzes: Int,
    val completedQuizzes: Int,
    val totalAchievements: Int,
    val earnedAchievements: Int
) 