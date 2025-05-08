package com.example.myworldapp2.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myworldapp2.data.entity.Tag
import com.example.myworldapp2.databinding.ItemTagBinding

class TagAdapter : ListAdapter<Tag, TagAdapter.TagViewHolder>(TagDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val binding = ItemTagBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TagViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TagViewHolder(private val binding: ItemTagBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(tag: Tag) {
            binding.tvTagName.text = tag.name
            
            // Set the tag background color from the tag entity
            try {
                val color = Color.parseColor(tag.color)
                val drawable = binding.tvTagName.background.mutate()
                drawable.setTint(color)
                binding.tvTagName.background = drawable
            } catch (e: IllegalArgumentException) {
                // If color parsing fails, use default color
                e.printStackTrace()
            }
        }
    }

    private class TagDiffCallback : DiffUtil.ItemCallback<Tag>() {
        override fun areItemsTheSame(oldItem: Tag, newItem: Tag): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Tag, newItem: Tag): Boolean {
            return oldItem == newItem
        }
    }
} 