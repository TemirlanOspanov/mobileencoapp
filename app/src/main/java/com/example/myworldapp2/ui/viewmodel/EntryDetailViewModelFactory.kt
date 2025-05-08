package com.example.myworldapp2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myworldapp2.data.repository.BookmarkRepository
import com.example.myworldapp2.data.repository.CategoryRepository
import com.example.myworldapp2.data.repository.CommentRepository
import com.example.myworldapp2.data.repository.EntryRepository
import com.example.myworldapp2.data.repository.LikeRepository
import com.example.myworldapp2.data.repository.TagRepository
import com.example.myworldapp2.data.repository.UserRepository

/**
 * Factory для создания EntryDetailViewModel с необходимыми зависимостями
 */
class EntryDetailViewModelFactory(
    private val entryRepository: EntryRepository,
    private val categoryRepository: CategoryRepository,
    private val userRepository: UserRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val tagRepository: TagRepository,
    private val likeRepository: LikeRepository,
    private val commentRepository: CommentRepository
) : ViewModelProvider.Factory {

    private var entryId: Long = 0

    fun setEntryId(id: Long) {
        entryId = id
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EntryDetailViewModel::class.java)) {
            return EntryDetailViewModel(
                entryId,
                entryRepository,
                categoryRepository,
                userRepository,
                bookmarkRepository,
                tagRepository,
                likeRepository,
                commentRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 