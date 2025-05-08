package com.example.myworldapp2.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myworldapp2.R
import com.example.myworldapp2.data.entity.Achievement
import com.example.myworldapp2.data.model.AchievementWithProgress
import com.google.android.material.progressindicator.LinearProgressIndicator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Адаптер для отображения списка достижений
 */
class AchievementAdapter(
    private val onAchievementClicked: (AchievementWithProgress) -> Unit = {}
) : ListAdapter<AchievementWithProgress, AchievementAdapter.AchievementViewHolder>(AchievementDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_achievement, parent, false)
        return AchievementViewHolder(view)
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AchievementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgIcon: ImageView = itemView.findViewById(R.id.imgAchievementIcon)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvAchievementTitle)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvAchievementDescription)
        private val progressBar: LinearProgressIndicator = itemView.findViewById(R.id.progressAchievement)
        private val tvProgress: TextView = itemView.findViewById(R.id.tvProgressText)
        private val imgCompleted: ImageView = itemView.findViewById(R.id.imgCompleted)
        private val tvCompletedDate: TextView = itemView.findViewById(R.id.tvCompletedDate)
        private val tvPoints: TextView = itemView.findViewById(R.id.tvPoints)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onAchievementClicked(getItem(position))
                }
            }
        }

        fun bind(achievementWithProgress: AchievementWithProgress) {
            val achievement = achievementWithProgress.achievement
            val progress = achievementWithProgress.progress
            val isCompleted = achievementWithProgress.isCompleted
            val completedDate = achievementWithProgress.completedDate

            // Устанавливаем название и описание
            tvTitle.text = achievement.title
            tvDescription.text = achievement.description

            // Устанавливаем иконку
            val iconResId = itemView.context.resources.getIdentifier(
                achievement.iconName, "drawable", itemView.context.packageName
            )
            if (iconResId != 0) {
                imgIcon.setImageResource(iconResId)
            } else {
                imgIcon.setImageResource(R.drawable.ic_trophy)
            }

            // Устанавливаем очки
            tvPoints.text = itemView.context.getString(
                R.string.achievement_points, achievement.points
            )

            // Обрабатываем статус выполнения
            if (isCompleted) {
                progressBar.progress = 100
                imgCompleted.visibility = View.VISIBLE
                tvCompletedDate.visibility = View.VISIBLE
                tvCompletedDate.text = itemView.context.getString(
                    R.string.achievement_completed_date,
                    formatDate(completedDate)
                )
                tvProgress.visibility = View.GONE
            } else {
                // Вычисляем прогресс в процентах
                val progressPercent = if (achievement.targetProgress > 0) {
                    (progress * 100 / achievement.targetProgress).coerceIn(0, 100)
                } else {
                    0
                }
                
                progressBar.progress = progressPercent
                imgCompleted.visibility = View.GONE
                tvCompletedDate.visibility = View.GONE
                tvProgress.visibility = View.VISIBLE
                tvProgress.text = itemView.context.getString(
                    R.string.progress_format, progress, achievement.targetProgress
                )
            }
        }

        private fun formatDate(date: Date?): String {
            if (date == null) return ""
            val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            return dateFormat.format(date)
        }
    }
}

/**
 * DiffCallback для эффективного обновления списка достижений
 */
class AchievementDiffCallback : DiffUtil.ItemCallback<AchievementWithProgress>() {
    override fun areItemsTheSame(oldItem: AchievementWithProgress, newItem: AchievementWithProgress): Boolean {
        return oldItem.achievement.id == newItem.achievement.id
    }

    override fun areContentsTheSame(oldItem: AchievementWithProgress, newItem: AchievementWithProgress): Boolean {
        return oldItem == newItem
    }
} 