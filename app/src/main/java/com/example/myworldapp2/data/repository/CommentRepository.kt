package com.example.myworldapp2.data.repository

import androidx.lifecycle.LiveData
import com.example.myworldapp2.data.dao.CommentDao
import com.example.myworldapp2.data.entity.Comment
import java.util.Date

/**
 * Репозиторий для работы с комментариями
 */
class CommentRepository(private val commentDao: CommentDao) {

    // Получение комментария по ID
    fun getCommentById(commentId: Long): LiveData<Comment> {
        return commentDao.getCommentById(commentId)
    }
    
    // Получение комментариев для статьи
    fun getCommentsByEntryId(entryId: Long): LiveData<List<Comment>> {
        return commentDao.getCommentsByEntryId(entryId)
    }
    
    // Получение комментариев пользователя
    fun getCommentsByUser(userId: Long): LiveData<List<Comment>> {
        return commentDao.getCommentsByUser(userId)
    }
    
    // Добавление нового комментария
    suspend fun insertComment(userId: Long, entryId: Long, content: String): Long {
        val comment = Comment(
            userId = userId,
            entryId = entryId,
            content = content
        )
        return commentDao.insertComment(comment)
    }
    
    // Обновление комментария
    suspend fun updateComment(commentId: Long, newContent: String) {
        val comment = commentDao.getCommentById(commentId).value
        if (comment != null) {
            val updatedComment = comment.copy(
                content = newContent,
                updatedAt = Date()
            )
            commentDao.update(updatedComment)
        }
    }
    
    // Удаление комментария
    suspend fun deleteComment(comment: Comment) {
        commentDao.delete(comment)
    }
    
    // Удаление всех комментариев для статьи
    suspend fun deleteAllCommentsForEntry(entryId: Long) {
        commentDao.deleteAllCommentsForEntry(entryId)
    }
    
    // Получение количества комментариев для статьи
    suspend fun getCommentCountForEntry(entryId: Long): Int {
        return commentDao.getCommentCountByEntry(entryId)
    }
    
    // Получение количества комментариев пользователя
    suspend fun getCommentCountForUser(userId: Long): Int {
        return commentDao.getCommentCountByUser(userId)
    }
} 