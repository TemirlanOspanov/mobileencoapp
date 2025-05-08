package com.example.myworldapp2.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myworldapp2.R
import com.example.myworldapp2.data.entity.Entry
import com.example.myworldapp2.databinding.ItemEntryAdminBinding
import android.util.Log

/**
 * Адаптер для отображения статей в RecyclerView
 */
class EntryAdapter(
    private val onEntryClicked: (Entry) -> Unit,
    private val onEntryEdit: (Entry) -> Unit = {},
    private val onEntryDelete: (Entry) -> Unit = {}
) : ListAdapter<Entry, EntryAdapter.EntryViewHolder>(EntryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryViewHolder {
        val binding = ItemEntryAdminBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EntryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * ViewHolder для отображения элемента статьи
     */
    inner class EntryViewHolder(
        private val binding: ItemEntryAdminBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onEntryClicked(getItem(position))
                }
            }

            binding.moreButton.setOnClickListener {
                showPopupMenu(it)
            }
        }

        private fun showPopupMenu(view: android.view.View) {
            val context = view.context
            val position = bindingAdapterPosition
            if (position == RecyclerView.NO_POSITION) return
            
            val entry = getItem(position)
            val popup = PopupMenu(context, view)
            
            Log.d("EntryAdapter", "Showing popup menu for entry: ${entry.title}")
            
            // Inflate the menu
            val inflater = popup.menuInflater
            inflater.inflate(R.menu.menu_entry_item, popup.menu)
            
            // Set click listener for menu items
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_edit -> {
                        Log.d("EntryAdapter", "Edit clicked for: ${entry.title}")
                        onEntryEdit(entry)
                        true
                    }
                    R.id.action_delete -> {
                        Log.d("EntryAdapter", "Delete clicked for: ${entry.title}")
                        onEntryDelete(entry)
                        true
                    }
                    else -> false
                }
            }
            
            // Show the popup menu
            popup.show()
        }

        fun bind(entry: Entry) {
            binding.titleTextView.text = entry.title
            binding.summaryTextView.text = entry.content.take(100) + if (entry.content.length > 100) "..." else ""
            binding.categoryTextView.text = "Категория: ${entry.categoryId}" // TODO: Получить название категории
            
            // Make sure the more button is visible and clickable
            binding.moreButton.visibility = android.view.View.VISIBLE
        }
    }
}

/**
 * DiffCallback для эффективного обновления списка статей
 */
class EntryDiffCallback : DiffUtil.ItemCallback<Entry>() {
    override fun areItemsTheSame(oldItem: Entry, newItem: Entry): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Entry, newItem: Entry): Boolean {
        return oldItem == newItem
    }
} 