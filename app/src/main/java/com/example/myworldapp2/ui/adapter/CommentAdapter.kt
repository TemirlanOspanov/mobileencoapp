package com.example.myworldapp2.ui.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myworldapp2.R
import com.example.myworldapp2.data.entity.Comment
import com.example.myworldapp2.data.entity.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Адаптер для отображения комментариев
 */
class CommentAdapter(
    private val currentUserId: Long,
    private val getUserById: suspend (Long) -> User?,
    private val onDeleteComment: (Comment) -> Unit
) : ListAdapter<Comment, CommentAdapter.CommentViewHolder>(CommentDiffCallback()) {

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view, currentUserId, getUserById, onDeleteComment)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * ViewHolder для отображения комментария
     */
    class CommentViewHolder(
        itemView: View,
        private val currentUserId: Long,
        private val getUserById: suspend (Long) -> User?,
        private val onDeleteComment: (Comment) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val avatarImageView: ImageView = itemView.findViewById(R.id.img_user_avatar)
        private val usernameTextView: TextView = itemView.findViewById(R.id.tv_username)
        private val dateTextView: TextView = itemView.findViewById(R.id.tv_comment_date)
        private val contentTextView: TextView = itemView.findViewById(R.id.tv_comment_text)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.btn_delete_comment)

        fun bind(comment: Comment) {
            // Устанавливаем текст комментария
            contentTextView.text = comment.content

            // Форматируем и устанавливаем дату
            val dateFormat = SimpleDateFormat("d MMMM yyyy, HH:mm", Locale("ru"))
            dateTextView.text = dateFormat.format(comment.createdAt)

            // Проверяем, является ли текущий пользователь автором комментария
            val isCurrentUserComment = comment.userId == currentUserId
            
            // Показываем кнопку удаления только для своих комментариев
            deleteButton.visibility = if (isCurrentUserComment) View.VISIBLE else View.GONE
            
            // Устанавливаем обработчик нажатия на кнопку удаления
            deleteButton.setOnClickListener {
                if (isCurrentUserComment) {
                    // Показываем диалог подтверждения
                    showDeleteConfirmationDialog(comment)
                }
            }

            // Загружаем информацию о пользователе асинхронно
            CoroutineScope(Dispatchers.Main).launch {
                val user = withContext(Dispatchers.IO) {
                    getUserById(comment.userId)
                }

                // Отображаем имя пользователя
                usernameTextView.text = user?.name ?: "Неизвестный пользователь"

                // Загружаем аватар пользователя с помощью Glide
                if (user != null && !user.avatarUrl.isNullOrEmpty()) {
                    Glide.with(itemView.context)
                        .load(user.avatarUrl)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .circleCrop()
                        .into(avatarImageView)
                } else {
                    // Если у пользователя нет аватара, используем placeholder
                    Glide.with(itemView.context)
                         .load(R.drawable.ic_launcher_foreground)
                         .circleCrop()
                         .into(avatarImageView)
                }
            }

            // Для обратной совместимости сохраняем функциональность долгого нажатия
            itemView.setOnLongClickListener {
                if (isCurrentUserComment) {
                    showPopupMenu(comment)
                    true
                } else {
                    false
                }
            }
        }

        private fun showDeleteConfirmationDialog(comment: Comment) {
            val context = itemView.context
            AlertDialog.Builder(context)
                .setTitle(R.string.confirm_delete)
                .setMessage(R.string.confirm_delete_comment_message)
                .setPositiveButton(R.string.delete) { _, _ ->
                    onDeleteComment(comment)
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }

        private fun showPopupMenu(comment: Comment) {
            val popup = PopupMenu(itemView.context, itemView)
            popup.inflate(R.menu.menu_comment_actions)
            
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_delete_comment -> {
                        showDeleteConfirmationDialog(comment)
                        true
                    }
                    else -> false
                }
            }
            
            popup.show()
        }
    }
}

/**
 * DiffCallback для эффективного обновления списка комментариев
 */
class CommentDiffCallback : DiffUtil.ItemCallback<Comment>() {
    override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
        return oldItem == newItem
    }
} 