package com.example.myworldapp2

import android.app.Application
import android.util.Log
import com.example.myworldapp2.data.database.AppDatabase
import com.example.myworldapp2.data.repository.*
import com.example.myworldapp2.util.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Класс приложения для инициализации базы данных и репозиториев
 */
class KidsEncyclopediaApp : Application() {

    // Область корутин для выполнения асинхронных операций
    private val _applicationScope = CoroutineScope(SupervisorJob())
    
    // Публичный доступ к области корутин
    val applicationScope get() = _applicationScope

    // Менеджер сессии
    val sessionManager by lazy { SessionManager(applicationContext) }

    // Ленивая инициализация базы данных
    val database by lazy { 
        Log.d("KidsEncyclopediaApp", "Инициализация базы данных")
        AppDatabase.getDatabase(this, _applicationScope) 
    }

    // Получаем DAOs для репозиториев
    val categoryDao by lazy { database.categoryDao() }
    val entryDao: com.example.myworldapp2.data.dao.EntryDao by lazy { database.entryDao() }
    val bookmarkDao by lazy { 
        Log.d("KidsEncyclopediaApp", "Инициализация BookmarkDao")
        database.bookmarkDao() 
    }
    val commentDao by lazy { database.commentDao() }
    val quizDao by lazy { database.quizDao() }
    val quizQuestionDao by lazy { database.quizQuestionDao() }
    val quizAnswerDao by lazy { database.quizAnswerDao() }
    val userQuizResultDao by lazy { database.userQuizResultDao() }
    val achievementDao by lazy { database.achievementDao() }
    val userAchievementDao by lazy { database.userAchievementDao() }
    val userDao by lazy { database.userDao() }
    val userProgressDao by lazy { database.userProgressDao() }
    val tagDao by lazy { database.tagDao() }
    val entryTagDao by lazy { database.entryTagDao() }
    val likeDao by lazy { database.likeDao() }

    // Инициализация репозиториев в правильном порядке
    // Сначала инициализируем репозитории, которые не зависят от других
    val categoryRepository by lazy { CategoryRepository(categoryDao, entryDao) }
    val entryRepository: com.example.myworldapp2.data.repository.EntryRepository by lazy { EntryRepository(entryDao = entryDao) }
    val bookmarkRepository by lazy { 
        Log.d("KidsEncyclopediaApp", "Инициализация BookmarkRepository")
        BookmarkRepository(bookmarkDao = bookmarkDao, entryDao = entryDao)
    }
    val commentRepository by lazy { CommentRepository(commentDao) }
    val quizRepository by lazy { 
        QuizRepository(
            quizDao, 
            quizQuestionDao, 
            quizAnswerDao
        ) 
    }
    val userQuizResultRepository by lazy { UserQuizResultRepository(userQuizResultDao) }
    val achievementRepository by lazy { 
        AchievementRepository.getInstance(achievementDao, userAchievementDao)
    }
    val userAchievementRepository by lazy { UserAchievementRepository(userAchievementDao) }
    val tagRepository by lazy { TagRepository(tagDao, entryTagDao) }
    val likeRepository by lazy { LikeRepository(likeDao) }
    
    // Теперь инициализируем UserRepository, который зависит от других репозиториев
    val userRepository by lazy { 
        UserRepository(
            userDao = userDao, 
            userProgressDao = userProgressDao,
            categoryRepository = categoryRepository,
            entryRepository = entryRepository,
            quizRepository = quizRepository,
            userQuizResultRepository = userQuizResultRepository,
            userAchievementRepository = userAchievementRepository,
            sessionManager = sessionManager
        ) 
    }
    
    // И наконец SearchRepository, который использует разные репозитории
    val searchRepository by lazy {
        SearchRepository(
            entryDao = entryDao,
            entryRepository = entryRepository,
            categoryDao = categoryDao,
            tagDao = tagDao,
            userProgressDao = userProgressDao
        )
    }

    override fun onCreate() {
        super.onCreate()
        
        Log.d("KidsEncyclopediaApp", "Инициализация приложения...")
        
        // Приложение использует ленивую инициализацию, поэтому нужно обратиться к репозиториям
        // чтобы они были созданы перед проверкой пользователей
        val db = database 
        
        // Инициализируем без проверки базы данных, так как это делается в MainActivity
        // Это предотвращает дублирование данных при многократной инициализации
        Log.d("KidsEncyclopediaApp", "База данных инициализирована. Базовая настройка завершена.")
    }
} 