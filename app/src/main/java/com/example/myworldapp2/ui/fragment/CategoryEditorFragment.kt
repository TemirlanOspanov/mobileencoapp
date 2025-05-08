package com.example.myworldapp2.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.myworldapp2.KidsEncyclopediaApp
import com.example.myworldapp2.R
import com.example.myworldapp2.databinding.FragmentCategoryEditorBinding
import com.example.myworldapp2.ui.viewmodel.CategoryEditorViewModel
import com.example.myworldapp2.ui.viewmodel.CategoryEditorViewModelFactory
import com.google.android.material.snackbar.Snackbar

class CategoryEditorFragment : Fragment() {

    private var _binding: FragmentCategoryEditorBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CategoryEditorViewModel
    private val args: CategoryEditorFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val app = requireActivity().application as KidsEncyclopediaApp
        val factory = CategoryEditorViewModelFactory(app.categoryRepository)
        viewModel = ViewModelProvider(this, factory)[CategoryEditorViewModel::class.java]

        setupToolbar()
        setupObservers()
        setupListeners()

        if (args.categoryId != 0L) {
            viewModel.loadCategory(args.categoryId)
            binding.toolbar.title = getString(R.string.edit_category)
        } else {
            binding.toolbar.title = getString(R.string.add_category)
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupObservers() {
        viewModel.category.observe(viewLifecycleOwner) { category ->
            category?.let {
                binding.etCategoryName.setText(it.name)
                binding.etCategoryDescription.setText(it.description)
                binding.etCategoryColor.setText(it.color)
                binding.etCategoryIcon.setText(it.icon)
                updateColorPreview(it.color)
            }
        }

        viewModel.saveResult.observe(viewLifecycleOwner) { result ->
            result.fold(
                onSuccess = {
                    Snackbar.make(binding.root, R.string.category_saved, Snackbar.LENGTH_LONG).show()
                    findNavController().navigateUp()
                },
                onFailure = { exception ->
                    Snackbar.make(binding.root, getString(R.string.error_saving_category, exception.message), Snackbar.LENGTH_LONG).show()
                }
            )
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Handle loading state, e.g., show/hide progress bar
            // For now, just log or disable UI elements
        }
    }

    private fun setupListeners() {
        binding.saveButton.setOnClickListener {
            saveCategory()
        }

        binding.etCategoryColor.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateColorPreview(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun updateColorPreview(colorHex: String) {
        try {
            val color = Color.parseColor(colorHex)
            binding.colorPreview.setBackgroundColor(color)
            binding.tilCategoryColor.error = null
        } catch (e: IllegalArgumentException) {
            binding.colorPreview.setBackgroundColor(Color.TRANSPARENT) // Or a default error color
             binding.tilCategoryColor.error = getString(R.string.invalid_color_hex)
        }
    }

    private fun saveCategory() {
        val name = binding.etCategoryName.text.toString().trim()
        val description = binding.etCategoryDescription.text.toString().trim()
        val color = binding.etCategoryColor.text.toString().trim()
        val icon = binding.etCategoryIcon.text.toString().trim()

        if (name.isEmpty()) {
            binding.tilCategoryName.error = getString(R.string.field_required)
            return
        }
         binding.tilCategoryName.error = null

        if (color.isEmpty() || !color.matches(Regex("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$"))) {
            binding.tilCategoryColor.error = getString(R.string.invalid_color_hex) // More specific error
            return
        }
        binding.tilCategoryColor.error = null

        viewModel.saveCategory(args.categoryId, name, description, color, icon)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 