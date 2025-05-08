package com.example.myworldapp2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myworldapp2.data.repository.UserRepository

/**
 * Factory для создания ProfileViewModel с необходимыми зависимостями
 */
class ProfileViewModelFactory(
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 