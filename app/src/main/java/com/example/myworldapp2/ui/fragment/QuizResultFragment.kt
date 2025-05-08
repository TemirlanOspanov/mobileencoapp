package com.example.myworldapp2.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.myworldapp2.KidsEncyclopediaApp
import com.example.myworldapp2.R
import com.example.myworldapp2.databinding.FragmentQuizResultBinding
import com.example.myworldapp2.ui.viewmodel.QuizResultViewModel
import com.example.myworldapp2.ui.viewmodel.QuizResultViewModelFactory
import com.google.android.material.snackbar.Snackbar

class QuizResultFragment : Fragment() {

    private var _binding: FragmentQuizResultBinding? = null
    private val binding get() = _binding!!

    private val args: QuizResultFragmentArgs by navArgs()
    private lateinit var viewModel: QuizResultViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Получаем экземпляр приложения
        val app = requireActivity().application as KidsEncyclopediaApp
        
        // Создаем фабрику для ViewModel с необходимыми зависимостями
        val viewModelFactory = QuizResultViewModelFactory(
            app.quizRepository,
            app.userQuizResultRepository,
            app.achievementRepository,
            app.userAchievementRepository
        )
        
        // Инициализируем ViewModel
        viewModel = ViewModelProvider(this, viewModelFactory)[QuizResultViewModel::class.java]
        
        // Загружаем информацию о викторине
        viewModel.loadQuizDetails(args.quizId, args.score, args.totalQuestions)

        setupToolbar()
        setupButtons()
        observeViewModel()
        updateResultUI()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            navigateToQuizList()
        }
    }

    private fun setupButtons() {
        // Кнопка для повторного прохождения викторины
        binding.btnTryAgain.setOnClickListener {
            val action = QuizResultFragmentDirections
                .actionQuizResultFragmentToQuizPlayFragment(args.quizId)
            findNavController().navigate(action)
        }
        
        // Кнопка для возврата к списку викторин
        binding.btnBackToQuizzes.setOnClickListener {
            navigateToQuizList()
        }
    }

    private fun observeViewModel() {
        // Наблюдаем за данными викторины
        viewModel.quizWithDetails.observe(viewLifecycleOwner) { quizWithDetails ->
            quizWithDetails?.let {
                binding.toolbar.title = it.quiz.title
                binding.tvQuizTitle.text = it.quiz.title
                
                // Показываем информацию о связанной статье
                it.entry?.let { entry ->
                    binding.tvRelatedArticle.text = entry.title
                    binding.btnViewArticle.visibility = View.VISIBLE
                    binding.btnViewArticle.setOnClickListener { _ ->
                        val action = QuizResultFragmentDirections
                            .actionQuizResultFragmentToEntryDetailFragment(entry.id)
                        findNavController().navigate(action)
                    }
                } ?: run {
                    binding.tvRelatedArticle.text = getString(R.string.no_related_article)
                    binding.btnViewArticle.visibility = View.GONE
                }
            }
        }
        
        // Наблюдаем за разблокированными достижениями
        viewModel.unlockedAchievements.observe(viewLifecycleOwner) { achievements ->
            if (achievements.isNotEmpty()) {
                binding.achievementsCard.visibility = View.VISIBLE
                
                // Здесь можно было бы добавить отображение достижений в RecyclerView
                // Для простоты просто показываем текстовое сообщение
                binding.tvAchievementsList.text = "Вы получили ${achievements.size} достижений!"
            } else {
                binding.achievementsCard.visibility = View.GONE
            }
        }

        // Наблюдаем за сообщениями об ошибках
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty()) {
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun updateResultUI() {
        // Показываем результат
        val score = args.score
        val totalQuestions = args.totalQuestions
        
        binding.tvScore.text = getString(R.string.your_score, score, totalQuestions)
        
        // Вычисляем процент правильных ответов
        val percentage = if (totalQuestions > 0) {
            (score * 100) / totalQuestions
        } else {
            0
        }
        
        // Настраиваем UI на основе результата
        if (percentage >= 80) {
            // Отличный результат
            binding.ivResult.setImageResource(android.R.drawable.btn_star_big_on)
            binding.tvCongratulations.text = getString(R.string.congratulations)
            binding.tvFeedback.text = "Отличная работа! Вы очень хорошо знаете эту тему."
        } else if (percentage >= 60) {
            // Хороший результат
            binding.ivResult.setImageResource(android.R.drawable.btn_star_big_on)
            binding.tvCongratulations.text = "Хороший результат!"
            binding.tvFeedback.text = "Хорошая работа, вы знаете большую часть материала."
        } else {
            // Результат требует улучшения
            binding.ivResult.setImageResource(android.R.drawable.ic_menu_help)
            binding.tvCongratulations.text = "Продолжайте учиться!"
            binding.tvFeedback.text = "Вы можете улучшить свой результат. Попробуйте еще раз после изучения материала."
        }
        
        // Настраиваем круговой индикатор
        binding.circularProgressIndicator.progress = percentage
    }

    private fun navigateToQuizList() {
        findNavController().navigate(R.id.quizListFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 