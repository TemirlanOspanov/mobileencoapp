package com.example.myworldapp2.data.model

import com.example.myworldapp2.data.entity.Entry

/**
 * Класс, представляющий результат поиска
 * @param entry Найденная статья
 * @param matchType Тип совпадения (по названию, тексту, тегу и т.д.)
 * @param highlightedText Выделенный фрагмент текста с найденной фразой (опционально)
 * @param relevanceScore Оценка релевантности результата (выше - лучше)
 */
data class SearchResult(
    val entry: Entry,
    val matchType: MatchType,
    val highlightedText: String? = null,
    val relevanceScore: Float = 1.0f
)

/**
 * Перечисление типов совпадений при поиске
 */
enum class MatchType {
    TITLE_EXACT,     // Точное совпадение в заголовке
    TITLE_PARTIAL,   // Частичное совпадение в заголовке
    CONTENT_EXACT,   // Точное совпадение в содержимом
    CONTENT_PARTIAL, // Частичное совпадение в содержимом
    TAG_MATCH,       // Совпадение по тегу
    CATEGORY_MATCH   // Совпадение по категории
} 