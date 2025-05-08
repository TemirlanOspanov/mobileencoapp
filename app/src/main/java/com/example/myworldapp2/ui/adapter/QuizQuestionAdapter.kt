package com.example.myworldapp2.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myworldapp2.data.entity.QuizQuestion
import com.example.myworldapp2.databinding.ItemQuizQuestionBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Адаптер для отображения вопросов викторины в RecyclerView
 */
class QuizQuestionAdapter(
    private val onQuestionClicked: (QuizQuestion) -> Unit,
    private val onQuestionEdit: (QuizQuestion) -> Unit,
    private val onQuestionDelete: (QuizQuestion) -> Unit
) : ListAdapter<QuizQuestion, QuizQuestionAdapter.QuestionViewHolder>(QuestionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val binding = ItemQuizQuestionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return QuestionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1)
    }

    inner class QuestionViewHolder(
        private val binding: ItemQuizQuestionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onQuestionClicked(getItem(position))
                }
            }

            binding.editButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onQuestionEdit(getItem(position))
                }
            }

            binding.deleteButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onQuestionDelete(getItem(position))
                }
            }
        }

        fun bind(question: QuizQuestion, number: Int) {
            binding.questionNumber.text = number.toString()
            binding.questionText.text = question.questionText
            
            // Установим значение по умолчанию для количества ответов
            binding.answerCount.text = "Вариантов ответа: ..."
            
            // Запускаем асинхронную загрузку количества ответов
            val questionId = question.id
            if (questionId != 0L) {
                // Используем более простой подход - через корутину из GlobalScope
                GlobalScope.launch {
                    try {
                        // Получаем информацию через QuizRepository в контексте адаптера
                        val repository = (binding.root.context.applicationContext as com.example.myworldapp2.KidsEncyclopediaApp).quizRepository
                        val answers = repository.getAnswersForQuestion(questionId)
                        
                        // Обновляем UI в основном потоке
                        withContext(kotlinx.coroutines.Dispatchers.Main) {
                            binding.answerCount.text = "Вариантов ответа: ${answers.size}"
                        }
                    } catch (e: Exception) {
                        // В случае ошибки показываем неопределенное количество
                        withContext(kotlinx.coroutines.Dispatchers.Main) {
                            binding.answerCount.text = "Вариантов ответа: ?"
                        }
                    }
                }
            }
        }
    }

    class QuestionDiffCallback : DiffUtil.ItemCallback<QuizQuestion>() {
        override fun areItemsTheSame(oldItem: QuizQuestion, newItem: QuizQuestion): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: QuizQuestion, newItem: QuizQuestion): Boolean {
            return oldItem == newItem
        }
    }
} 