package com.example.myworldapp2.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myworldapp2.R
import com.example.myworldapp2.data.model.ActivityType
import com.example.myworldapp2.data.model.UserActivity
import com.example.myworldapp2.databinding.ItemRecentActivityBinding
import com.example.myworldapp2.util.TimeUtils

/**
 * Адаптер для отображения недавних активностей пользователя
 */
class UserActivityAdapter(
    private val onActivityClicked: (UserActivity) -> Unit
) : ListAdapter<UserActivity, UserActivityAdapter.UserActivityViewHolder>(UserActivityDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserActivityViewHolder {
        val binding = ItemRecentActivityBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserActivityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserActivityViewHolder, position: Int) {
        val activity = getItem(position)
        holder.bind(activity)
        holder.itemView.setOnClickListener {
            onActivityClicked(activity)
        }
    }

    class UserActivityViewHolder(
        private val binding: ItemRecentActivityBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(activity: UserActivity) {
            binding.apply {
                // Устанавливаем название активности
                tvActivityTitle.text = activity.title
                
                // Устанавливаем детали активности
                tvActivityDetails.text = activity.details
                
                // Устанавливаем время активности
                tvActivityTime.text = TimeUtils.getTimeAgo(
                    binding.root.context,
                    activity.timestamp
                )
                
                // Устанавливаем иконку в зависимости от типа активности
                val iconResId = when (activity.type) {
                    ActivityType.READ_ENTRY -> android.R.drawable.ic_menu_view
                    ActivityType.COMPLETED_QUIZ -> android.R.drawable.ic_menu_help
                    ActivityType.EARNED_ACHIEVEMENT -> android.R.drawable.btn_star_big_on
                    ActivityType.ADDED_BOOKMARK -> android.R.drawable.ic_input_get
                    ActivityType.ADDED_COMMENT -> android.R.drawable.ic_menu_edit
                }
                ivActivityIcon.setImageResource(iconResId)
            }
        }
    }

    class UserActivityDiffCallback : DiffUtil.ItemCallback<UserActivity>() {
        override fun areItemsTheSame(oldItem: UserActivity, newItem: UserActivity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: UserActivity, newItem: UserActivity): Boolean {
            return oldItem == newItem
        }
    }
} 