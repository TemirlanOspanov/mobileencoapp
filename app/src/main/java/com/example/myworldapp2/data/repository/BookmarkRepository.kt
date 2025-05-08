package com.example.myworldapp2.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.map
import androidx.lifecycle.MutableLiveData
import com.example.myworldapp2.data.dao.BookmarkDao
import com.example.myworldapp2.data.dao.EntryDao
import com.example.myworldapp2.data.entity.Bookmark
import com.example.myworldapp2.data.entity.Entry
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Репозиторий для работы с закладками
 */
class BookmarkRepository(
    private val bookmarkDao: BookmarkDao,
    private val entryDao: EntryDao // Добавляем EntryDao для получения данных об статьях
) {
    
    // LiveData для закладок с возможностью принудительного обновления
    private val bookmarkedEntriesCache = MutableLiveData<List<Entry>>()
    
    // Получение закладок пользователя
    fun getBookmarksByUser(userId: Long): LiveData<List<Bookmark>> {
        Log.d("BookmarkRepository", "getBookmarksByUser: userId=$userId")
        return bookmarkDao.getBookmarksByUser(userId)
    }
    
    // Получение статей, добавленных в закладки, с их полной информацией
    fun getBookmarkedEntriesWithDetails(userId: Long): LiveData<List<Entry>> {
        Log.d("BookmarkRepository", "getBookmarkedEntriesWithDetails: userId=$userId")
        
        // Немедленно запускаем обновление кэша
        refreshBookmarkedEntriesNow(userId)
        
        return bookmarkedEntriesCache
    }
    
    // Синхронно обновляет кеш закладок с немедленным запросом данных
    private fun refreshBookmarkedEntriesNow(userId: Long) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                Log.d("BookmarkRepository", "Запуск немедленного обновления закладок для userId=$userId")
                
                // Напрямую получаем ID всех закладок для пользователя
                val bookmarkIds = withContext(Dispatchers.IO) {
                    bookmarkDao.getBookmarkIdsByUserSync(userId)
                }
                
                Log.d("BookmarkRepository", "Найдены ID закладок: $bookmarkIds (количество: ${bookmarkIds.size})")
                
                if (bookmarkIds.isNotEmpty()) {
                    // Напрямую получаем информацию о статьях через DAO
                    val entries = withContext(Dispatchers.IO) {
                        entryDao.getEntriesByIds(bookmarkIds)
                    }
                    
                    Log.d("BookmarkRepository", "Загружены статьи: ${entries.size}")
                    
                    // Обновляем кеш
                    bookmarkedEntriesCache.postValue(entries)
                } else {
                    Log.d("BookmarkRepository", "Закладки не найдены для пользователя $userId")
                    bookmarkedEntriesCache.postValue(emptyList())
                }
            } catch (e: Exception) {
                Log.e("BookmarkRepository", "Ошибка при обновлении закладок", e)
                bookmarkedEntriesCache.postValue(emptyList())
            }
        }
    }
    
    // Проверка, добавлена ли статья в закладки у пользователя
    suspend fun isBookmarked(userId: Long, entryId: Long): Boolean {
        val result = bookmarkDao.getBookmarkByUserAndEntry(userId, entryId) != null
        Log.d("BookmarkRepository", "isBookmarked: userId=$userId, entryId=$entryId, result=$result")
        return result
    }
    
    // Добавление закладки
    suspend fun addBookmark(userId: Long, entryId: Long): Long {
        Log.d("BookmarkRepository", "addBookmark: userId=$userId, entryId=$entryId")
        return withContext(Dispatchers.IO) {
            val bookmark = Bookmark(userId = userId, entryId = entryId)
            val result = bookmarkDao.insert(bookmark)
            Log.d("BookmarkRepository", "addBookmark: результат=$result")
            
            // Увеличиваем задержку, чтобы транзакция точно успела завершиться
            delay(200)
            
            // Дополнительно проверяем, что закладка действительно добавлена
            val isActuallyBookmarked = bookmarkDao.getBookmarkByUserAndEntry(userId, entryId) != null
            Log.d("BookmarkRepository", "Проверка после добавления: закладка существует = $isActuallyBookmarked")
            
            // После добавления обновляем кеш
            refreshBookmarkedEntriesNow(userId)
            
            result
        }
    }
    
    // Удаление закладки
    suspend fun removeBookmark(userId: Long, entryId: Long) {
        Log.d("BookmarkRepository", "removeBookmark: userId=$userId, entryId=$entryId")
        withContext(Dispatchers.IO) {
            bookmarkDao.deleteByUserAndEntry(userId, entryId)
            
            // Увеличиваем задержку, чтобы транзакция точно успела завершиться
            delay(200)
            
            // Дополнительно проверяем, что закладка действительно удалена
            val stillExists = bookmarkDao.getBookmarkByUserAndEntry(userId, entryId) != null
            Log.d("BookmarkRepository", "Проверка после удаления: закладка всё ещё существует = $stillExists")
            
            // После удаления обновляем кеш
            refreshBookmarkedEntriesNow(userId)
        }
    }
    
    // Переключение состояния закладки (добавить, если нет; удалить, если есть)
    suspend fun toggleBookmark(userId: Long, entryId: Long): Boolean {
        val isBookmarked = isBookmarked(userId, entryId)
        Log.d("BookmarkRepository", "toggleBookmark: userId=$userId, entryId=$entryId, текущее состояние=$isBookmarked")
        
        return withContext(Dispatchers.IO) {
            if (isBookmarked) {
                removeBookmark(userId, entryId)
            } else {
                addBookmark(userId, entryId)
            }
            
            // Дополнительная проверка нового состояния
            val newState = !isBookmarked
            Log.d("BookmarkRepository", "toggleBookmark: новое состояние=$newState")
            
            // Принудительное уведомление наблюдателей о изменении закладок
            try {
                // Запрашиваем полное количество закладок для проверки состояния БД
                val totalBookmarks = bookmarkDao.getBookmarkCountByUser(userId)
                Log.d("BookmarkRepository", "Общее количество закладок пользователя: $totalBookmarks")
                
                // Обновляем кеш закладок
                refreshBookmarkedEntriesNow(userId)
            } catch (e: Exception) {
                Log.e("BookmarkRepository", "Ошибка при проверке количества закладок", e)
            }
            
            !isBookmarked
        }
    }
    
    // Получение количества закладок пользователя
    suspend fun getBookmarkCountByUser(userId: Long): Int {
        val count = bookmarkDao.getBookmarkCountByUser(userId)
        Log.d("BookmarkRepository", "getBookmarkCountByUser: userId=$userId, count=$count")
        return count
    }
    
    // Получение количества закладок статьи
    suspend fun getBookmarkCountByEntry(entryId: Long): Int {
        val count = bookmarkDao.getBookmarkCountByEntry(entryId)
        Log.d("BookmarkRepository", "getBookmarkCountByEntry: entryId=$entryId, count=$count")
        return count
    }
} 