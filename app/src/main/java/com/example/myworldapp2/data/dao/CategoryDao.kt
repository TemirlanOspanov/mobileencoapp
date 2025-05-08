package com.example.myworldapp2.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myworldapp2.data.entity.Category

/**
 * DAO для работы с категориями
 */
@Dao
interface CategoryDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<Category>): List<Long>
    
    @Update
    suspend fun update(category: Category)
    
    @Delete
    suspend fun delete(category: Category)
    
    @Query("SELECT * FROM categories WHERE id = :categoryId")
    fun getCategoryById(categoryId: Long): LiveData<Category>
    
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): LiveData<List<Category>>
    
    // Синхронная версия метода получения категорий
    @Query("SELECT * FROM categories ORDER BY name ASC")
    suspend fun getAllCategoriesSync(): List<Category>
    
    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryByIdSync(id: Long): Category?
    
    @Query("SELECT name FROM categories WHERE id = :id")
    suspend fun getCategoryNameById(id: Long): String?
    
    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int
    
    @Query("DELETE FROM categories")
    suspend fun deleteAll()
    
    @Query("SELECT * FROM categories WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchCategories(query: String): LiveData<List<Category>>

    /**
     * Поиск категорий по названию
     * @param query Поисковый запрос с символами % для LIKE запроса
     * @return Список категорий, соответствующих запросу
     */
    @Query("SELECT * FROM categories WHERE name LIKE :query")
    suspend fun searchByName(query: String): List<Category>
} 