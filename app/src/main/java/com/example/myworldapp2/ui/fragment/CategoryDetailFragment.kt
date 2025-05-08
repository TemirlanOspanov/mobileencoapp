package com.example.myworldapp2.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myworldapp2.KidsEncyclopediaApp
import com.example.myworldapp2.R
import com.example.myworldapp2.data.entity.Entry
import com.example.myworldapp2.databinding.FragmentCategoryDetailBinding
import com.example.myworldapp2.ui.adapter.EntryAdapter
import com.example.myworldapp2.ui.viewmodel.CategoryDetailViewModel
import com.example.myworldapp2.ui.viewmodel.CategoryDetailViewModelFactory
import kotlinx.coroutines.launch

class CategoryDetailFragment : Fragment() {

    private var _binding: FragmentCategoryDetailBinding? = null
    private val binding get() = _binding!!

    private val args: CategoryDetailFragmentArgs by navArgs()
    private lateinit var viewModel: CategoryDetailViewModel
    private lateinit var entryAdapter: EntryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get application instance
        val app = requireActivity().application as KidsEncyclopediaApp
        
        // Create ViewModel factory with dependencies from app
        val viewModelFactory = CategoryDetailViewModelFactory(
            app.categoryRepository,
            app.entryRepository,
            app.userRepository
        ).apply {
            setCategoryId(args.categoryId)
        }
        
        // Initialize ViewModel
        viewModel = ViewModelProvider(this, viewModelFactory)[CategoryDetailViewModel::class.java]

        setupToolbar()
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        entryAdapter = EntryAdapter(
            onEntryClicked = { entry ->
                navigateToEntryDetail(entry)
            }
        )

        binding.rvEntries.apply {
            adapter = entryAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        }
    }

    private fun observeViewModel() {
        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe category
        viewModel.category.observe(viewLifecycleOwner) { category ->
            if (category != null) {
                binding.tvCategoryName.text = category.name
                binding.tvCategoryDescription.text = category.description
                
                // Set category color and icon
                binding.categoryHeader.setBackgroundColor(
                    resources.getColor(getCategoryColorResourceId(category.id), null)
                )
                binding.imgCategoryIcon.setImageResource(getCategoryIconResourceId(category.id))
            }
        }

        // Observe entries
        viewModel.entries.observe(viewLifecycleOwner) { entries ->
            if (entries.isNullOrEmpty()) {
                binding.emptyView.visibility = View.VISIBLE
                binding.rvEntries.visibility = View.GONE
            } else {
                binding.emptyView.visibility = View.GONE
                binding.rvEntries.visibility = View.VISIBLE
                entryAdapter.submitList(entries)
            }
        }

        // Observe progress
        viewModel.categoryProgress.observe(viewLifecycleOwner) { progress ->
            binding.progressCategory.progress = (progress * 100).toInt()
            binding.tvCategoryProgress.text = getString(
                R.string.category_progress_format,
                (progress * 100).toInt()
            )
        }
    }

    private fun navigateToEntryDetail(entry: Entry) {
        // Mark as read for calculation progress
        viewModel.markEntryAsRead(entry.id)
        
        // Navigate to entry detail
        val action = CategoryDetailFragmentDirections
            .actionCategoryDetailFragmentToEntryDetailFragment(entry.id)
        findNavController().navigate(action)
    }

    private fun getCategoryColorResourceId(categoryId: Long): Int {
        // Return different colors based on category ID
        return when (categoryId % 6) {
            0L -> R.color.category_animals
            1L -> R.color.category_plants
            2L -> R.color.category_space
            3L -> R.color.category_history
            4L -> R.color.category_technology
            else -> R.color.category_science
        }
    }

    private fun getCategoryIconResourceId(categoryId: Long): Int {
        // Return different icons based on category ID
        // Using Android system icons as placeholders
        return when (categoryId % 6) {
            0L -> android.R.drawable.ic_menu_compass
            1L -> android.R.drawable.ic_menu_gallery
            2L -> android.R.drawable.ic_menu_mapmode
            3L -> android.R.drawable.ic_menu_today
            4L -> android.R.drawable.ic_menu_preferences
            else -> android.R.drawable.ic_menu_info_details
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 