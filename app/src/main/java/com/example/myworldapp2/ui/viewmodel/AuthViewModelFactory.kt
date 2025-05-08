package com.example.myworldapp2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myworldapp2.data.repository.UserRepository

/**
 * Фабрика для создания AuthViewModel
 * @param userRepository Репозиторий пользователей
 */
class AuthViewModelFactory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 