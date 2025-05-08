package com.example.myworldapp2.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.myworldapp2.KidsEncyclopediaApp
import com.example.myworldapp2.databinding.FragmentQuizDetailBinding
import com.example.myworldapp2.ui.viewmodel.QuizDetailViewModel
import com.example.myworldapp2.ui.viewmodel.QuizDetailViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class QuizDetailFragment : Fragment() {

    private var _binding: FragmentQuizDetailBinding? = null
    private val binding get() = _binding!!

    private val args: QuizDetailFragmentArgs by navArgs()
    private lateinit var viewModel: QuizDetailViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Получаем экземпляр приложения
        val app = requireActivity().application as KidsEncyclopediaApp
        
        // Создаем фабрику для ViewModel с необходимыми зависимостями
        val viewModelFactory = QuizDetailViewModelFactory(
            app.quizRepository,
            app.userQuizResultRepository,
            app.entryRepository
        )
        
        // Инициализируем ViewModel
        viewModel = ViewModelProvider(this, viewModelFactory)[QuizDetailViewModel::class.java]
        
        // Загружаем информацию о викторине
        viewModel.loadQuizDetails(args.quizId)

        setupToolbar()
        setupStartButton()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupStartButton() {
        binding.btnStartQuiz.setOnClickListener {
            viewModel.getQuizDetails()?.let { quiz ->
                if (quiz.questions.isNotEmpty()) {
                    // Переходим к экрану прохождения викторины
                    val action = QuizDetailFragmentDirections
                        .actionQuizDetailFragmentToQuizPlayFragment(args.quizId)
                    findNavController().navigate(action)
                } else {
                    Snackbar.make(
                        binding.root,
                        "Викторина не содержит вопросов",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun observeViewModel() {
        // Наблюдаем за данными викторины
        viewModel.quizWithDetails.observe(viewLifecycleOwner) { quizWithDetails ->
            quizWithDetails?.let {
                updateUI(it)
            }
        }

        // Наблюдаем за состоянием загрузки
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.quizContent.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        // Наблюдаем за сообщениями об ошибках
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty()) {
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun updateUI(quiz: com.example.myworldapp2.data.entity.QuizWithDetails) {
        binding.apply {
            // Настраиваем заголовок и описание
            tvQuizTitle.text = quiz.quiz.title
            tvQuizDescription.text = quiz.quiz.description
            
            // Показываем информацию о количестве вопросов
            val questionsCount = quiz.questions.size
            tvQuestionCount.text = "$questionsCount вопрос(ов)"
            
            // Показываем информацию о статье, если она есть
            quiz.entry?.let { entry ->
                tvRelatedArticleLabel.visibility = View.VISIBLE
                tvRelatedArticle.visibility = View.VISIBLE
                tvRelatedArticle.text = entry.title
                
                // Настраиваем кнопку для перехода к статье
                btnViewArticle.visibility = View.VISIBLE
                btnViewArticle.setOnClickListener {
                    val action = QuizDetailFragmentDirections
                        .actionQuizDetailFragmentToEntryDetailFragment(entry.id)
                    findNavController().navigate(action)
                }
            } ?: run {
                tvRelatedArticleLabel.visibility = View.GONE
                tvRelatedArticle.visibility = View.GONE
                btnViewArticle.visibility = View.GONE
            }
            
            // Показываем предыдущие результаты, если есть
            quiz.userResult?.let { result ->
                tvBestScore.visibility = View.VISIBLE
                tvBestScore.text = "Ваш лучший результат: ${result.score}/$questionsCount"
                
                // Изменяем текст кнопки начала
                btnStartQuiz.text = "Пройти викторину снова"
            } ?: run {
                tvBestScore.visibility = View.GONE
                btnStartQuiz.text = "Начать викторину"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 