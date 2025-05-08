package com.example.myworldapp2.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myworldapp2.data.entity.QuizWithDetails
import com.example.myworldapp2.databinding.ItemQuizBinding

/**
 * Адаптер для отображения списка викторин
 */
class QuizAdapter(
    private val onQuizClicked: (QuizWithDetails) -> Unit
) : ListAdapter<QuizWithDetails, QuizAdapter.QuizViewHolder>(QuizDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val binding = ItemQuizBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return QuizViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class QuizViewHolder(
        private val binding: ItemQuizBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onQuizClicked(getItem(position))
                }
            }
        }

        fun bind(quiz: QuizWithDetails) {
            binding.apply {
                tvQuizTitle.text = quiz.quiz.title
                tvQuizDescription.text = quiz.quiz.description
                // Показываем связанную статью, если она есть
                tvEntryTitle.text = quiz.entry?.title ?: "Нет связанной статьи"
                
                // Показываем количество вопросов
                val questionsCount = quiz.questions.size
                tvQuestionCount.text = "$questionsCount вопрос(ов)"
                
                // Показываем лучший результат, если есть
                quiz.userResult?.let { result ->
                    tvBestScore.text = "Лучший результат: ${result.score}/${questionsCount}"
                } ?: run {
                    tvBestScore.text = "Еще не пройдено"
                }
            }
        }
    }

    class QuizDiffCallback : DiffUtil.ItemCallback<QuizWithDetails>() {
        override fun areItemsTheSame(oldItem: QuizWithDetails, newItem: QuizWithDetails): Boolean {
            return oldItem.quiz.id == newItem.quiz.id
        }

        override fun areContentsTheSame(oldItem: QuizWithDetails, newItem: QuizWithDetails): Boolean {
            return oldItem == newItem
        }
    }
} 