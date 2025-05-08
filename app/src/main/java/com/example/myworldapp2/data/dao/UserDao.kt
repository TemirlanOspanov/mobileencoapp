package com.example.myworldapp2.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myworldapp2.data.entity.User
import android.util.Log

/**
 * DAO для работы с пользователями
 */
@Dao
interface UserDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User): Long
    
    @Update
    suspend fun update(user: User)
    
    @Delete
    suspend fun delete(user: User)
    
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: Long): LiveData<User>
    
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserByIdSync(userId: Long): User?
    
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?
    
    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAllUsers(): LiveData<List<User>>
    
    @Query("SELECT * FROM users WHERE role = :role ORDER BY name ASC")
    fun getUsersByRole(role: String): LiveData<List<User>>
    
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
    
    @Query("DELETE FROM users")
    suspend fun deleteAll()
    
    @Query("SELECT * FROM users WHERE email = :email AND passwordHash = :passwordHash LIMIT 1")
    suspend fun login(email: String, passwordHash: String): User?
} 