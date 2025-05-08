package com.example.myworldapp2.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.myworldapp2.KidsEncyclopediaApp
import com.example.myworldapp2.R
import com.example.myworldapp2.data.model.UserActivity
import com.example.myworldapp2.databinding.FragmentProfileBinding
import com.example.myworldapp2.ui.adapter.CategoryProgressAdapter
import com.example.myworldapp2.ui.adapter.UserActivityAdapter
import com.example.myworldapp2.ui.viewmodel.ProfileViewModel
import com.example.myworldapp2.ui.viewmodel.ProfileViewModelFactory
import com.google.android.material.snackbar.Snackbar
import java.util.HashMap

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProfileViewModel
    private lateinit var categoryProgressAdapter: CategoryProgressAdapter
    private lateinit var userActivityAdapter: UserActivityAdapter

    // Хранилище кэша для улучшения производительности (HashMap)
    private val dataCache = HashMap<String, String>()
    
    /**
     * Метод для демонстрации работы с HashMap
     */
    private fun updateCache(key: String, value: String) {
        dataCache[key] = value
    }
    
    /**
     * Метод для получения данных из кэша
     */
    private fun getFromCache(key: String): String {
        return dataCache[key] ?: "Не найдено в кэше"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Получаем экземпляр приложения
        val app = requireActivity().application as KidsEncyclopediaApp
        
        // Создаем фабрику для ViewModel с необходимыми зависимостями
        val viewModelFactory = ProfileViewModelFactory(app.userRepository)
        
        // Инициализируем ViewModel
        viewModel = ViewModelProvider(this, viewModelFactory)[ProfileViewModel::class.java]
        
        // Добавляем тестовые данные в кэш
        updateCache("user_role", "student")
        updateCache("last_visited", "home_screen")
        
        setupAdapters()
        setupButtons()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        // Обновляем данные профиля при каждом возвращении на экран
        viewModel.loadUserData()
        
        // Обновляем информацию о последнем посещении в кэше
        updateCache("last_visit_time", System.currentTimeMillis().toString())
    }

    private fun setupAdapters() {
        // Адаптер для прогресса по категориям
        categoryProgressAdapter = CategoryProgressAdapter()
        binding.rvCategoryProgress.adapter = categoryProgressAdapter
        
        // Адаптер для последних активностей
        userActivityAdapter = UserActivityAdapter { activity ->
            handleActivityClick(activity)
        }
        binding.rvRecentActivity.adapter = userActivityAdapter
    }

    private fun setupButtons() {
        // Кнопка редактирования профиля
        binding.btnEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }
        
        // Кнопка смены пароля
        binding.btnChangePassword.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_changePasswordFragment)
        }
    }

    private fun observeViewModel() {
        // Наблюдаем за данными пользователя
        viewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.tvUsername.text = it.name
                binding.tvEmail.text = it.email
                // Сохраняем имя пользователя в кэш
                updateCache("current_username", it.name)
            }
        }
        
        // Наблюдаем за статистикой пользователя
        viewModel.userStats.observe(viewLifecycleOwner) { stats ->
            stats?.let {
                // Обновляем прогресс
                val overallProgress = viewModel.calculateOverallProgress()
                binding.progressOverall.progress = overallProgress
                binding.tvOverallProgress.text = getString(R.string.overall_progress, overallProgress.toFloat())
                
                // Обновляем статистику по статьям
                binding.tvReadEntries.text = getString(
                    R.string.read_entries_count,
                    it.readEntries,
                    it.totalEntries
                )
                
                // Обновляем статистику по викторинам
                binding.tvCompletedQuizzes.text = getString(
                    R.string.completed_quizzes_count,
                    it.completedQuizzes,
                    it.totalQuizzes
                )
                
                // Обновляем статистику по достижениям
                binding.tvAchievements.text = getString(
                    R.string.earned_achievements_count,
                    it.earnedAchievements,
                    it.totalAchievements
                )
                
                // Сохраняем статистику в кэш для быстрого доступа
                updateCache("total_entries", it.totalEntries.toString())
                updateCache("read_entries", it.readEntries.toString())
            }
        }
        
        // Наблюдаем за прогрессом по категориям
        viewModel.categoryProgress.observe(viewLifecycleOwner) { progress ->
            categoryProgressAdapter.submitList(progress)
        }
        
        // Наблюдаем за последними активностями
        viewModel.recentActivities.observe(viewLifecycleOwner) { activities ->
            userActivityAdapter.submitList(activities)
        }
        
        // Наблюдаем за состоянием загрузки
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Прогресс-бара нет в макете, поэтому просто логируем состояние
            if (isLoading) {
                Snackbar.make(binding.root, "Загрузка данных...", Snackbar.LENGTH_SHORT).show()
            }
        }
        
        // Наблюдаем за сообщениями об ошибках
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty()) {
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                // Сохраняем последнюю ошибку в кэш для аналитики
                updateCache("last_error", message)
            }
        }
    }

    /**
     * Обрабатывает клик по элементу активности
     */
    private fun handleActivityClick(activity: UserActivity) {
        // Сохраняем информацию о нажатии в кэш
        updateCache("last_clicked_activity", activity.type.name)
        
        when (activity.type) {
            com.example.myworldapp2.data.model.ActivityType.READ_ENTRY -> {
                val action = ProfileFragmentDirections
                    .actionProfileFragmentToEntryDetailFragment(activity.relatedId)
                findNavController().navigate(action)
            }
            com.example.myworldapp2.data.model.ActivityType.COMPLETED_QUIZ -> {
                val action = ProfileFragmentDirections
                    .actionProfileFragmentToQuizDetailFragment(activity.relatedId)
                findNavController().navigate(action)
            }
            com.example.myworldapp2.data.model.ActivityType.EARNED_ACHIEVEMENT -> {
                // TODO: Переход к экрану достижений
                Snackbar.make(binding.root, "Просмотр достижения: ${getFromCache("achievement_name")}", Snackbar.LENGTH_SHORT).show()
            }
            com.example.myworldapp2.data.model.ActivityType.ADDED_BOOKMARK -> {
                val action = ProfileFragmentDirections
                    .actionProfileFragmentToEntryDetailFragment(activity.relatedId)
                findNavController().navigate(action)
            }
            com.example.myworldapp2.data.model.ActivityType.ADDED_COMMENT -> {
                val action = ProfileFragmentDirections
                    .actionProfileFragmentToEntryDetailFragment(activity.relatedId)
                findNavController().navigate(action)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 