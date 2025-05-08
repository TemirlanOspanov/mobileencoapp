package com.example.myworldapp2.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.myworldapp2.KidsEncyclopediaApp
import com.example.myworldapp2.R
import com.example.myworldapp2.databinding.FragmentSearchBinding
import com.example.myworldapp2.ui.adapter.SearchResultAdapter
import com.example.myworldapp2.ui.viewmodel.SearchViewModel
import com.example.myworldapp2.ui.viewmodel.SearchViewModelFactory
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import java.util.ArrayList
import android.content.Context
import android.view.inputmethod.InputMethodManager

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SearchViewModel
    private lateinit var adapter: SearchResultAdapter

    private lateinit var searchHistoryListView: ListView
    private lateinit var searchHistoryTitle: TextView
    private val searchHistoryList = ArrayList<String>()
    private lateinit var searchHistoryAdapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupUI()
        observeViewModel()

        // Инициализация ListView для истории поиска
        searchHistoryTitle = view.findViewById(R.id.tv_search_history)
        searchHistoryListView = view.findViewById(R.id.lv_search_history)
        
        // Создаем адаптер для списка истории поиска
        searchHistoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            searchHistoryList
        )
        searchHistoryListView.adapter = searchHistoryAdapter
        
        // Обработка клика по элементу истории
        searchHistoryListView.setOnItemClickListener { _, _, position, _ ->
            val query = searchHistoryList[position]
            // Устанавливаем текст поиска из истории
            binding.searchEditText.setText(query)
            // Выполняем поиск
            performSearch(query)
        }
    }

    private fun setupViewModel() {
        val app = requireActivity().application as KidsEncyclopediaApp
        
        // Получаем SearchRepository из KidsEncyclopediaApp
        val searchRepository = app.searchRepository
        
        // Создаем фабрику для ViewModel
        val viewModelFactory = SearchViewModelFactory(searchRepository, app.userRepository)
        
        // Инициализируем ViewModel
        viewModel = ViewModelProvider(this, viewModelFactory)[SearchViewModel::class.java]
    }

    private fun setupUI() {
        // Настраиваем панель инструментов
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        
        // Настраиваем поле поиска
        binding.searchEditText.apply {
            doAfterTextChanged { text ->
                if (!text.isNullOrBlank() && text.length >= 3) {
                    viewModel.search(text.toString())
                } else if (text.isNullOrBlank()) {
                    viewModel.clearResults()
                }
            }
            
            // Обработчик нажатия кнопки "Поиск" на клавиатуре
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    val query = binding.searchEditText.text.toString().trim()
                    if (query.isNotEmpty()) {
                        performSearch(query)
                        // Скрываем клавиатуру
                        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
                    }
                    return@setOnEditorActionListener true
                }
                false
            }
        }
        
        // Настраиваем адаптер для результатов поиска
        adapter = SearchResultAdapter { searchResult ->
            // Переходим на страницу статьи
            val action = SearchFragmentDirections.actionSearchFragmentToEntryDetailFragment(
                searchResult.entry.id
            )
            findNavController().navigate(action)
        }
        binding.searchResultsRecyclerView.adapter = adapter
        
        // Настраиваем фильтры
        binding.filterCategory.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setFilterByCategory(isChecked)
        }
        
        binding.filterTag.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setFilterByTag(isChecked)
        }
        
        binding.filterUnread.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setFilterUnread(isChecked)
        }
    }

    private fun observeViewModel() {
        // Отслеживаем результаты поиска
        viewModel.searchResults.observe(viewLifecycleOwner) { results ->
            adapter.submitList(results)
            
            // Показываем сообщение о количестве результатов или пустой вид
            if (results.isEmpty() && !binding.searchEditText.text.isNullOrBlank()) {
                binding.emptyView.visibility = View.VISIBLE
                binding.searchResultsRecyclerView.visibility = View.GONE
            } else {
                binding.emptyView.visibility = View.GONE
                binding.searchResultsRecyclerView.visibility = View.VISIBLE
                
                // Обновляем заголовок с количеством результатов
                if (results.isNotEmpty()) {
                    binding.toolbar.title = getString(R.string.search_results, results.size)
                } else {
                    binding.toolbar.title = getString(R.string.search)
                }
            }
        }
        
        // Отслеживаем статус загрузки
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        // Отслеживаем ошибки
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
            }
        }
        
        // Отслеживаем статус фильтров
        viewModel.filterByCategory.observe(viewLifecycleOwner) { isChecked ->
            binding.filterCategory.isChecked = isChecked
        }
        
        viewModel.filterByTag.observe(viewLifecycleOwner) { isChecked ->
            binding.filterTag.isChecked = isChecked
        }
        
        viewModel.filterUnread.observe(viewLifecycleOwner) { isChecked ->
            binding.filterUnread.isChecked = isChecked
        }
    }

    // Метод для добавления запроса в историю поиска
    private fun addToSearchHistory(query: String) {
        // Проверяем, есть ли уже такой запрос в истории
        if (searchHistoryList.contains(query)) {
            // Если есть, удаляем старый и добавляем заново (чтобы переместить в начало)
            searchHistoryList.remove(query)
        }
        
        // Добавляем новый запрос в начало списка
        searchHistoryList.add(0, query)
        
        // Ограничиваем историю 10 последними запросами
        if (searchHistoryList.size > 10) {
            searchHistoryList.removeAt(searchHistoryList.size - 1)
        }
        
        // Обновляем адаптер
        searchHistoryAdapter.notifyDataSetChanged()
        
        // Показываем секцию истории поиска
        if (searchHistoryList.isNotEmpty()) {
            searchHistoryTitle.visibility = View.VISIBLE
            searchHistoryListView.visibility = View.VISIBLE
        }
    }
    
    // Обновленная функция performSearch для интеграции с существующим поиском
    private fun performSearch(query: String) {
        // Добавляем запрос в историю
        if (query.isNotEmpty()) {
            addToSearchHistory(query)
        }
        
        // Выполняем поиск с помощью ViewModel
        viewModel.search(query)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 