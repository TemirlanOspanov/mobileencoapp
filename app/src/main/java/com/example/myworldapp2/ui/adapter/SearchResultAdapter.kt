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
import com.example.myworldapp2.data.model.MatchType
import com.example.myworldapp2.data.model.SearchResult
import com.bumptech.glide.Glide

/**
 * Адаптер для отображения результатов поиска
 */
class SearchResultAdapter(
    private val onItemClick: (SearchResult) -> Unit
) : ListAdapter<SearchResult, SearchResultAdapter.SearchResultViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
        return SearchResultViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SearchResultViewHolder(
        itemView: View,
        private val onItemClick: (SearchResult) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        private val tvMatchType: TextView = itemView.findViewById(R.id.tv_match_type)
        private val tvHighlightedText: TextView = itemView.findViewById(R.id.tv_highlighted_text)
        private val ivEntryImage: ImageView = itemView.findViewById(R.id.iv_entry_image)

        fun bind(searchResult: SearchResult) {
            // Отображаем название статьи
            tvTitle.text = searchResult.entry.title
            
            // Отображаем тип совпадения
            tvMatchType.text = getMatchTypeText(searchResult.matchType)
            
            // Отображаем выделенный текст (если есть)
            if (searchResult.highlightedText.isNullOrEmpty()) {
                tvHighlightedText.visibility = View.GONE
            } else {
                tvHighlightedText.visibility = View.VISIBLE
                tvHighlightedText.text = searchResult.highlightedText
            }
            
            // Загружаем изображение
            if (!searchResult.entry.imageUrl.isNullOrEmpty()) {
                Glide.with(itemView)
                    .load(searchResult.entry.imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(ivEntryImage)
                ivEntryImage.visibility = View.VISIBLE
            } else {
                ivEntryImage.visibility = View.GONE
            }
            
            // Настраиваем обработчик нажатия
            itemView.setOnClickListener {
                onItemClick(searchResult)
            }
        }
        
        /**
         * Возвращает текстовое описание типа совпадения
         */
        private fun getMatchTypeText(matchType: MatchType): String {
            return when (matchType) {
                MatchType.TITLE_EXACT -> itemView.context.getString(R.string.match_title_exact)
                MatchType.TITLE_PARTIAL -> itemView.context.getString(R.string.match_title_partial)
                MatchType.CONTENT_EXACT -> itemView.context.getString(R.string.match_content_exact)
                MatchType.CONTENT_PARTIAL -> itemView.context.getString(R.string.match_content_partial)
                MatchType.TAG_MATCH -> itemView.context.getString(R.string.match_tag)
                MatchType.CATEGORY_MATCH -> itemView.context.getString(R.string.match_category)
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SearchResult>() {
            override fun areItemsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean {
                return oldItem.entry.id == newItem.entry.id
            }

            override fun areContentsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean {
                return oldItem == newItem
            }
        }
    }
} 