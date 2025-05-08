package com.example.myworldapp2.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myworldapp2.KidsEncyclopediaApp
import com.example.myworldapp2.R
import com.example.myworldapp2.data.entity.Quiz
import com.example.myworldapp2.databinding.FragmentQuizzesManagementBinding
import com.example.myworldapp2.ui.adapter.QuizAdminAdapter
import com.example.myworldapp2.ui.viewmodel.QuizzesViewModel
import com.example.myworldapp2.ui.viewmodel.QuizzesViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

/**
 * Фрагмент для управления викторинами в админ-панели
 */
class QuizzesManagementFragment : Fragment() {

    private var _binding: FragmentQuizzesManagementBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: QuizzesViewModel
    private lateinit var adapter: QuizAdminAdapter
    
    // Хранение количества вопросов в викторинах
    private val questionCounts = mutableMapOf<Long, Int>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizzesManagementBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val app = requireActivity().application as KidsEncyclopediaApp
        
        // Создаем ViewModel и Factory с передачей репозиториев
        val viewModelFactory = QuizzesViewModelFactory(
            app.quizRepository,
            app.entryRepository
        )
        viewModel = ViewModelProvider(this, viewModelFactory)[QuizzesViewModel::class.java]
        
        // Настраиваем RecyclerView с адаптером
        setupRecyclerView()
        
        // Наблюдаем за данными
        observeViewModel()
    }
    
    private fun setupRecyclerView() {
        adapter = QuizAdminAdapter(
            onQuizClicked = { quiz ->
                // При клике на викторину показываем ее содержимое
                Snackbar.make(binding.root, "Просмотр викторины: ${quiz.title}", Snackbar.LENGTH_SHORT).show()
            },
            onQuizEdit = { quiz ->
                // При клике на редактирование переходим к редактору с ID викторины
                val navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                val bundle = Bundle().apply {
                    putLong("quizId", quiz.id)
                }
                navController.navigate(R.id.editQuizFragment, bundle)
            },
            onQuizDelete = { quiz ->
                // При клике на удаление показываем диалог подтверждения
                confirmDeleteQuiz(quiz)
            },
            getQuestionCount = { quizId ->
                questionCounts[quizId] ?: 0
            }
        )
        
        binding.quizzesRecyclerView.adapter = adapter
        binding.quizzesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }
    
    private fun observeViewModel() {
        // Наблюдаем за списком викторин
        viewModel.quizzes.observe(viewLifecycleOwner) { quizzes ->
            adapter.submitList(quizzes)
            
            // Отображаем пустой вид, если нет викторин
            if (quizzes.isEmpty()) {
                binding.emptyView.visibility = View.VISIBLE
                binding.quizzesRecyclerView.visibility = View.GONE
            } else {
                binding.emptyView.visibility = View.GONE
                binding.quizzesRecyclerView.visibility = View.VISIBLE
            }
        }
        
        // Наблюдаем за состоянием загрузки
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        // Наблюдаем за количеством вопросов в викторинах
        viewModel.questionCounts.observe(viewLifecycleOwner) { counts ->
            questionCounts.clear()
            questionCounts.putAll(counts)
            adapter.notifyDataSetChanged()
        }
    }
    
    private fun confirmDeleteQuiz(quiz: Quiz) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.confirm_delete)
            .setMessage(getString(R.string.confirm_delete_quiz, quiz.title))
            .setPositiveButton(R.string.delete) { _, _ ->
                deleteQuiz(quiz)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun deleteQuiz(quiz: Quiz) {
        viewModel.viewModelScope.launch {
            try {
                viewModel.deleteQuiz(quiz)
                Snackbar.make(
                    binding.root,
                    getString(R.string.quiz_deleted, quiz.title),
                    Snackbar.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.error_delete_quiz, e.message),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Обновляем список викторин при возвращении к фрагменту
        viewModel.loadQuizzes()
        viewModel.loadQuestionCounts()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 