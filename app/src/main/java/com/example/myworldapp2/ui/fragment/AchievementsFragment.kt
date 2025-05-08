package com.example.myworldapp2.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myworldapp2.KidsEncyclopediaApp
import com.example.myworldapp2.R
import com.example.myworldapp2.data.model.AchievementWithProgress
import com.example.myworldapp2.ui.adapter.AchievementAdapter
import com.example.myworldapp2.ui.viewmodel.AchievementsViewModel
import com.example.myworldapp2.ui.viewmodel.AchievementsViewModelFactory
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout

/**
 * Фрагмент для отображения списка достижений пользователя
 */
class AchievementsFragment : Fragment() {

    private val viewModel: AchievementsViewModel by viewModels { 
        val app = requireActivity().application as KidsEncyclopediaApp
        AchievementsViewModelFactory(app.achievementRepository).setUserId(4)
    }

    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingView: LinearLayout
    private lateinit var errorView: LinearLayout
    private lateinit var emptyView: LinearLayout
    private lateinit var tvErrorMessage: TextView
    private lateinit var adapter: AchievementAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_achievements, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Инициализация UI элементов
        view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).setNavigationOnClickListener { 
            findNavController().navigateUp() 
        }
        
        // Настройка табов
        tabLayout = view.findViewById(R.id.tabLayout)
        setupTabLayout()
        
        // Настройка RecyclerView и состояний
        recyclerView = view.findViewById(R.id.recyclerView)
        loadingView = view.findViewById(R.id.loadingView)
        errorView = view.findViewById(R.id.errorView)
        emptyView = view.findViewById(R.id.emptyView)
        tvErrorMessage = view.findViewById(R.id.tvErrorMessage)
        
        // Настройка адаптера
        adapter = AchievementAdapter { achievement ->
            onAchievementClicked(achievement)
        }
        
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        
        // Настройка кнопки повторной загрузки
        view.findViewById<Button>(R.id.btnRetry).setOnClickListener {
            viewModel.refresh()
        }
        
        // Подписка на изменения данных в ViewModel
        observeViewModel()
    }
    
    /**
     * Настройка вкладок для фильтрации достижений
     */
    private fun setupTabLayout() {
        // Добавление вкладок для разных типов достижений
        tabLayout.addTab(tabLayout.newTab().setText(R.string.achievements_all))
        tabLayout.addTab(tabLayout.newTab().setText(R.string.reading_achievements))
        tabLayout.addTab(tabLayout.newTab().setText(R.string.quiz_achievements))
        tabLayout.addTab(tabLayout.newTab().setText(R.string.social_achievements))
        
        // Обработчик переключения вкладок
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val type = when(tab.position) {
                    0 -> AchievementsViewModel.AchievementType.ALL
                    1 -> AchievementsViewModel.AchievementType.READING
                    2 -> AchievementsViewModel.AchievementType.QUIZ
                    3 -> AchievementsViewModel.AchievementType.SOCIAL
                    else -> AchievementsViewModel.AchievementType.ALL
                }
                viewModel.setAchievementType(type)
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }
    
    /**
     * Подписка на изменения данных в ViewModel
     */
    private fun observeViewModel() {
        // Наблюдение за изменениями списка достижений
        viewModel.achievements.observe(viewLifecycleOwner) { achievements ->
            adapter.submitList(achievements)
            
            // Показываем сообщение о пустом списке, если достижений нет
            if (achievements.isEmpty()) {
                showEmptyView()
            } else {
                showContent()
            }
        }
        
        // Наблюдение за состоянием загрузки
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                showLoading()
            }
        }
        
        // Наблюдение за ошибками
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrEmpty()) {
                showError(errorMessage)
            }
        }
        
        // Наблюдение за выбранным типом достижений
        viewModel.selectedType.observe(viewLifecycleOwner) { type ->
            val tabPosition = when(type) {
                AchievementsViewModel.AchievementType.ALL -> 0
                AchievementsViewModel.AchievementType.READING -> 1
                AchievementsViewModel.AchievementType.QUIZ -> 2
                AchievementsViewModel.AchievementType.SOCIAL -> 3
                else -> 0
            }
            
            // Выбираем соответствующую вкладку
            if (tabLayout.selectedTabPosition != tabPosition) {
                tabLayout.getTabAt(tabPosition)?.select()
            }
        }
    }
    
    /**
     * Обработка нажатия на достижение
     */
    private fun onAchievementClicked(achievement: AchievementWithProgress) {
        // Увеличиваем прогресс достижения
        viewModel.incrementAchievement(achievement)
        
        // Показываем уведомление пользователю
        val message = if (achievement.isCompleted) {
            getString(R.string.achievement_already_completed, achievement.achievement.title)
        } else {
            val newProgress = achievement.progress + 1
            val targetProgress = achievement.achievement.targetProgress
            val percentage = (newProgress * 100 / targetProgress).coerceIn(0, 100)
            
            if (newProgress >= targetProgress) {
                getString(R.string.achievement_just_completed, achievement.achievement.title)
            } else {
                getString(R.string.achievement_progress_incremented, 
                    achievement.achievement.title, newProgress, targetProgress, percentage)
            }
        }
        
        Snackbar.make(
            requireView(),
            message,
            Snackbar.LENGTH_SHORT
        ).show()
    }
    
    // Методы управления видимостью различных состояний UI
    
    private fun showLoading() {
        loadingView.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        errorView.visibility = View.GONE
        emptyView.visibility = View.GONE
    }
    
    private fun showContent() {
        loadingView.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        errorView.visibility = View.GONE
        emptyView.visibility = View.GONE
    }
    
    private fun showError(message: String) {
        loadingView.visibility = View.GONE
        recyclerView.visibility = View.GONE
        errorView.visibility = View.VISIBLE
        emptyView.visibility = View.GONE
        
        tvErrorMessage.text = message
    }
    
    private fun showEmptyView() {
        loadingView.visibility = View.GONE
        recyclerView.visibility = View.GONE
        errorView.visibility = View.GONE
        emptyView.visibility = View.VISIBLE
    }
} 
 