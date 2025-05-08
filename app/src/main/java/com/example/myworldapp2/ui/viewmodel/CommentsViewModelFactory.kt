package com.example.myworldapp2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myworldapp2.data.repository.CommentRepository
import com.example.myworldapp2.data.repository.UserRepository

/**
 * Factory для создания CommentsViewModel с необходимыми зависимостями
 */
class CommentsViewModelFactory(
    private val commentRepository: CommentRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {

    private var entryId: Long = 0

    fun setEntryId(id: Long) {
        entryId = id
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CommentsViewModel::class.java)) {
            return CommentsViewModel(
                entryId,
                commentRepository,
                userRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 