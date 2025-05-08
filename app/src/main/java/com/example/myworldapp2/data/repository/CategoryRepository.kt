package com.example.myworldapp2.data.repository

import androidx.lifecycle.LiveData
import com.example.myworldapp2.data.dao.CategoryDao
import com.example.myworldapp2.data.dao.EntryDao
import com.example.myworldapp2.data.entity.Category
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Репозиторий для работы с категориями
 */
class CategoryRepository(
    private val categoryDao: CategoryDao,
    private val entryDao: EntryDao
) {
    
    /**
     * Получить все категории
     */
    fun getAllCategories(): LiveData<List<Category>> {
        return categoryDao.getAllCategories()
    }
    
    /**
     * Получить категорию по ID
     */
    fun getCategoryById(id: Long): LiveData<Category> {
        return categoryDao.getCategoryById(id)
    }
    
    /**
     * Добавить новую категорию
     */
    suspend fun insertCategory(category: Category): Long {
        return withContext(Dispatchers.IO) {
            categoryDao.insert(category)
        }
    }
    
    /**
     * Обновить существующую категорию
     */
    suspend fun updateCategory(category: Category) {
        withContext(Dispatchers.IO) {
            categoryDao.update(category)
        }
    }
    
    /**
     * Удалить категорию
     */
    suspend fun deleteCategory(category: Category) {
        withContext(Dispatchers.IO) {
            categoryDao.delete(category)
        }
    }
    
    /**
     * Поиск категорий по имени
     */
    fun searchCategories(query: String): LiveData<List<Category>> {
        return categoryDao.searchCategories(query)
    }
    
    /**
     * Получить количество категорий
     */
    suspend fun getCategoryCount(): Int {
        return withContext(Dispatchers.IO) {
            categoryDao.getCategoryCount()
        }
    }
    
    /**
     * Получить все категории с количеством статей в каждой
     * @return Map, где ключ - ID категории, значение - количество статей в этой категории
     */
    suspend fun getCategoriesWithEntriesCount(): Map<Long, Int> {
        return withContext(Dispatchers.IO) {
            val categories = categoryDao.getAllCategoriesSync()
            val result = mutableMapOf<Long, Int>()
            
            categories.forEach { category ->
                val count = entryDao.getEntryCountByCategoryId(category.id)
                result[category.id] = count
            }
            
            result
        }
    }
    
    /**
     * Добавить новую категорию с проверкой уникальности имени
     * @param name Название категории
     * @param description Описание категории
     * @param icon Имя иконки или URL
     * @param color Цвет в HEX формате
     * @return ID новой категории или -1, если категория с таким именем уже существует
     */
    suspend fun addNewCategory(name: String, description: String, icon: String, color: String): Long {
        return withContext(Dispatchers.IO) {
            // Проверяем, существует ли категория с таким именем
            val existingCategories = categoryDao.searchByName("%$name%")
            if (existingCategories.any { it.name.equals(name, ignoreCase = true) }) {
                return@withContext -1L
            }
            
            // Создаем и добавляем новую категорию
            val newCategory = Category(
                name = name,
                description = description,
                icon = icon,
                color = color
            )
            
            categoryDao.insert(newCategory)
        }
    }
    
    /**
     * Синхронное получение всех категорий (нужно для UserRepository)
     */
    suspend fun getAllCategoriesSync(): List<Category> {
        return withContext(Dispatchers.IO) {
            categoryDao.getAllCategoriesSync()
        }
    }
    
    /**
     * Синхронное получение категории по ID
     */
    suspend fun getCategoryByIdSync(categoryId: Long): Category? {
        return withContext(Dispatchers.IO) {
            categoryDao.getCategoryByIdSync(categoryId)
        }
    }
    
    /**
     * Получение названия категории по ID
     */
    suspend fun getCategoryNameById(categoryId: Long): String? {
        return withContext(Dispatchers.IO) {
            categoryDao.getCategoryNameById(categoryId)
        }
    }
} 