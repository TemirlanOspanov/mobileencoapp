package com.example.myworldapp2.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myworldapp2.KidsEncyclopediaApp
import com.example.myworldapp2.R
import com.example.myworldapp2.data.entity.Category
import com.example.myworldapp2.databinding.FragmentCategoriesManagementBinding
import com.example.myworldapp2.ui.adapter.CategoryAdminAdapter
import com.example.myworldapp2.ui.viewmodel.CategoriesViewModel
import com.example.myworldapp2.ui.viewmodel.CategoriesViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

/**
 * Фрагмент для управления категориями в админ-панели
 */
class CategoriesManagementFragment : Fragment() {

    private var _binding: FragmentCategoriesManagementBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CategoriesViewModel
    private lateinit var adapter: CategoryAdminAdapter
    
    // Хранение количества статей в категориях
    private val entryCounts = mutableMapOf<Long, Int>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoriesManagementBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val app = requireActivity().application as KidsEncyclopediaApp
        
        // Создаем ViewModel и Factory с передачей репозиториев
        val viewModelFactory = CategoriesViewModelFactory(
            app.categoryRepository,
            app.entryRepository
        )
        viewModel = ViewModelProvider(this, viewModelFactory)[CategoriesViewModel::class.java]
        
        // Настраиваем RecyclerView с адаптером
        setupRecyclerView()
        
        // Наблюдаем за данными
        observeViewModel()
    }
    
    private fun setupRecyclerView() {
        adapter = CategoryAdminAdapter(
            onCategoryClicked = { category ->
                // При клике на категорию показываем ее содержимое (можно расширить для просмотра деталей)
                Snackbar.make(binding.root, "Просмотр категории: ${category.name}", Snackbar.LENGTH_SHORT).show()
            },
            onCategoryEdit = { category ->
                // При клике на редактирование переходим к редактору с ID категории
                // Используем прямую навигацию к фрагменту по его ID
                val bundle = Bundle().apply {
                    putLong("categoryId", category.id)
                }
                // Получаем NavController на уровне Activity
                activity?.let {
                    val navController = androidx.navigation.Navigation.findNavController(it, R.id.nav_host_fragment)
                    navController.navigate(R.id.categoryEditorFragment, bundle)
                } ?: run {
                    Snackbar.make(binding.root, "Ошибка навигации", Snackbar.LENGTH_SHORT).show()
                }
            },
            onCategoryDelete = { category ->
                // При клике на удаление показываем диалог подтверждения
                confirmDeleteCategory(category)
            },
            getEntryCount = { categoryId ->
                entryCounts[categoryId] ?: 0
            }
        )
        
        binding.categoriesRecyclerView.adapter = adapter
        binding.categoriesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }
    
    private fun observeViewModel() {
        // Наблюдаем за списком категорий
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            adapter.submitList(categories)
            
            // Отображаем пустой вид, если нет категорий
            if (categories.isEmpty()) {
                binding.emptyView.visibility = View.VISIBLE
                binding.categoriesRecyclerView.visibility = View.GONE
            } else {
                binding.emptyView.visibility = View.GONE
                binding.categoriesRecyclerView.visibility = View.VISIBLE
            }
        }
        
        // Наблюдаем за состоянием загрузки
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        // Наблюдаем за количеством статей в категориях
        viewModel.entryCounts.observe(viewLifecycleOwner) { counts ->
            entryCounts.clear()
            entryCounts.putAll(counts)
            adapter.notifyDataSetChanged()
        }
    }
    
    private fun confirmDeleteCategory(category: Category) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.confirm_delete)
            .setMessage(getString(R.string.confirm_delete_category, category.name))
            .setPositiveButton(R.string.delete) { _, _ ->
                deleteCategory(category)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun deleteCategory(category: Category) {
        viewModel.viewModelScope.launch {
            try {
                viewModel.deleteCategory(category)
                Snackbar.make(
                    binding.root,
                    getString(R.string.category_deleted, category.name),
                    Snackbar.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.error_delete_category, e.message),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Обновляем список категорий при возвращении к фрагменту
        viewModel.loadCategories()
        viewModel.loadEntryCounts()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 