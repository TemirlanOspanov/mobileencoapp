package com.example.myworldapp2.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myworldapp2.KidsEncyclopediaApp
import com.example.myworldapp2.R
import com.example.myworldapp2.data.entity.Entry
import com.example.myworldapp2.databinding.FragmentEntriesManagementBinding
import com.example.myworldapp2.ui.adapter.EntryAdapter
import com.example.myworldapp2.ui.viewmodel.EntryListViewModel
import com.example.myworldapp2.ui.viewmodel.EntryListViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

/**
 * Фрагмент для управления статьями в административной панели
 */
class EntriesManagementFragment : Fragment() {

    private var _binding: FragmentEntriesManagementBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: EntryListViewModel
    private lateinit var adapter: EntryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEntriesManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val app = requireActivity().application as KidsEncyclopediaApp
        
        // Создаем ViewModel и Factory с передачей репозитория
        val viewModelFactory = EntryListViewModelFactory(app.entryRepository)
        viewModel = ViewModelProvider(this, viewModelFactory)[EntryListViewModel::class.java]
        
        // Настраиваем RecyclerView с адаптером
        setupRecyclerView()
        
        // Наблюдаем за данными
        observeViewModel()
    }
    
    private fun setupRecyclerView() {
        adapter = EntryAdapter(
            onEntryClicked = { entry ->
                // When clicking an entry, use edit mode instead of view mode since detailsFragment doesn't exist
                val bundle = Bundle().apply { putLong("entryId", entry.id) }
                findNavController().navigate(R.id.entryEditorFragment, bundle)
                // Show a message that we're opening in edit mode
                Snackbar.make(binding.root, "Открытие статьи '${entry.title}' в режиме редактирования", Snackbar.LENGTH_SHORT).show()
            },
            onEntryEdit = { entry ->
                // При клике на редактирование переходим к редактору с ID статьи
                val bundle = Bundle().apply { putLong("entryId", entry.id) }
                findNavController().navigate(R.id.entryEditorFragment, bundle)
            },
            onEntryDelete = { entry ->
                // При клике на удаление показываем диалог подтверждения
                confirmDeleteEntry(entry)
            }
        )
        
        binding.entriesRecyclerView.adapter = adapter
        binding.entriesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }
    
    private fun observeViewModel() {
        // Наблюдаем за списком статей
        viewModel.entries.observe(viewLifecycleOwner) { entries ->
            adapter.submitList(entries)
            
            // Отображаем пустой вид, если нет статей
            if (entries.isEmpty()) {
                binding.emptyView.visibility = View.VISIBLE
                binding.entriesRecyclerView.visibility = View.GONE
            } else {
                binding.emptyView.visibility = View.GONE
                binding.entriesRecyclerView.visibility = View.VISIBLE
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
    
    private fun navigateToAddEntry() {
        // Переход на экран добавления статьи
        val bundle = Bundle().apply { putLong("entryId", 0L) }
        findNavController().navigate(R.id.entryEditorFragment, bundle)
    }
    
    private fun confirmDeleteEntry(entry: Entry) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.confirm_delete)
            .setMessage(getString(R.string.confirm_delete_entry, entry.title))
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteEntry(entry)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    override fun onResume() {
        super.onResume()
        // Обновляем список статей при возвращении к фрагменту
        viewModel.loadEntries()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 