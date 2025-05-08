package com.example.myworldapp2.ui.fragment

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.myworldapp2.KidsEncyclopediaApp
import com.example.myworldapp2.R
import com.example.myworldapp2.data.entity.QuestionWithAnswers
import com.example.myworldapp2.databinding.FragmentQuizPlayBinding
import com.example.myworldapp2.ui.viewmodel.QuizPlayViewModel
import com.example.myworldapp2.ui.viewmodel.QuizPlayViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class QuizPlayFragment : Fragment() {

    private var _binding: FragmentQuizPlayBinding? = null
    private val binding get() = _binding!!

    private val args: QuizPlayFragmentArgs by navArgs()
    private lateinit var viewModel: QuizPlayViewModel
    
    private var countDownTimer: CountDownTimer? = null
    private var timeRemaining = 0L
    private val questionTimeInSeconds = 30L // 30 секунд на вопрос
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizPlayBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Получаем экземпляр приложения
        val app = requireActivity().application as KidsEncyclopediaApp
        
        // Создаем фабрику для ViewModel с необходимыми зависимостями
        val viewModelFactory = QuizPlayViewModelFactory(
            app.quizRepository,
            app.userQuizResultRepository
        )
        
        // Инициализируем ViewModel
        viewModel = ViewModelProvider(this, viewModelFactory)[QuizPlayViewModel::class.java]
        
        // Загружаем данные викторины
        viewModel.loadQuiz(args.quizId)
        
        setupToolbar()
        setupButtons()
        setupBackPressHandler()
        observeViewModel()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            showExitConfirmationDialog()
        }
    }
    
    private fun setupButtons() {
        // Кнопка подтверждения ответа
        binding.btnSubmitAnswer.setOnClickListener {
            submitAnswer()
        }
    }
    
    private fun setupBackPressHandler() {
        // Обработка нажатия кнопки "Назад"
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showExitConfirmationDialog()
                }
            }
        )
    }
    
    private fun observeViewModel() {
        // Наблюдаем за загрузкой викторины
        viewModel.quizWithDetails.observe(viewLifecycleOwner) { quizWithDetails ->
            binding.toolbar.title = quizWithDetails?.quiz?.title ?: getString(R.string.quizzes)
        }
        
        // Наблюдаем за текущим вопросом
        viewModel.currentQuestion.observe(viewLifecycleOwner) { question ->
            if (question != null) {
                showQuestion(question)
                startTimer()
            }
        }
        
        // Наблюдаем за прогрессом прохождения викторины
        viewModel.questionProgress.observe(viewLifecycleOwner) { progress ->
            binding.progressIndicator.progress = progress
        }
        
        // Наблюдаем за номером вопроса
        viewModel.questionNumber.observe(viewLifecycleOwner) { (current, total) ->
            binding.tvQuestionNumber.text = getString(R.string.question, current, total)
        }
        
        // Наблюдаем за сообщениями об ошибках
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty()) {
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            }
        }
        
        // Наблюдаем за состоянием загрузки
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.quizContent.visibility = if (isLoading) View.GONE else View.VISIBLE
        }
        
        // Наблюдаем за завершением викторины
        viewModel.quizCompleted.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                cancelTimer()
                showQuizResult(result.first, result.second)
            }
        }
    }
    
    private fun showQuestion(questionWithAnswers: QuestionWithAnswers) {
        cancelTimer()
        
        val question = questionWithAnswers.question
        val answers = questionWithAnswers.answers
        
        // Показываем текст вопроса
        binding.tvQuestionText.text = question.questionText
        
        // Очищаем предыдущие варианты ответов
        binding.radioGroup.removeAllViews()
        
        // Добавляем варианты ответов
        answers.forEachIndexed { index, answer ->
            val radioButton = RadioButton(context).apply {
                id = View.generateViewId()
                text = answer.answerText
                tag = answer.id
            }
            binding.radioGroup.addView(radioButton)
        }
        
        // Сбрасываем выбор
        binding.radioGroup.clearCheck()
        
        // Обновляем кнопку
        binding.btnSubmitAnswer.isEnabled = false
        
        // Добавляем слушатель для разблокировки кнопки при выборе ответа
        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            binding.btnSubmitAnswer.isEnabled = checkedId != -1
        }
        
        // Запускаем таймер
        startTimer()
    }
    
    private fun submitAnswer() {
        val selectedId = binding.radioGroup.checkedRadioButtonId
        if (selectedId != -1) {
            val radioButton = binding.root.findViewById<RadioButton>(selectedId)
            val answerId = radioButton.tag as Long
            
            // Передаем ID выбранного ответа в ViewModel
            viewModel.submitAnswer(answerId)
        }
    }
    
    private fun startTimer() {
        // Отменяем предыдущий таймер, если он существует
        cancelTimer()
        
        // Устанавливаем начальное время
        timeRemaining = questionTimeInSeconds * 1000
        
        // Обновляем UI таймера
        binding.tvTimeRemaining.text = getString(R.string.time_remaining, questionTimeInSeconds)
        binding.tvTimeRemaining.visibility = View.VISIBLE
        
        // Создаем и запускаем новый таймер
        countDownTimer = object : CountDownTimer(timeRemaining, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemaining = millisUntilFinished
                val secondsRemaining = millisUntilFinished / 1000
                binding.tvTimeRemaining.text = getString(R.string.time_remaining, secondsRemaining)
            }
            
            override fun onFinish() {
                // Если время вышло, автоматически переходим к следующему вопросу
                viewModel.timeUp()
            }
        }.start()
    }
    
    private fun cancelTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
    }
    
    private fun showExitConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Прервать викторину?")
            .setMessage("Ваш прогресс в этой викторине будет утерян.")
            .setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Выйти") { _, _ -> findNavController().navigateUp() }
            .show()
    }
    
    private fun showQuizResult(score: Int, totalQuestions: Int) {
        val action = QuizPlayFragmentDirections
            .actionQuizPlayFragmentToQuizResultFragment(args.quizId, score, totalQuestions)
        findNavController().navigate(action)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        cancelTimer()
        _binding = null
    }
} 