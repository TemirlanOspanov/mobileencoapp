package com.example.myworldapp2.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myworldapp2.R
import com.example.myworldapp2.data.entity.Category

/**
 * Адаптер для отображения категорий в RecyclerView
 */
class CategoryAdapter(private val onCategoryClicked: (Category) -> Unit) :
    ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_card, parent, false)
        return CategoryViewHolder(view, onCategoryClicked)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * ViewHolder для отображения элемента категории
     */
    class CategoryViewHolder(
        itemView: View,
        private val onCategoryClicked: (Category) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val background: RelativeLayout = itemView.findViewById(R.id.category_background)
        private val nameTextView: TextView = itemView.findViewById(R.id.tv_category_name)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.tv_category_description)
        private val iconImageView: ImageView = itemView.findViewById(R.id.img_category_icon)
        private val entryCountTextView: TextView = itemView.findViewById(R.id.tv_entry_count)

        fun bind(category: Category) {
            nameTextView.text = category.name
            descriptionTextView.text = category.description
            
            // Устанавливаем цвет фона из category.color (HEX строка)
            try {
                background.setBackgroundColor(Color.parseColor(category.color))
            } catch (e: Exception) {
                // Если не удалось распарсить цвет, используем цвет по умолчанию
                background.setBackgroundColor(Color.parseColor("#4CAF50"))
            }
            
            // Загрузка иконки по имени или из url
            // В реальном приложении здесь будет код для загрузки иконки через Glide
            // или из ресурсов по имени
            // Временно используем стандартную иконку
            
            // Временно задаем количество статей
            entryCountTextView.text = itemView.context.getString(
                R.string.entries_count, 0
            )
            
            // Обработка нажатия на элемент
            itemView.setOnClickListener {
                onCategoryClicked(category)
            }
        }
    }
}

/**
 * DiffCallback для эффективного обновления списка категорий
 */
class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
    override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
        return oldItem == newItem
    }
} 