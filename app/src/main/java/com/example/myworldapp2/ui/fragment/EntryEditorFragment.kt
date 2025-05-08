package com.example.myworldapp2.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
// import androidx.navigation.fragment.navArgs - не используется
import com.example.myworldapp2.KidsEncyclopediaApp
import com.example.myworldapp2.R
import com.example.myworldapp2.data.entity.Entry
import com.example.myworldapp2.data.entity.Tag
import com.example.myworldapp2.databinding.FragmentEntryEditorBinding
import com.example.myworldapp2.ui.dialog.AddTagDialog
import com.example.myworldapp2.ui.viewmodel.EntryEditorViewModel
import com.example.myworldapp2.ui.viewmodel.EntryEditorViewModelFactory
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import android.content.res.ColorStateList
import android.graphics.Color

class EntryEditorFragment : Fragment(), AddTagDialog.TagDialogListener {

    private var _binding: FragmentEntryEditorBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: EntryEditorViewModel
    // private val args: EntryEditorFragmentArgs by navArgs() - не используется
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEntryEditorBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val app = requireActivity().application as KidsEncyclopediaApp
        val viewModelFactory = EntryEditorViewModelFactory(
            app.entryRepository,
            app.categoryRepository,
            app.tagRepository
        )
        
        viewModel = ViewModelProvider(this, viewModelFactory)[EntryEditorViewModel::class.java]
        
        // Получаем entryId из аргументов
        val entryId = arguments?.getLong("entryId") ?: 0L
        
        // Загружаем список категорий
        viewModel.loadCategories()
        
        // Загружаем список всех тегов
        viewModel.loadAllTags()
        
        // Если редактируем существующую статью
        if (entryId > 0) {
            viewModel.loadEntry(entryId)
            binding.saveButton.text = getString(R.string.save)
            binding.titleTextView.text = getString(R.string.edit_entry)
        } else {
            binding.titleTextView.text = getString(R.string.add_entry)
        }
        
        setupObservers()
        setupListeners()
    }
    
    private fun setupObservers() {
        // Наблюдаем за загруженной статьей
        viewModel.entry.observe(viewLifecycleOwner) { entry ->
            entry?.let {
                binding.entryTitleEditText.setText(it.title)
                binding.entryContentEditText.setText(it.content)
                // Не используем summary, так как его нет в Entry
            }
        }
        
        // Наблюдаем за списком категорий
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                categories.map { it.name }
            )
            binding.categorySpinner.adapter = adapter
            
            // Если редактируем статью, выбираем соответствующую категорию
            viewModel.entry.value?.let { entry ->
                val position = categories.indexOfFirst { it.id == entry.categoryId }
                if (position >= 0) {
                    binding.categorySpinner.setSelection(position)
                }
            }
        }
        
        // Наблюдаем за списком всех тегов
        viewModel.allTags.observe(viewLifecycleOwner) { tags ->
            updateAllTagsChipGroup(tags)
        }
        
        // Наблюдаем за списком тегов статьи
        viewModel.entryTags.observe(viewLifecycleOwner) { tags ->
            updateSelectedTagsChipGroup(tags)
        }
        
        // Наблюдаем за сообщениями об успешном сохранении
        viewModel.saveSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Snackbar.make(binding.root, R.string.entry_saved, Snackbar.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
        
        // Наблюдаем за ошибками
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty()) {
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            }
        }
    }
    
    private fun setupListeners() {
        binding.saveButton.setOnClickListener {
            saveEntry()
        }
        
        binding.cancelButton.setOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.addTagButton.setOnClickListener {
            showAddTagDialog()
        }
    }
    
    /**
     * Обновляет ChipGroup со всеми доступными тегами
     */
    private fun updateAllTagsChipGroup(tags: List<Tag>) {
        binding.allTagsChipGroup.removeAllViews()
        
        for (tag in tags) {
            val chip = createTagChip(tag)
            chip.isCheckable = true
            
            // Если тег уже выбран, устанавливаем checked = true
            if (viewModel.isTagSelected(tag.id)) {
                chip.isChecked = true
            }
            
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    viewModel.addTagToSelection(tag.id)
                } else {
                    viewModel.removeTagFromSelection(tag.id)
                }
                updateSelectedTagsView()
            }
            
            binding.allTagsChipGroup.addView(chip)
        }
    }
    
    /**
     * Обновляет ChipGroup с выбранными тегами статьи
     */
    private fun updateSelectedTagsChipGroup(tags: List<Tag>) {
        updateSelectedTagsView()
    }
    
    /**
     * Обновляет отображение выбранных тегов в верхнем ChipGroup
     */
    private fun updateSelectedTagsView() {
        binding.selectedTagsChipGroup.removeAllViews()
        
        val selectedIds = viewModel.selectedTagIds.value ?: mutableSetOf()
        val allTags = viewModel.allTags.value ?: listOf()
        
        // Фильтруем только выбранные теги
        val selectedTags = allTags.filter { selectedIds.contains(it.id) }
        
        for (tag in selectedTags) {
            val chip = createTagChip(tag)
            chip.isCloseIconVisible = true
            
            chip.setOnCloseIconClickListener {
                viewModel.removeTagFromSelection(tag.id)
                updateSelectedTagsView()
                updateAllTagsChipGroup(allTags)
            }
            
            binding.selectedTagsChipGroup.addView(chip)
        }
        
        // Показываем или скрываем группу тегов
        binding.selectedTagsChipGroup.visibility = if (selectedTags.isNotEmpty()) View.VISIBLE else View.GONE
    }
    
    /**
     * Создает чип для тега с заданным цветом
     */
    private fun createTagChip(tag: Tag): Chip {
        val chip = Chip(requireContext())
        chip.text = tag.name
        
        try {
            // Применяем цвет тега
            val color = Color.parseColor(tag.color)
            chip.chipBackgroundColor = ColorStateList.valueOf(color)
            
            // Настраиваем цвет текста в зависимости от яркости фона
            val isLight = calculateLuminance(color) > 0.5
            chip.setTextColor(if (isLight) Color.BLACK else Color.WHITE)
        } catch (e: Exception) {
            // В случае ошибки используем цвет по умолчанию
            chip.chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#4CAF50"))
            chip.setTextColor(Color.WHITE)
        }
        
        return chip
    }
    
    /**
     * Рассчитывает яркость цвета для определения контрастного цвета текста
     */
    private fun calculateLuminance(color: Int): Double {
        val r = (color shr 16 and 0xff) / 255.0
        val g = (color shr 8 and 0xff) / 255.0
        val b = (color and 0xff) / 255.0
        return 0.299 * r + 0.587 * g + 0.114 * b
    }
    
    /**
     * Показывает диалог добавления нового тега
     */
    private fun showAddTagDialog() {
        val dialog = AddTagDialog.newInstance()
        dialog.show(childFragmentManager, "AddTagDialog")
    }
    
    /**
     * Обработчик добавления нового тега из диалога
     */
    override fun onTagAdded(name: String, color: String) {
        viewModel.createAndSelectTag(name, color)
    }
    
    private fun saveEntry() {
        val title = binding.entryTitleEditText.text.toString().trim()
        val content = binding.entryContentEditText.text.toString().trim()
        val entrySummary = binding.entrySummaryEditText.text.toString().trim()
        
        if (title.isEmpty()) {
            binding.entryTitleEditText.error = getString(R.string.field_required)
            return
        }
        
        if (content.isEmpty()) {
            binding.entryContentEditText.error = getString(R.string.field_required)
            return
        }
        
        // Получаем выбранную категорию
        val categoryPosition = binding.categorySpinner.selectedItemPosition
        val categoryId = viewModel.getCategoryIdAtPosition(categoryPosition)
        
        // Получаем entryId из аргументов
        val entryId = arguments?.getLong("entryId") ?: 0L
        
        // Создаем объект Entry с корректными параметрами
        val entry = Entry(
            id = if (entryId > 0) entryId else 0,
            title = title,
            content = content,
            categoryId = categoryId,
            imageUrl = "", // пустая строка вместо null
            // Используем текущую дату для createdAt и updatedAt
            createdAt = java.util.Date(),
            updatedAt = java.util.Date()
        )
        
        // Сохраняем статью
        viewModel.saveEntry(entry)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 