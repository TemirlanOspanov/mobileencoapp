package com.example.myworldapp2.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myworldapp2.KidsEncyclopediaApp
import com.example.myworldapp2.R
import com.example.myworldapp2.databinding.FragmentBookmarksBinding
import com.example.myworldapp2.ui.adapter.EntryAdapter
import com.example.myworldapp2.ui.viewmodel.BookmarksViewModel
import com.example.myworldapp2.ui.viewmodel.BookmarksViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BookmarksFragment : Fragment() {

    private var _binding: FragmentBookmarksBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: BookmarksViewModel
    private lateinit var entryAdapter: EntryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookmarksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Получаем экземпляр приложения
        val app = requireActivity().application as KidsEncyclopediaApp
        
        // Создаем фабрику для ViewModel с необходимыми зависимостями
        val viewModelFactory = BookmarksViewModelFactory(
            app.bookmarkRepository,
            app.entryRepository,
            app.categoryRepository
        )
        
        // Инициализируем ViewModel
        viewModel = ViewModelProvider(this, viewModelFactory)[BookmarksViewModel::class.java]

        setupToolbar()
        setupRecyclerView()
        setupEmptyView()
        observeViewModel()
        
        // Показываем индикатор загрузки при старте
        binding.swipeRefreshLayout?.isRefreshing = true
        
        // Принудительно запрашиваем закладки при создании фрагмента
        forceLoadBookmarks()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        // Настраиваем адаптер для отображения закладок
        entryAdapter = EntryAdapter(
            onEntryClicked = { entry ->
                // Навигация к деталям статьи
                val action = BookmarksFragmentDirections
                    .actionBookmarksFragmentToEntryDetailFragment(entry.id)
                findNavController().navigate(action)
            }
        )

        // Настраиваем RecyclerView
        binding.rvBookmarks.apply {
            adapter = entryAdapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        }
    }

    private fun setupEmptyView() {
        // Настраиваем кнопку "Просмотреть категории" на пустом экране
        binding.btnExplore.setOnClickListener {
            findNavController().navigate(R.id.categoriesFragment)
        }
    }

    private fun observeViewModel() {
        // Наблюдаем за списком закладок
        viewModel.bookmarkedEntries.observe(viewLifecycleOwner) { entries ->
            val nonNullEntries = entries ?: emptyList()
            android.util.Log.d("BookmarksFragment", "Получены закладки: ${nonNullEntries.size} элементов")
            
            // Обновляем UI в соответствии с полученными данными
            updateUI(nonNullEntries)
        }

        // Добавляем возможность обновления списка закладок через pull-to-refresh
        binding.swipeRefreshLayout?.setOnRefreshListener {
            android.util.Log.d("BookmarksFragment", "Запрос на обновление списка закладок")
            viewModel.refreshBookmarks()
        }

        // Наблюдаем за состоянием загрузки
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (_binding != null) { // Check if binding is not null
                // Обновляем состояние SwipeRefreshLayout
                binding.swipeRefreshLayout?.isRefreshing = isLoading
                // Показываем/скрываем основной индикатор загрузки только если не обновление свайпом
                binding.progressBar?.visibility = if (isLoading && binding.swipeRefreshLayout?.isRefreshing == false) 
                    View.VISIBLE else View.GONE
            }
        }

        // Наблюдаем за сообщениями об ошибках
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty()) {
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            }
        }
    }
    
    private fun updateUI(entries: List<com.example.myworldapp2.data.entity.Entry>) {
        if (entries.isEmpty()) {
            android.util.Log.d("BookmarksFragment", "Список закладок пуст, показываем пустой экран")
            binding.emptyView.visibility = View.VISIBLE
            binding.rvBookmarks.visibility = View.GONE
        } else {
            android.util.Log.d("BookmarksFragment", "Есть закладки, показываем список")
            binding.emptyView.visibility = View.GONE
            binding.rvBookmarks.visibility = View.VISIBLE
            
            // Сначала очищаем список для гарантированного обновления
            entryAdapter.submitList(null)
            
            // Затем устанавливаем новый список с небольшой задержкой
            viewLifecycleOwner.lifecycleScope.launch {
                delay(50)
                entryAdapter.submitList(entries)
            }
        }
        
        // Завершаем индикатор обновления
        binding.swipeRefreshLayout?.isRefreshing = false
    }

    override fun onResume() {
        super.onResume()
        android.util.Log.d("BookmarksFragment", "onResume: обновляем список закладок")
        
        // Запускаем обновление сразу при переходе на экран
        refreshBookmarks()
        
        // Запускаем жесткое обновление данных с небольшой задержкой
        // чтобы дать время на все операции обновления БД
        viewLifecycleOwner.lifecycleScope.launch {
            delay(300)
            forceLoadBookmarks()
        }
    }

    /**
     * Выполняет "жесткое" обновление данных через пересоздание адаптера
     * и принудительный запрос новых данных
     */
    private fun manual_refresh() {
        android.util.Log.d("BookmarksFragment", "Выполняем жесткое обновление данных закладок")
        
        // Показываем индикатор загрузки
        binding.swipeRefreshLayout?.isRefreshing = true
        
        // Принудительно запрашиваем актуальные данные у ViewModel
        viewModel.refreshBookmarks()
    }

    /**
     * Принудительно обновляет список закладок
     */
    private fun refreshBookmarks() {
        android.util.Log.d("BookmarksFragment", "Запрашиваем принудительное обновление списка закладок")
        
        // Показываем индикатор загрузки
        binding.swipeRefreshLayout?.isRefreshing = true
        
        // Запрашиваем обновление данных
        viewModel.refreshBookmarks()
    }

    /**
     * Принудительно запрашивает данные о закладках через прямой доступ к репозиторию
     */
    private fun forceLoadBookmarks() {
        android.util.Log.d("BookmarksFragment", "Принудительно запрашиваем закладки напрямую")
        
        // Показываем индикатор загрузки
        binding.swipeRefreshLayout?.isRefreshing = true
        
        // Запускаем асинхронную загрузку
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Запрашиваем информацию о закладках напрямую из ViewModel
                viewModel.forceLoadBookmarks()
                
                // Ждем немного, чтобы пользователь увидел, что происходит обновление
                delay(500)
            } finally {
                // Скрываем индикатор загрузки
                binding.swipeRefreshLayout?.isRefreshing = false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 