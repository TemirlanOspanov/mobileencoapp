package com.example.myworldapp2.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myworldapp2.KidsEncyclopediaApp
import com.example.myworldapp2.R
import com.example.myworldapp2.data.entity.Achievement
import com.example.myworldapp2.data.entity.Category
import com.example.myworldapp2.data.entity.Entry
import com.example.myworldapp2.ui.adapter.AchievementAdapter
import com.example.myworldapp2.ui.adapter.CategoryAdapter
import com.example.myworldapp2.ui.adapter.EntryAdapter
import com.example.myworldapp2.ui.viewmodel.HomeViewModel
import com.example.myworldapp2.ui.viewmodel.HomeViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel
    
    // UI элементы
    private lateinit var tvWelcome: TextView
    private lateinit var progressOverall: ProgressBar
    private lateinit var tvProgressPercent: TextView
    private lateinit var rvCategories: RecyclerView
    private lateinit var rvEntries: RecyclerView
    private lateinit var rvAchievements: RecyclerView
    private lateinit var emptyView: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var btnExplore: Button
    private lateinit var btnAllCategories: Button
    private lateinit var btnAllEntries: Button
    private lateinit var btnAllAchievements: Button
    
    // Адаптеры
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var entryAdapter: EntryAdapter
    private lateinit var achievementAdapter: AchievementAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Инициализация UI компонентов
        initViews(view)
        
        // Настройка ViewModel
        setupViewModel()
        
        // Настройка адаптеров
        setupAdapters()
        
        // Настройка слушателей для кнопок
        setupListeners()
        
        // Наблюдение за изменениями данных
        observeData()
    }
    
    private fun initViews(view: View) {
        tvWelcome = view.findViewById(R.id.tv_welcome)
        progressOverall = view.findViewById(R.id.progress_overall)
        tvProgressPercent = view.findViewById(R.id.tv_progress_percent)
        rvCategories = view.findViewById(R.id.rv_categories)
        rvEntries = view.findViewById(R.id.rv_entries)
        rvAchievements = view.findViewById(R.id.rv_achievements)
        emptyView = view.findViewById(R.id.empty_view)
        progressBar = view.findViewById(R.id.progress_bar)
        btnExplore = view.findViewById(R.id.btn_explore)
        btnAllCategories = view.findViewById(R.id.btn_all_categories)
        btnAllEntries = view.findViewById(R.id.btn_all_entries)
        btnAllAchievements = view.findViewById(R.id.btn_all_achievements)
        
        // Настройка RecyclerView с горизонтальным скроллингом для категорий и достижений
        rvCategories.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvAchievements.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        
        // Настройка RecyclerView с вертикальным скроллингом для статей
        rvEntries.layoutManager = LinearLayoutManager(context)
    }
    
    private fun setupViewModel() {
        val app = requireActivity().application as KidsEncyclopediaApp
        val factory = HomeViewModelFactory(
            app.categoryRepository,
            app.entryRepository,
            app.userRepository,
            app.achievementRepository,
            app.userAchievementRepository
        )
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
    }
    
    private fun setupAdapters() {
        // Адаптер для категорий
        categoryAdapter = CategoryAdapter { category ->
            onCategoryClicked(category)
        }
        rvCategories.adapter = categoryAdapter
        
        // Адаптер для статей (без getCategory)
        entryAdapter = EntryAdapter(
            onEntryClicked = { entry ->
                onEntryClicked(entry)
            }
        )
        rvEntries.adapter = entryAdapter
        
        // Адаптер для достижений
        achievementAdapter = AchievementAdapter(
            onAchievementClicked = { achievementWithProgress ->
                onAchievementClicked(achievementWithProgress.achievement)
            }
        )
        rvAchievements.adapter = achievementAdapter
    }
    
    private fun setupListeners() {
        btnExplore.setOnClickListener {
            findNavController().navigate(R.id.categoriesFragment)
        }
        
        btnAllCategories.setOnClickListener {
            findNavController().navigate(R.id.categoriesFragment)
        }
        
        btnAllEntries.setOnClickListener {
            // TODO: Navigate to a full entry list screen if available
            findNavController().navigate(R.id.categoriesFragment) // Placeholder nav
        }
        
        btnAllAchievements.setOnClickListener {
            findNavController().navigate(R.id.achievementsFragment)
        }
    }
    
    private fun observeData() {
        // Показываем загрузку
        showLoading(true)
        
        // Наблюдаем за категориями
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            // Обновляем адаптер
            categoryAdapter.submitList(categories)
            
            // Если категорий нет, показываем пустой экран
            if (categories.isNullOrEmpty()) {
                showEmptyState(true)
            } else {
                showEmptyState(false)
            }
        }
        
        // Наблюдаем за недавними статьями
        viewModel.recentEntries.observe(viewLifecycleOwner) { entries ->
            // Обновляем адаптер
            entryAdapter.submitList(entries)
            
            // Если недавних статей нет, показываем пустой экран
            if (viewModel.categories.value.isNullOrEmpty() && entries.isNullOrEmpty()) {
                showEmptyState(true)
            } else {
                showEmptyState(false)
            }
        }
        
        // Наблюдаем за достижениями пользователя
        viewModel.userAchievements.observe(viewLifecycleOwner) { achievements ->
            // Обновляем адаптер
            achievementAdapter.submitList(achievements)
        }
        
        // Наблюдаем за общим прогрессом чтения
        viewModel.overallProgress.observe(viewLifecycleOwner) { progress ->
            // Обновляем прогресс-бар и текст
            progressOverall.progress = progress.toInt()
            tvProgressPercent.text = 
                getString(R.string.overall_progress, progress)
            
            // Прячем загрузку
            showLoading(false)
        }
        
        viewModel.welcomeMessage.observe(viewLifecycleOwner) { message ->
            tvWelcome.text = message
        }
        
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty()) {
                Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
            }
        }
    }
    
    private fun onCategoryClicked(category: Category) {
        // Переходим к деталям категории
        val action = HomeFragmentDirections.actionHomeFragmentToCategoryDetailFragment(category.id)
        findNavController().navigate(action)
    }
    
    private fun onEntryClicked(entry: Entry) {
        // Переходим к деталям статьи
        viewModel.markEntryAsRead(entry.id)
        val action = HomeFragmentDirections.actionHomeFragmentToEntryDetailFragment(entry.id)
        findNavController().navigate(action)
    }
    
    private fun onAchievementClicked(achievement: Achievement) {
        // Показываем информацию о достижении
        Snackbar.make(
            requireView(),
            "${achievement.title}: ${achievement.description}",
            Snackbar.LENGTH_LONG
        ).show()
    }
    
    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        
        if (isLoading) {
            rvCategories.visibility = View.GONE
            rvEntries.visibility = View.GONE
            rvAchievements.visibility = View.GONE
            emptyView.visibility = View.GONE
        } else {
            rvCategories.visibility = View.VISIBLE
            rvEntries.visibility = View.VISIBLE
            rvAchievements.visibility = View.VISIBLE
        }
    }
    
    private fun showEmptyState(isEmpty: Boolean) {
        emptyView.visibility = if (isEmpty) View.VISIBLE else View.GONE
        
        if (isEmpty) {
            rvCategories.visibility = View.GONE
            rvEntries.visibility = View.GONE
            rvAchievements.visibility = View.GONE
        } else {
            rvCategories.visibility = View.VISIBLE
            rvEntries.visibility = View.VISIBLE
            rvAchievements.visibility = View.VISIBLE
        }
    }

    /**
     * Метод для фильтрации уникальных категорий через HashSet
     */
    private fun getUniqueCategories(categoryIds: List<Long>): Set<Long> {
        val uniqueCategories = HashSet<Long>()
        uniqueCategories.addAll(categoryIds)
        return uniqueCategories
    }
} 