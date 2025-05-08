package com.example.myworldapp2.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myworldapp2.KidsEncyclopediaApp
import com.example.myworldapp2.R
import com.example.myworldapp2.databinding.FragmentContentManagementBinding
import com.example.myworldapp2.ui.adapter.ContentManagementPagerAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator

/**
 * Фрагмент для управления контентом (статьями, категориями, викторинами)
 */
class ContentManagementFragment : Fragment() {

    private var _binding: FragmentContentManagementBinding? = null
    private val binding get() = _binding!!

    private lateinit var app: KidsEncyclopediaApp

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContentManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        app = requireActivity().application as KidsEncyclopediaApp
        
        // Проверяем, является ли пользователь администратором
        app.userRepository.currentUser.observe(viewLifecycleOwner) { user ->
            if (user == null || user.role != "admin") {
                // Если пользователь не админ, перенаправляем на главную страницу
                findNavController().navigate(R.id.homeFragment)
                Snackbar.make(
                    binding.root,
                    R.string.admin_access_denied,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
        
        setupTabLayout()
        setupFabButton()
    }
    
    private fun setupTabLayout() {
        // Создаем адаптер для ViewPager
        val pagerAdapter = ContentManagementPagerAdapter(
            this, 
            listOf(
                R.string.entries to EntriesManagementFragment(),
                R.string.categories to CategoriesManagementFragment(),
                R.string.quizzes to QuizzesManagementFragment()
            )
        )
        
        binding.viewPager.adapter = pagerAdapter
        
        // Связываем ViewPager с TabLayout
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.setText(pagerAdapter.getTabTitle(position))
        }.attach()
    }
    
    private fun setupFabButton() {
        binding.fabAdd.setOnClickListener {
            val currentTabPosition = binding.viewPager.currentItem
            
            when (currentTabPosition) {
                0 -> { // Статьи
                    val action = ContentManagementFragmentDirections.actionContentManagementFragmentToEntryEditorFragment(0L)
                    findNavController().navigate(action)
                }
                1 -> { // Категории
                    val action = ContentManagementFragmentDirections.actionContentManagementFragmentToCategoryEditorFragment(0L)
                    findNavController().navigate(action)
                }
                2 -> { // Викторины
                    // Переходим к редактору викторин для создания новой викторины
                    findNavController().navigate(R.id.editQuizFragment)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 