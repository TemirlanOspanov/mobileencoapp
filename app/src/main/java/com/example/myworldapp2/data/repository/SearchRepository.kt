package com.example.myworldapp2.data.repository

import com.example.myworldapp2.data.dao.CategoryDao
import com.example.myworldapp2.data.dao.EntryDao
import com.example.myworldapp2.data.dao.TagDao
import com.example.myworldapp2.data.dao.UserProgressDao
import com.example.myworldapp2.data.model.MatchType
import com.example.myworldapp2.data.model.SearchResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Репозиторий для поиска
 */
class SearchRepository(
    private val entryDao: EntryDao,
    private val entryRepository: EntryRepository,
    private val categoryDao: CategoryDao,
    private val tagDao: TagDao,
    private val userProgressDao: UserProgressDao
) {
    /**
     * Поиск статей по запросу
     * @param query Поисковый запрос
     * @param filterByCategory Фильтр по категории
     * @param filterByTag Фильтр по тегу
     * @param filterUnread Показывать только непрочитанные
     * @param userId ID пользователя (необходим для проверки прочитанных статей)
     * @return Flow со списком результатов поиска
     */
    fun search(
        query: String,
        filterByCategory: Boolean = false,
        filterByTag: Boolean = false,
        filterUnread: Boolean = false,
        userId: Long? = null
    ): Flow<List<SearchResult>> = flow {
        val results = mutableListOf<SearchResult>()
        
        // Если строка поиска пустая, возвращаем пустой список
        if (query.isBlank()) {
            emit(emptyList())
            return@flow
        }
        
        // 1. Поиск по заголовкам
        val titleMatches = entryDao.searchByTitle("%$query%")
        for (entry in titleMatches) {
            val matchType = if (entry.title.equals(query, ignoreCase = true)) {
                MatchType.TITLE_EXACT
            } else {
                MatchType.TITLE_PARTIAL
            }
            
            // Если включен фильтр непрочитанных, проверяем статус
            if (filterUnread && userId != null && userProgressDao.isEntryRead(userId, entry.id)) {
                continue
            }
            
            results.add(
                SearchResult(
                    entry = entry,
                    matchType = matchType,
                    relevanceScore = if (matchType == MatchType.TITLE_EXACT) 10.0f else 5.0f
                )
            )
        }
        
        // 2. Поиск по содержимому
        val contentMatches = entryDao.searchByContent("%$query%")
        for (entry in contentMatches) {
            // Пропускаем, если эта статья уже найдена по заголовку
            if (results.any { it.entry.id == entry.id }) {
                continue
            }
            
            val matchType = if (entry.content.equals(query, ignoreCase = true)) {
                MatchType.CONTENT_EXACT
            } else {
                MatchType.CONTENT_PARTIAL
            }
            
            // Если включен фильтр непрочитанных, проверяем статус
            if (filterUnread && userId != null && userProgressDao.isEntryRead(userId, entry.id)) {
                continue
            }
            
            // Находим контекст совпадения (фрагмент текста с найденным запросом)
            val highlightedText = getHighlightedText(entry.content, query)
            
            results.add(
                SearchResult(
                    entry = entry,
                    matchType = matchType,
                    highlightedText = highlightedText,
                    relevanceScore = if (matchType == MatchType.CONTENT_EXACT) 4.0f else 3.0f
                )
            )
        }
        
        // 3. Поиск по тегам (если включен соответствующий фильтр)
        if (filterByTag) {
            val tagMatches = tagDao.searchEntriesByTagName("%$query%")
            
            for (entry in tagMatches) {
                // Пропускаем, если эта статья уже найдена
                if (results.any { it.entry.id == entry.id }) {
                    continue
                }
                
                // Если включен фильтр непрочитанных, проверяем статус
                if (filterUnread && userId != null && userProgressDao.isEntryRead(userId, entry.id)) {
                    continue
                }
                
                results.add(
                    SearchResult(
                        entry = entry,
                        matchType = MatchType.TAG_MATCH,
                        relevanceScore = 2.0f
                    )
                )
            }
        }
        
        // 4. Поиск по категориям (если включен соответствующий фильтр)
        if (filterByCategory) {
            val categoryMatches = categoryDao.searchByName("%$query%")
            
            for (category in categoryMatches) {
                val entriesInCategory = entryRepository.getEntriesByCategorySync(category.id)
                
                for (entry in entriesInCategory) {
                    // Пропускаем, если эта статья уже найдена
                    if (results.any { it.entry.id == entry.id }) {
                        continue
                    }
                    
                    // Если включен фильтр непрочитанных, проверяем статус
                    if (filterUnread && userId != null && userProgressDao.isEntryRead(userId, entry.id)) {
                        continue
                    }
                    
                    results.add(
                        SearchResult(
                            entry = entry,
                            matchType = MatchType.CATEGORY_MATCH,
                            relevanceScore = 1.0f
                        )
                    )
                }
            }
        }
        
        // Сортируем результаты по релевантности (сначала более релевантные)
        val sortedResults = results.sortedByDescending { it.relevanceScore }
        
        // Отправляем результаты
        emit(sortedResults)
    }
    
    /**
     * Получить фрагмент текста с найденным запросом (для отображения контекста)
     * @param content Полный текст
     * @param query Поисковый запрос
     * @return Фрагмент текста с найденным запросом
     */
    private fun getHighlightedText(content: String, query: String): String {
        val queryIndex = content.indexOf(query, ignoreCase = true)
        if (queryIndex == -1) return ""
        
        // Извлекаем контекст с некоторым отступом до и после найденного запроса
        val start = maxOf(0, queryIndex - 50)
        val end = minOf(content.length, queryIndex + query.length + 50)
        
        val prefix = if (start > 0) "..." else ""
        val suffix = if (end < content.length) "..." else ""
        
        return prefix + content.substring(start, end) + suffix
    }
} 