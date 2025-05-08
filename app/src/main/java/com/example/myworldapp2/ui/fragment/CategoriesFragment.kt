package com.example.myworldapp2.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myworldapp2.KidsEncyclopediaApp
import com.example.myworldapp2.R
import com.example.myworldapp2.data.entity.Category
import com.example.myworldapp2.ui.adapter.CategoryAdapter
import com.example.myworldapp2.ui.viewmodel.CategoriesViewModel
import com.example.myworldapp2.ui.viewmodel.CategoriesViewModelFactory

class CategoriesFragment : Fragment() {

    private lateinit var viewModel: CategoriesViewModel
    
    // UI элементы
    private lateinit var rvCategories: RecyclerView
    private lateinit var emptyView: LinearLayout
    private lateinit var progressBar: ProgressBar
    
    // Адаптер
    private lateinit var categoryAdapter: CategoryAdapter
    
    // Карта количества статей в категориях
    private val entryCounts = mutableMapOf<Long, Int>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_categories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Инициализация UI компонентов
        initViews(view)
        
        // Настройка ViewModel
        setupViewModel()
        
        // Настройка адаптера
        setupAdapter()
        
        // Наблюдение за изменениями данных
        observeData()
    }
    
    private fun initViews(view: View) {
        rvCategories = view.findViewById(R.id.rv_categories)
        emptyView = view.findViewById(R.id.empty_view)
        progressBar = view.findViewById(R.id.progress_bar)
        
        // Настройка RecyclerView - GridLayoutManager с spanCount = 2
        // Удаляем настройку layoutManager здесь, так как она уже определена в XML
    }
    
    private fun setupViewModel() {
        val app = requireActivity().application as KidsEncyclopediaApp
        val factory = CategoriesViewModelFactory(
            app.categoryRepository,
            app.entryRepository
        )
        viewModel = ViewModelProvider(this, factory)[CategoriesViewModel::class.java]
    }
    
    private fun setupAdapter() {
        categoryAdapter = CategoryAdapter { category ->
            onCategoryClicked(category)
        }
        rvCategories.adapter = categoryAdapter
    }
    
    private fun observeData() {
        // Показываем загрузку
        showLoading(true)
        
        // Наблюдаем за категориями
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            // Обновляем адаптер
            categoryAdapter.submitList(categories)
            
            // Если категорий нет, показываем пустой экран
            if (categories.isEmpty()) {
                showEmptyState(true)
            } else {
                showEmptyState(false)
            }
        }
        
        // Наблюдаем за количеством статей в категориях
        viewModel.entryCounts.observe(viewLifecycleOwner) { counts ->
            entryCounts.clear()
            entryCounts.putAll(counts)
            // Обновляем адаптер, чтобы отобразить количество статей
            categoryAdapter.notifyDataSetChanged()
        }
        
        // Наблюдаем за состоянием загрузки
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            showLoading(isLoading)
        }
    }
    
    private fun onCategoryClicked(category: Category) {
        // Переходим к деталям категории
        val action = CategoriesFragmentDirections.actionCategoriesFragmentToCategoryDetailFragment(category.id)
        findNavController().navigate(action)
    }
    
    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        
        if (isLoading) {
            rvCategories.visibility = View.GONE
            emptyView.visibility = View.GONE
        } else {
            rvCategories.visibility = View.VISIBLE
        }
    }
    
    private fun showEmptyState(isEmpty: Boolean) {
        emptyView.visibility = if (isEmpty) View.VISIBLE else View.GONE
        
        if (isEmpty) {
            rvCategories.visibility = View.GONE
        } else {
            rvCategories.visibility = View.VISIBLE
        }
    }
} 