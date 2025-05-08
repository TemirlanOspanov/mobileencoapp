package com.example.myworldapp2.ui.adapter

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myworldapp2.R
import com.example.myworldapp2.data.entity.User
import com.example.myworldapp2.databinding.ItemUserBinding

/**
 * Адаптер для отображения списка пользователей в RecyclerView
 */
class UserAdapter(
    private val onUserClick: (User) -> Unit,
    private val onEditRole: (User) -> Unit,
    private val onDeleteUser: (User) -> Unit
) : ListAdapter<User, UserAdapter.UserViewHolder>(UserDiffCallback()) {

    private var originalList: List<User> = emptyList()
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user)
    }
    
    /**
     * Установка полного списка пользователей (без фильтрации)
     */
    fun setUsers(users: List<User>) {
        originalList = users
        submitList(users)
    }
    
    /**
     * Фильтрация списка пользователей по запросу (имя или email)
     */
    fun filter(query: String) {
        val filteredList = if (query.isEmpty()) {
            originalList
        } else {
            originalList.filter {
                it.name.contains(query, ignoreCase = true) || 
                it.email.contains(query, ignoreCase = true)
            }
        }
        submitList(filteredList)
    }
    
    /**
     * Фильтрация списка пользователей по роли
     */
    fun filterByRole(role: String?) {
        val filteredList = when (role) {
            null -> originalList // Все пользователи
            else -> originalList.filter { it.role == role }
        }
        submitList(filteredList)
    }

    inner class UserViewHolder(
        private val binding: ItemUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        init {
            // Обработка клика по элементу списка
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onUserClick(getItem(position))
                }
            }
            
            // Обработка клика по кнопке меню
            binding.menuButton.setOnClickListener { view ->
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    showPopupMenu(view, getItem(position))
                }
            }
        }
        
        fun bind(user: User) {
            // Заполняем данные пользователя
            binding.tvUserName.text = user.name
            binding.tvUserEmail.text = user.email
            
            // Устанавливаем роль пользователя с соответствующим цветом
            val roleText = when (user.role) {
                "admin" -> "Администратор"
                "editor" -> "Редактор"
                else -> "Пользователь"
            }
            binding.chipUserRole.text = roleText
            
            // Цвет для роли (можно выбрать нужные цвета)
            val chipColor = when (user.role) {
                "admin" -> R.color.admin_color
                "editor" -> R.color.editor_color
                else -> R.color.user_color
            }
            binding.chipUserRole.setChipBackgroundColorResource(chipColor)
            
            // Загрузка аватара пользователя (если есть URL)
            user.avatarUrl?.let { url ->
                // TODO: загрузить аватар с использованием Glide или другой библиотеки
                // Glide.with(binding.root.context)
                //     .load(url)
                //     .placeholder(R.drawable.ic_user_placeholder)
                //     .into(binding.ivUserAvatar)
            } ?: run {
                // Если URL аватара отсутствует, показываем дефолтную иконку
                binding.ivUserAvatar.setImageResource(R.drawable.ic_user_placeholder)
            }
        }
        
        private fun showPopupMenu(view: View, user: User) {
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.menu_user_actions, popup.menu)
            
            // Если пользователь является админом, скрываем опцию "Сделать админом"
            if (user.role == "admin") {
                popup.menu.findItem(R.id.action_make_admin).isVisible = false
                popup.menu.findItem(R.id.action_make_user).isVisible = true
            } else {
                popup.menu.findItem(R.id.action_make_admin).isVisible = true
                popup.menu.findItem(R.id.action_make_user).isVisible = false
            }
            
            popup.setOnMenuItemClickListener { menuItem: MenuItem ->
                when (menuItem.itemId) {
                    R.id.action_make_admin, R.id.action_make_user -> {
                        onEditRole(user)
                        true
                    }
                    R.id.action_delete -> {
                        onDeleteUser(user)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    /**
     * DiffCallback для эффективного обновления списка
     */
    class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
} 