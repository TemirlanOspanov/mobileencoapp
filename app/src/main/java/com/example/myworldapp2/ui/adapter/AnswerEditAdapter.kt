package com.example.myworldapp2.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myworldapp2.data.entity.QuizAnswer
import com.example.myworldapp2.databinding.ItemAnswerEditBinding

/**
 * Адаптер для редактирования вариантов ответов в диалоге
 */
class AnswerEditAdapter(
    private val onDeleteAnswer: (Int) -> Unit
) : ListAdapter<QuizAnswer, AnswerEditAdapter.AnswerViewHolder>(AnswerDiffCallback()) {

    // Создаем изменяемый список для хранения ответов
    private val answersList = mutableListOf<QuizAnswer>()
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnswerViewHolder {
        val binding = ItemAnswerEditBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AnswerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AnswerViewHolder, position: Int) {
        val answer = getItem(position)
        holder.bind(answer, position)
    }
    
    override fun submitList(list: List<QuizAnswer>?) {
        list?.let {
            answersList.clear()
            answersList.addAll(it)
        }
        super.submitList(list?.toList()) // Создаем копию списка для предотвращения гонок данных
    }
    
    // Геттер для получения актуального списка ответов
    fun getAnswers(): List<QuizAnswer> = answersList.toList()

    inner class AnswerViewHolder(
        val binding: ItemAnswerEditBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(answer: QuizAnswer, position: Int) {
            // Заполняем поле текста ответа
            binding.etAnswerText.setText(answer.answerText)
            
            // Устанавливаем состояние чекбокса
            binding.checkboxCorrect.isChecked = answer.isCorrect
            
            // Добавляем слушатель для чекбокса
            binding.checkboxCorrect.setOnCheckedChangeListener { _, isChecked ->
                // Обновляем значение в списке сразу при изменении
                val currentPosition = adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION && currentPosition < answersList.size) {
                    answersList[currentPosition] = answersList[currentPosition].copy(isCorrect = isChecked)
                }
            }
            
            // Обработчик изменения текста ответа
            binding.etAnswerText.setOnFocusChangeListener { _, hasFocus ->
                val currentPosition = adapterPosition
                if (!hasFocus && currentPosition != RecyclerView.NO_POSITION && currentPosition < answersList.size) {
                    val text = binding.etAnswerText.text.toString().trim()
                    answersList[currentPosition] = answersList[currentPosition].copy(answerText = text)
                }
            }
            
            // Добавляем TextWatcher для обновления текста в реальном времени
            binding.etAnswerText.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                
                override fun afterTextChanged(s: android.text.Editable?) {
                    val currentPosition = adapterPosition
                    if (currentPosition != RecyclerView.NO_POSITION && currentPosition < answersList.size) {
                        val text = s?.toString()?.trim() ?: ""
                        answersList[currentPosition] = answersList[currentPosition].copy(answerText = text)
                    }
                }
            })
            
            // Обработчик клика на кнопку удаления
            binding.btnDeleteAnswer.setOnClickListener {
                val currentPosition = adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    onDeleteAnswer(currentPosition)
                }
            }
        }
    }

    /**
     * DiffCallback для эффективного обновления списка
     */
    class AnswerDiffCallback : DiffUtil.ItemCallback<QuizAnswer>() {
        override fun areItemsTheSame(oldItem: QuizAnswer, newItem: QuizAnswer): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: QuizAnswer, newItem: QuizAnswer): Boolean {
            return oldItem == newItem
        }
    }
} 