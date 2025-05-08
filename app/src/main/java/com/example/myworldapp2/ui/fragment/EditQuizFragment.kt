package com.example.myworldapp2.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.myworldapp2.KidsEncyclopediaApp
import com.example.myworldapp2.R
import com.example.myworldapp2.data.entity.Entry
import com.example.myworldapp2.data.entity.Quiz
import com.example.myworldapp2.databinding.FragmentEditQuizBinding
import com.example.myworldapp2.ui.viewmodel.EditQuizViewModel
import com.example.myworldapp2.ui.viewmodel.EditQuizViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.Date

/**
 * Фрагмент для создания или редактирования викторины
 */
class EditQuizFragment : Fragment() {

    private var _binding: FragmentEditQuizBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: EditQuizViewModel
    
    // ID викторины при редактировании
    private var quizId: Long = 0
    
    // Список статей для выбора
    private val entries = mutableListOf<Entry>()
    
    // Текущая редактируемая викторина
    private var currentQuiz: Quiz? = null

    override fun onCreateView(
        inflater: LayoutInflater, 
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditQuizBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Получаем ID викторины из аргументов
        arguments?.let { args ->
            quizId = args.getLong("quizId", 0L)
        }
        
        // Получаем приложение для доступа к репозиториям
        val app = requireActivity().application as KidsEncyclopediaApp
        
        // Создаем ViewModel с фабрикой и передаем репозитории
        val viewModelFactory = EditQuizViewModelFactory(app.quizRepository, app.entryRepository)
        viewModel = ViewModelProvider(this, viewModelFactory)[EditQuizViewModel::class.java]
        
        // Настраиваем UI на основе режима (создание или редактирование)
        setupUI()
        
        // Загружаем данные
        loadData()
    }
    
    private fun setupUI() {
        // Настраиваем заголовок и кнопку сохранения
        val isEditMode = quizId > 0
        
        binding.toolbarTitle.text = if (isEditMode) {
            getString(R.string.edit_quiz)
        } else {
            getString(R.string.create_quiz)
        }
        
        // Обработчик кнопки "Назад"
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
        
        // Обработчик кнопки "Сохранить"
        binding.saveButton.setOnClickListener {
            saveQuiz()
        }
        
        // Наблюдаем за состоянием сохранения
        viewModel.saveStatus.observe(viewLifecycleOwner) { success ->
            if (success) {
                // Показываем уведомление об успешном сохранении
                Snackbar.make(
                    binding.root,
                    if (isEditMode) R.string.quiz_updated else R.string.quiz_created,
                    Snackbar.LENGTH_SHORT
                ).show()
                
                // Получаем ID сохраненной викторины
                val savedQuizId = viewModel.savedQuizId.value ?: quizId
                if (savedQuizId > 0) {
                    // Переходим к редактированию вопросов викторины
                    val bundle = Bundle().apply {
                        putLong("quizId", savedQuizId)
                    }
                    findNavController().navigate(
                        R.id.action_editQuizFragment_to_quizQuestionsFragment,
                        bundle
                    )
                } else {
                    findNavController().navigateUp()
                }
            }
        }
        
        // Наблюдаем за сообщениями об ошибках
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMsg ->
            if (errorMsg.isNotEmpty()) {
                Snackbar.make(binding.root, errorMsg, Snackbar.LENGTH_LONG).show()
            }
        }
    }
    
    private fun loadData() {
        // Используем viewLifecycleOwner.lifecycleScope, чтобы корутина отменялась при уничтожении view
        viewLifecycleOwner.lifecycleScope.launch {
            // Показываем прогресс только если binding доступен
            _binding?.progressBar?.visibility = View.VISIBLE
            
            try {
                // Загружаем список статей для выбора
                val entryList = viewModel.loadEntries()
                entries.clear()
                entries.addAll(entryList)
                
                // Проверяем, что binding не уничтожен перед обновлением UI
                _binding?.let {
                    setupEntriesDropdown(entries)
                }
                
                // Если это режим редактирования, загружаем данные викторины
                if (quizId > 0) {
                    val quiz = viewModel.getQuizById(quizId)
                    if (quiz != null) {
                        currentQuiz = quiz
                        
                        // Проверяем, что binding не уничтожен перед обновлением UI
                        _binding?.let {
                            populateForm(quiz)
                        }
                    } else {
                        // Проверяем, что binding не уничтожен перед отображением ошибки
                        _binding?.let {
                            showError(getString(R.string.quiz_not_found))
                        }
                    }
                }
            } catch (e: Exception) {
                // Проверяем, что binding не уничтожен перед отображением ошибки
                _binding?.let {
                    showError(getString(R.string.error_loading_data, e.message))
                }
            } finally {
                // Проверяем, что binding не уничтожен перед обновлением UI
                _binding?.progressBar?.visibility = View.GONE
            }
        }
    }
    
    private fun setupEntriesDropdown(entries: List<Entry>) {
        // Создаем адаптер для выпадающего списка статей
        val entryNames = entries.map { it.title }.toTypedArray()
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            entryNames
        )
        
        binding.entryDropdown.setAdapter(adapter)
        
        // Если есть текущая викторина, выбираем соответствующую статью
        currentQuiz?.let { quiz ->
            val entryIndex = entries.indexOfFirst { it.id == quiz.entryId }
            if (entryIndex >= 0) {
                binding.entryDropdown.setText(entryNames[entryIndex], false)
            }
        }
    }
    
    private fun populateForm(quiz: Quiz) {
        binding.titleEditText.setText(quiz.title)
        binding.descriptionEditText.setText(quiz.description)
        
        // Выбираем связанную статью в выпадающем списке
        if (entries.isNotEmpty()) {
            val entryIndex = entries.indexOfFirst { it.id == quiz.entryId }
            if (entryIndex >= 0) {
                binding.entryDropdown.setText(entries[entryIndex].title, false)
            }
        }
    }
    
    private fun saveQuiz() {
        // Проверяем валидность полей
        val title = binding.titleEditText.text.toString().trim()
        val description = binding.descriptionEditText.text.toString().trim()
        val selectedEntryName = binding.entryDropdown.text.toString().trim()
        
        // Проверяем, что все поля заполнены
        if (title.isEmpty()) {
            binding.titleLayout.error = getString(R.string.error_empty_field)
            return
        } else {
            binding.titleLayout.error = null
        }
        
        if (description.isEmpty()) {
            binding.descriptionLayout.error = getString(R.string.error_empty_field)
            return
        } else {
            binding.descriptionLayout.error = null
        }
        
        if (selectedEntryName.isEmpty()) {
            binding.entryLayout.error = getString(R.string.error_select_entry)
            return
        } else {
            binding.entryLayout.error = null
        }
        
        // Находим выбранную статью
        val selectedEntry = entries.find { it.title == selectedEntryName }
        if (selectedEntry == null) {
            binding.entryLayout.error = getString(R.string.error_entry_not_found)
            return
        }
        
        // Создаем или обновляем объект викторины
        val quiz = currentQuiz?.copy(
            title = title,
            description = description,
            entryId = selectedEntry.id
        ) ?: Quiz(
            title = title,
            description = description,
            entryId = selectedEntry.id,
            createdAt = Date()
        )
        
        // Сохраняем викторину
        viewModel.saveQuiz(quiz)
    }
    
    private fun showError(message: String) {
        _binding?.let {
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 