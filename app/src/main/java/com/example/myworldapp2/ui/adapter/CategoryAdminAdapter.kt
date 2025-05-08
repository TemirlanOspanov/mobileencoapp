package com.example.myworldapp2.ui.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myworldapp2.R
import com.example.myworldapp2.data.entity.Category

/**
 * Адаптер для отображения категорий в админ-панели
 */
class CategoryAdminAdapter(
    private val onCategoryClicked: (Category) -> Unit,
    private val onCategoryEdit: (Category) -> Unit = {},
    private val onCategoryDelete: (Category) -> Unit = {},
    private val getEntryCount: (Long) -> Int = { 0 }
) : ListAdapter<Category, CategoryAdminAdapter.CategoryViewHolder>(CategoryAdminDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_admin, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val colorView: View = itemView.findViewById(R.id.categoryColorView)
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        private val entryCountTextView: TextView = itemView.findViewById(R.id.entryCountTextView)
        private val moreButton: ImageButton = itemView.findViewById(R.id.moreButton)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onCategoryClicked(getItem(position))
                }
            }

            moreButton.setOnClickListener {
                showPopupMenu(it)
            }
        }

        private fun showPopupMenu(view: View) {
            val context = view.context
            val position = bindingAdapterPosition
            if (position == RecyclerView.NO_POSITION) return
            
            val category = getItem(position)
            val popup = PopupMenu(context, view)
            
            Log.d("CategoryAdminAdapter", "Showing popup menu for category: ${category.name}")
            
            // Inflate the menu
            val inflater = popup.menuInflater
            inflater.inflate(R.menu.menu_category_item, popup.menu)
            
            // Set click listener for menu items
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_edit -> {
                        Log.d("CategoryAdminAdapter", "Edit clicked for: ${category.name}")
                        onCategoryEdit(category)
                        true
                    }
                    R.id.action_delete -> {
                        Log.d("CategoryAdminAdapter", "Delete clicked for: ${category.name}")
                        onCategoryDelete(category)
                        true
                    }
                    else -> false
                }
            }
            
            // Show the popup menu
            popup.show()
        }

        fun bind(category: Category) {
            nameTextView.text = category.name
            descriptionTextView.text = category.description
            
            // Set background color from category
            try {
                colorView.setBackgroundColor(Color.parseColor(category.color))
            } catch (e: Exception) {
                // If color parsing fails, use default color
                colorView.setBackgroundColor(Color.parseColor("#4CAF50"))
            }
            
            // Show entry count
            val entryCount = getEntryCount(category.id)
            entryCountTextView.text = itemView.context.getString(
                R.string.entries_count, entryCount
            )
            
            // Make sure the more button is visible
            moreButton.visibility = View.VISIBLE
        }
    }
}

class CategoryAdminDiffCallback : DiffUtil.ItemCallback<Category>() {
    override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
        return oldItem == newItem
    }
} 