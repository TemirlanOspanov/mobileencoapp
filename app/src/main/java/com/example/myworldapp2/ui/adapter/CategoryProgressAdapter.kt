package com.example.myworldapp2.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myworldapp2.R
import com.example.myworldapp2.data.model.CategoryProgress
import com.example.myworldapp2.databinding.ItemCategoryProgressBinding

/**
 * Адаптер для отображения прогресса по категориям
 */
class CategoryProgressAdapter : ListAdapter<CategoryProgress, CategoryProgressAdapter.CategoryProgressViewHolder>(CategoryProgressDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryProgressViewHolder {
        val binding = ItemCategoryProgressBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryProgressViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryProgressViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CategoryProgressViewHolder(
        private val binding: ItemCategoryProgressBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(categoryProgress: CategoryProgress) {
            binding.apply {
                // Устанавливаем цвет категории
                viewCategoryColor.setBackgroundColor(Color.parseColor(categoryProgress.colorHex))
                
                // Устанавливаем название категории
                tvCategoryName.text = categoryProgress.categoryName
                
                // Устанавливаем процент прогресса
                val progressPercent = calculateProgressPercent(
                    categoryProgress.readEntries,
                    categoryProgress.totalEntries
                )
                tvProgressPercent.text = binding.root.context.getString(
                    R.string.progress_percent,
                    progressPercent
                )
                
                // Устанавливаем прогресс-бар
                progressIndicator.progress = progressPercent
                
                // Устанавливаем детали прогресса
                tvProgressDetails.text = binding.root.context.getString(
                    R.string.read_entries_in_category,
                    categoryProgress.readEntries,
                    categoryProgress.totalEntries
                )
            }
        }

        /**
         * Вычисляет процент прогресса
         */
        private fun calculateProgressPercent(read: Int, total: Int): Int {
            return if (total > 0) {
                ((read.toFloat() / total.toFloat()) * 100).toInt()
            } else {
                0
            }
        }
    }

    class CategoryProgressDiffCallback : DiffUtil.ItemCallback<CategoryProgress>() {
        override fun areItemsTheSame(oldItem: CategoryProgress, newItem: CategoryProgress): Boolean {
            return oldItem.categoryId == newItem.categoryId
        }

        override fun areContentsTheSame(oldItem: CategoryProgress, newItem: CategoryProgress): Boolean {
            return oldItem == newItem
        }
    }
} 