package com.example.myworldapp2.ui.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.myworldapp2.KidsEncyclopediaApp
import com.example.myworldapp2.R
import com.example.myworldapp2.data.entity.Quiz
import com.example.myworldapp2.data.entity.QuizQuestion
import com.example.myworldapp2.data.entity.QuizAnswer
import com.example.myworldapp2.databinding.FragmentQuizQuestionsBinding
import com.example.myworldapp2.databinding.FragmentQuestionsTabBinding
import com.example.myworldapp2.databinding.FragmentSettingsTabBinding
import com.example.myworldapp2.ui.adapter.QuizQuestionAdapter
import com.example.myworldapp2.ui.adapter.AnswerEditAdapter
import com.example.myworldapp2.ui.viewmodel.QuizQuestionsViewModel
import com.example.myworldapp2.ui.viewmodel.QuizQuestionsViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.Date
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle

/**
 * Фрагмент для управления вопросами викторины
 */
class QuizQuestionsFragment : Fragment() {

    private var _binding: FragmentQuizQuestionsBinding? = null
    private val binding get() = _binding!!
    
    // Делаем binding доступным через свойство для вложенных фрагментов
    val fragmentBinding get() = _binding!!
    
    // Делаем ViewModel публичной
    lateinit var viewModel: QuizQuestionsViewModel
    private lateinit var adapter: QuizQuestionAdapter
    
    // ID викторины
    var quizId: Long = 0
    
    // Текущая викторина
    private var currentQuiz: Quiz? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizQuestionsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Получаем ID викторины из аргументов
        arguments?.let { args ->
            quizId = args.getLong("quizId", 0L)
        }
        
        if (quizId == 0L) {
            Snackbar.make(binding.root, R.string.error_loading_data, Snackbar.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }
        
        // Получаем приложение для доступа к репозиториям
        val app = requireActivity().application as KidsEncyclopediaApp
        
        // Создаем ViewModel с фабрикой и передаем репозитории
        val viewModelFactory = QuizQuestionsViewModelFactory(app.quizRepository)
        viewModel = ViewModelProvider(this, viewModelFactory)[QuizQuestionsViewModel::class.java]
        
        // Настраиваем интерфейс
        setupUI()
        
        // Добавляем меню в ActionBar
        setupMenu()
        
        // Загружаем данные
        loadData()
    }
    
    private fun setupUI() {
        // Скрываем собственный тулбар фрагмента, так как используем тулбар Activity
        binding.toolbar.visibility = View.GONE
        
        // Настраиваем TabLayout и ViewPager если они используются
        setupTabs()
        
        // Кнопка добавления вопросов
        binding.fabAddQuestion.visibility = View.VISIBLE
        binding.fabAddQuestion.setOnClickListener {
            createNewQuestion()
        }
        
        // Наблюдаем за сообщениями об ошибках
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMsg ->
            if (errorMsg.isNotEmpty()) {
                Snackbar.make(binding.root, errorMsg, Snackbar.LENGTH_LONG).show()
            }
        }
    }
    
    private fun setupTabs() {
        // Настраиваем TabLayout и ViewPager2
        val tabsAdapter = QuizTabsAdapter(this)
        binding.viewPager.adapter = tabsAdapter

        // Связываем TabLayout с ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.questions)
                1 -> getString(R.string.settings)
                else -> null
            }
        }.attach()
    }
    
    private fun loadData() {
        // Используем viewLifecycleOwner.lifecycleScope чтобы корутина отменялась при уничтожении view
        viewLifecycleOwner.lifecycleScope.launch {
            // Показываем прогресс только если binding доступен
            _binding?.progressBar?.visibility = View.VISIBLE
            
            try {
                // Загружаем викторину
                viewModel.loadQuiz(quizId)
                
                // Загружаем вопросы
                viewModel.loadQuestions(quizId)
            } catch (e: Exception) {
                // Проверяем, что binding не уничтожен перед отображением ошибки
                _binding?.let {
                    Snackbar.make(it.root, getString(R.string.error_loading_data, e.message), Snackbar.LENGTH_LONG).show()
                }
            } finally {
                // Проверяем, что binding не уничтожен перед обновлением UI
                _binding?.progressBar?.visibility = View.GONE
            }
        }
        
        // Наблюдаем за текущей викториной
        viewModel.quiz.observe(viewLifecycleOwner) { quiz ->
            if (quiz != null) {
                currentQuiz = quiz
                // Обновляем информацию только если binding не уничтожен
                _binding?.let {
                    updateQuizInfo(quiz)
                }
            }
        }
        
        // Наблюдаем за вопросами для обновления счетчика
        viewModel.questions.observe(viewLifecycleOwner) { questions ->
            updateQuestionCount(questions.size)
        }
    }
    
    private fun updateQuizInfo(quiz: Quiz) {
        binding.quizTitle.text = quiz.title
        binding.quizDescription.text = quiz.description
        
        // Обновляем заголовок в Toolbar только если нужно
        // binding.toolbarTitle.text = quiz.title
    }
    
    private fun updateQuestionCount(count: Int) {
        binding.questionCount.text = getString(R.string.question_count, count)
    }
    
    // Делаем методы работы с вопросами публичными
    fun editQuestion(question: QuizQuestion) {
        // Создаем диалоговое окно с использованием нового макета
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Редактирование вопроса")
            .setView(R.layout.dialog_edit_question)
            .setPositiveButton("Сохранить", null) // Мы настроим обработчик позже
            .setNegativeButton("Отмена", null)
            .create()
        
        dialog.show()
        
        // Получаем ссылки на элементы диалога
        val etQuestionText = dialog.findViewById<EditText>(R.id.et_question_text)
        val rvAnswers = dialog.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_answers)
        val btnAddAnswer = dialog.findViewById<Button>(R.id.btn_add_answer)
        
        // Заполняем поле текста вопроса
        etQuestionText?.setText(question.questionText)
        
        // Создаем временный список вариантов ответов
        val answersList = mutableListOf<QuizAnswer>()
        
        // Настраиваем RecyclerView с LinearLayoutManager
        val layoutManager = LinearLayoutManager(requireContext())
        rvAnswers?.layoutManager = layoutManager
        
        // Объявляем переменную до инициализации, чтобы избежать самоссылки
        lateinit var answersAdapter: AnswerEditAdapter
        
        // Создаем адаптер для отображения вариантов ответов
        answersAdapter = AnswerEditAdapter(
            onDeleteAnswer = { position ->
                // Проверяем, что позиция действительна перед удалением
                if (position >= 0 && position < answersList.size) {
                    answersList.removeAt(position)
                    // Создаем новый список для обновления адаптера
                    val newList = answersList.toList()
                    answersAdapter.submitList(newList)
                }
            }
        )
        
        // Настраиваем RecyclerView
        rvAnswers?.adapter = answersAdapter
        
        // Загружаем существующие варианты ответов из базы данных
        viewLifecycleOwner.lifecycleScope.launch {
            val answers = viewModel.getAnswersForQuestion(question.id)
            answersList.clear()
            answersList.addAll(answers)
            answersAdapter.submitList(answersList.toList())
        }
        
        // Настраиваем кнопку добавления варианта ответа
        btnAddAnswer?.setOnClickListener {
            // Создаем новый пустой вариант ответа
            val newAnswer = QuizAnswer(
                questionId = question.id,
                answerText = "",
                isCorrect = false
            )
            
            // Добавляем в список и обновляем адаптер
            answersList.add(newAnswer)
            // Создаем новый список для обновления адаптера
            val newList = answersList.toList()
            answersAdapter.submitList(newList)
        }
        
        // Настраиваем обработчик кнопки "Сохранить"
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val questionText = etQuestionText?.text?.toString()?.trim() ?: ""
            
            // Проверяем, что текст вопроса не пустой
            if (questionText.isEmpty()) {
                Snackbar.make(dialog.window?.decorView!!, "Текст вопроса не может быть пустым", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Проверяем, что есть хотя бы один вариант ответа
            if (answersList.isEmpty()) {
                Snackbar.make(dialog.window?.decorView!!, "Добавьте хотя бы один вариант ответа", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Получаем обновленный список ответов из адаптера
            val updatedAnswers = (rvAnswers?.adapter as? AnswerEditAdapter)?.getAnswers() ?: answersList
            
            // Проверяем, что хотя бы один вариант ответа отмечен как правильный
            if (updatedAnswers.none { it.isCorrect }) {
                Snackbar.make(dialog.window?.decorView!!, "Отметьте хотя бы один правильный ответ", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Проверяем, что все варианты ответов имеют непустой текст
            if (updatedAnswers.any { it.answerText.isEmpty() }) {
                Snackbar.make(dialog.window?.decorView!!, "Текст варианта ответа не может быть пустым", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Обновляем вопрос и варианты ответов в базе данных
            val updatedQuestion = question.copy(questionText = questionText)
            viewModel.updateQuestionWithAnswers(updatedQuestion, updatedAnswers)
            
            // Показываем сообщение об успешном обновлении
            _binding?.let {
                Snackbar.make(it.root, "Вопрос и варианты ответов обновлены", Snackbar.LENGTH_SHORT).show()
            }
            
            // Закрываем диалог
            dialog.dismiss()
        }
    }
    
    fun confirmDeleteQuestion(question: QuizQuestion) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.confirm_delete)
            .setMessage("Вы уверены, что хотите удалить этот вопрос?")
            .setPositiveButton(R.string.delete) { _, _ ->
                deleteQuestion(question)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun deleteQuestion(question: QuizQuestion) {
        // Удаляем вопрос
        viewModel.deleteQuestion(question)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    // Добавляем метод для подтверждения удаления викторины
    fun confirmDeleteQuiz(quiz: Quiz) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.confirm_delete)
            .setMessage(getString(R.string.confirm_delete_quiz, quiz.title))
            .setPositiveButton(R.string.delete) { _, _ ->
                deleteQuiz(quiz)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    // Добавляем метод для удаления викторины
    private fun deleteQuiz(quiz: Quiz) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.deleteQuiz(quiz)
                
                // Возвращаемся обратно
                findNavController().navigateUp()
                
                // Показываем сообщение
                Snackbar.make(
                    requireView(), 
                    getString(R.string.quiz_deleted, quiz.title), 
                    Snackbar.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Snackbar.make(
                    requireView(), 
                    getString(R.string.error_delete_quiz, e.message), 
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setupMenu() {
        // Добавляем MenuProvider для создания меню с кнопкой сохранения
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_quiz_questions, menu)
            }
            
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_save -> {
                        saveQuiz()
                        true
                    }
                    android.R.id.home -> {
                        // Обработка нажатия кнопки "Назад"
                        findNavController().navigateUp()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun saveQuiz() {
        // Получаем текущую викторину
        val quiz = viewModel.quiz.value
        if (quiz != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    // Обновляем викторину в базе данных
                    viewModel.updateQuiz(quiz)
                    
                    // Показываем сообщение об успешном сохранении
                    Snackbar.make(
                        binding.root,
                        getString(R.string.quiz_updated),
                        Snackbar.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    Snackbar.make(
                        binding.root,
                        "Ошибка при сохранении: ${e.message}",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    // Добавляем метод для создания нового вопроса
    private fun createNewQuestion() {
        // Проверяем, что викторина загружена
        val quiz = currentQuiz ?: run {
            Snackbar.make(binding.root, "Викторина не загружена", Snackbar.LENGTH_SHORT).show()
            return
        }
        
        // Создаем диалоговое окно с использованием того же макета, что и для редактирования
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Новый вопрос")
            .setView(R.layout.dialog_edit_question)
            .setPositiveButton("Сохранить", null) // Настроим обработчик позже
            .setNegativeButton("Отмена", null)
            .create()
        
        dialog.show()
        
        // Получаем ссылки на элементы диалога
        val etQuestionText = dialog.findViewById<EditText>(R.id.et_question_text)
        val rvAnswers = dialog.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_answers)
        val btnAddAnswer = dialog.findViewById<Button>(R.id.btn_add_answer)
        
        // Создаем временный список вариантов ответов
        val answersList = mutableListOf<QuizAnswer>()
        
        // Настраиваем RecyclerView с LinearLayoutManager
        val layoutManager = LinearLayoutManager(requireContext())
        rvAnswers?.layoutManager = layoutManager
        
        // Объявляем переменную до инициализации, чтобы избежать самоссылки
        lateinit var answersAdapter: AnswerEditAdapter
        
        // Создаем адаптер для отображения вариантов ответов
        answersAdapter = AnswerEditAdapter(
            onDeleteAnswer = { position ->
                // Проверяем, что позиция действительна перед удалением
                if (position >= 0 && position < answersList.size) {
                    answersList.removeAt(position)
                    // Создаем новый список для обновления адаптера
                    val newList = answersList.toList()
                    answersAdapter.submitList(newList)
                }
            }
        )
        
        // Настраиваем RecyclerView
        rvAnswers?.adapter = answersAdapter
        
        // Настраиваем кнопку добавления варианта ответа
        btnAddAnswer?.setOnClickListener {
            // Создаем новый пустой вариант ответа (questionId будет установлен позже)
            val newAnswer = QuizAnswer(
                questionId = 0, // Временный ID, будет заменен после сохранения вопроса
                answerText = "",
                isCorrect = false
            )
            
            // Добавляем в список и обновляем адаптер
            answersList.add(newAnswer)
            // Создаем новый список для обновления адаптера
            val newList = answersList.toList()
            answersAdapter.submitList(newList)
        }
        
        // Настраиваем обработчик кнопки "Сохранить"
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val questionText = etQuestionText?.text?.toString()?.trim() ?: ""
            
            // Проверяем, что текст вопроса не пустой
            if (questionText.isEmpty()) {
                Snackbar.make(dialog.window?.decorView!!, "Текст вопроса не может быть пустым", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Проверяем, что есть хотя бы один вариант ответа
            if (answersList.isEmpty()) {
                Snackbar.make(dialog.window?.decorView!!, "Добавьте хотя бы один вариант ответа", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Получаем обновленный список ответов из адаптера
            val updatedAnswers = (rvAnswers?.adapter as? AnswerEditAdapter)?.getAnswers() ?: answersList
            
            // Проверяем, что хотя бы один вариант ответа отмечен как правильный
            if (updatedAnswers.none { it.isCorrect }) {
                Snackbar.make(dialog.window?.decorView!!, "Отметьте хотя бы один правильный ответ", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Проверяем, что все варианты ответов имеют непустой текст
            if (updatedAnswers.any { it.answerText.isEmpty() }) {
                Snackbar.make(dialog.window?.decorView!!, "Текст варианта ответа не может быть пустым", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Создаем новый вопрос
            val newQuestion = QuizQuestion(
                quizId = quiz.id,
                questionText = questionText
            )
            
            // Сохраняем вопрос и его ответы
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    // Сохраняем вопрос
                    val questionId = viewModel.addQuestionAndGetId(newQuestion)
                    
                    // Сохраняем варианты ответов
                    for (answer in updatedAnswers) {
                        val newAnswer = answer.copy(questionId = questionId)
                        viewModel.addAnswer(newAnswer)
                    }
                    
                    // Показываем сообщение
                    _binding?.let {
                        Snackbar.make(it.root, "Вопрос добавлен", Snackbar.LENGTH_SHORT).show()
                    }
                    
                } catch (e: Exception) {
                    _binding?.let {
                        Snackbar.make(it.root, "Ошибка добавления вопроса: ${e.message}", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
            
            // Закрываем диалог
            dialog.dismiss()
        }
    }
}

// Добавим необходимый адаптер для ViewPager2
class QuizTabsAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> QuestionsTabFragment.newInstance()
            1 -> SettingsTabFragment.newInstance()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}

// Фрагмент для вкладки "Вопросы"
class QuestionsTabFragment : Fragment() {
    private var _binding: FragmentQuestionsTabBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var adapter: QuizQuestionAdapter
    private lateinit var viewModel: QuizQuestionsViewModel
    
    companion object {
        fun newInstance() = QuestionsTabFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuestionsTabBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Получаем ViewModel из родительского фрагмента
        viewModel = (parentFragment as QuizQuestionsFragment).viewModel
        
        // Настраиваем RecyclerView
        setupRecyclerView()
        
        // Настраиваем FAB для добавления вопросов
        val parentFab = (parentFragment as QuizQuestionsFragment).fragmentBinding.fabAddQuestion
        parentFab.setOnClickListener {
            addNewQuestion()
        }
        
        // Наблюдаем за вопросами
        viewModel.questions.observe(viewLifecycleOwner) { questions ->
            if (questions.isEmpty()) {
                binding.emptyState.visibility = View.VISIBLE
                binding.questionsRecyclerView.visibility = View.GONE
            } else {
                binding.emptyState.visibility = View.GONE
                binding.questionsRecyclerView.visibility = View.VISIBLE
                adapter.submitList(questions)
            }
        }
    }
    
    private fun setupRecyclerView() {
        adapter = QuizQuestionAdapter(
            onQuestionClicked = { question ->
                (parentFragment as QuizQuestionsFragment).editQuestion(question)
            },
            onQuestionEdit = { question ->
                (parentFragment as QuizQuestionsFragment).editQuestion(question)
            },
            onQuestionDelete = { question ->
                (parentFragment as QuizQuestionsFragment).confirmDeleteQuestion(question)
            }
        )
        
        binding.questionsRecyclerView.adapter = adapter
        binding.questionsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }
    
    private fun addNewQuestion() {
        // Создаем поле ввода для текста вопроса
        val input = EditText(requireContext())
        input.hint = "Введите текст вопроса"
        
        // Создаем диалоговое окно
        AlertDialog.Builder(requireContext())
            .setTitle("Новый вопрос")
            .setView(input)
            .setPositiveButton("Добавить") { _, _ ->
                val questionText = input.text.toString().trim()
                if (questionText.isNotEmpty()) {
                    // Получаем ID викторины
                    val quizId = (parentFragment as QuizQuestionsFragment).quizId
                    
                    // Создаем новый вопрос
                    val newQuestion = QuizQuestion(
                        quizId = quizId,
                        questionText = questionText,
                        createdAt = Date()
                    )
                    
                    // Сохраняем вопрос используя non-suspend метод
                    viewModel.addQuestion(newQuestion)
                    
                    // Показываем сообщение
                    _binding?.let {
                        Snackbar.make(it.root, "Вопрос добавлен", Snackbar.LENGTH_SHORT).show()
                    }
                } else {
                    _binding?.let {
                        Snackbar.make(it.root, "Текст вопроса не может быть пустым", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Фрагмент для вкладки "Настройки"
class SettingsTabFragment : Fragment() {
    private var _binding: FragmentSettingsTabBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: QuizQuestionsViewModel
    
    companion object {
        fun newInstance() = SettingsTabFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsTabBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Получаем ViewModel из родительского фрагмента
        viewModel = (parentFragment as QuizQuestionsFragment).viewModel
        
        // Настраиваем кнопку удаления викторины
        binding.btnDeleteQuiz.setOnClickListener {
            val quiz = viewModel.quiz.value
            if (quiz != null) {
                (parentFragment as QuizQuestionsFragment).confirmDeleteQuiz(quiz)
            }
        }
        
        // Настраиваем переключатели
        setupSwitches()
    }
    
    private fun setupSwitches() {
        // Здесь можно настроить работу переключателей
        // Например:
        binding.switchRandomOrder.setOnCheckedChangeListener { _, isChecked ->
            // Сохраняем настройку в ViewModel или базу данных
        }
        
        binding.switchShowAnswers.setOnCheckedChangeListener { _, isChecked ->
            // Сохраняем настройку в ViewModel или базу данных
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 