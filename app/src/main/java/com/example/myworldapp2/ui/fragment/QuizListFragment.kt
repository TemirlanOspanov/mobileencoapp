package com.example.myworldapp2.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myworldapp2.KidsEncyclopediaApp
import com.example.myworldapp2.data.entity.Quiz
import com.example.myworldapp2.databinding.FragmentQuizListBinding
import com.example.myworldapp2.databinding.ItemQuizBinding
import com.example.myworldapp2.ui.viewmodel.QuizListViewModel
import com.example.myworldapp2.ui.viewmodel.QuizListViewModelFactory
import com.google.android.material.snackbar.Snackbar

class QuizListFragment : Fragment() {

    private var _binding: FragmentQuizListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: QuizListViewModel
    private lateinit var quizAdapter: SimpleQuizAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Получаем экземпляр приложения
        val app = requireActivity().application as KidsEncyclopediaApp
        
        // Создаем фабрику для ViewModel с необходимыми зависимостями
        val viewModelFactory = QuizListViewModelFactory(
            app.quizRepository,
            app.userQuizResultRepository,
            app.entryRepository
        )
        
        // Инициализируем ViewModel
        viewModel = ViewModelProvider(this, viewModelFactory)[QuizListViewModel::class.java]

        setupToolbar()
        setupRecyclerView()
        setupEmptyView()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        // Настраиваем адаптер для отображения викторин
        quizAdapter = SimpleQuizAdapter { quiz ->
            // Навигация к деталям викторины
            val action = QuizListFragmentDirections
                .actionQuizListFragmentToQuizDetailFragment(quiz.id)
            findNavController().navigate(action)
        }

        // Настраиваем RecyclerView
        binding.rvQuizzes.apply {
            adapter = quizAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        }
    }

    private fun setupEmptyView() {
        // Настраиваем кнопку на пустом экране
        binding.btnExplore.setOnClickListener {
            findNavController().navigate(QuizListFragmentDirections.actionQuizListFragmentToCategoriesFragment())
        }
    }

    private fun observeViewModel() {
        // Наблюдаем за списком викторин
        viewModel.quizzes.observe(viewLifecycleOwner) { quizzes ->
            // Дополнительная защита от дублирования - используем distinctBy
            val distinctQuizzes = quizzes.distinctBy { it.id }
            
            if (distinctQuizzes.isEmpty()) {
                binding.emptyView.visibility = View.VISIBLE
                binding.rvQuizzes.visibility = View.GONE
            } else {
                binding.emptyView.visibility = View.GONE
                binding.rvQuizzes.visibility = View.VISIBLE
                quizAdapter.submitList(distinctQuizzes)
            }
        }

        // Наблюдаем за состоянием загрузки
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Наблюдаем за сообщениями об ошибках
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty()) {
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    /**
     * Упрощенный адаптер для отображения списка викторин без деталей
     */
    inner class SimpleQuizAdapter(
        private val onQuizClicked: (Quiz) -> Unit
    ) : ListAdapter<Quiz, SimpleQuizAdapter.QuizViewHolder>(SimpleQuizDiffCallback()) {
    
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
    
            fun bind(quiz: Quiz) {
                binding.apply {
                    tvQuizTitle.text = quiz.title
                    tvQuizDescription.text = quiz.description
                    // Базовая информация (без деталей)
                    tvEntryTitle.text = "Связанная статья ID: ${quiz.entryId}"
                    tvQuestionCount.text = "Данные загружаются..."
                    tvBestScore.text = "Нет данных"
                }
            }
        }
    }
    
    // Перемещено за пределы SimpleQuizAdapter, но все еще внутри QuizListFragment
    class SimpleQuizDiffCallback : DiffUtil.ItemCallback<Quiz>() {
        override fun areItemsTheSame(oldItem: Quiz, newItem: Quiz): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Quiz, newItem: Quiz): Boolean {
            return oldItem == newItem
        }
    }
} 