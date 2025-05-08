package com.example.myworldapp2.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myworldapp2.R
import com.example.myworldapp2.data.entity.Quiz
import com.example.myworldapp2.databinding.ItemQuizAdminBinding
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Адаптер для отображения викторин в админ-панели
 */
class QuizAdminAdapter(
    private val onQuizClicked: (Quiz) -> Unit,
    private val onQuizEdit: (Quiz) -> Unit,
    private val onQuizDelete: (Quiz) -> Unit,
    private val getQuestionCount: (Long) -> Int
) : ListAdapter<Quiz, QuizAdminAdapter.QuizViewHolder>(QuizDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val binding = ItemQuizAdminBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return QuizViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        val quiz = getItem(position)
        holder.bind(quiz)
    }

    inner class QuizViewHolder(
        private val binding: ItemQuizAdminBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(quiz: Quiz) {
            binding.apply {
                quizTitle.text = quiz.title
                quizDescription.text = quiz.description
                
                // Форматирование даты создания
                val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
                quizDate.text = dateFormat.format(quiz.createdAt)
                
                // Отображение количества вопросов
                val questionCount = getQuestionCount(quiz.id)
                quizQuestionCount.text = root.context.resources.getQuantityString(
                    R.plurals.question_count, questionCount, questionCount
                )
                
                // Обработка нажатия на элемент
                root.setOnClickListener {
                    onQuizClicked(quiz)
                }
                
                // Настройка меню
                moreButton.setOnClickListener {
                    showPopupMenu(it, quiz)
                }
            }
        }
        
        private fun showPopupMenu(view: View, quiz: Quiz) {
            val popup = PopupMenu(view.context, view)
            popup.inflate(R.menu.menu_item_actions)
            
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_edit -> {
                        onQuizEdit(quiz)
                        true
                    }
                    R.id.action_delete -> {
                        onQuizDelete(quiz)
                        true
                    }
                    else -> false
                }
            }
            
            popup.show()
        }
    }
}

/**
 * DiffUtil для сравнения викторин
 */
class QuizDiffCallback : DiffUtil.ItemCallback<Quiz>() {
    override fun areItemsTheSame(oldItem: Quiz, newItem: Quiz): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Quiz, newItem: Quiz): Boolean {
        return oldItem == newItem
    }
} 