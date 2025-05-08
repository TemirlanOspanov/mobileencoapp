package com.example.myworldapp2.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myworldapp2.KidsEncyclopediaApp
import com.example.myworldapp2.R
import com.example.myworldapp2.databinding.FragmentAdminPanelBinding
import com.google.android.material.snackbar.Snackbar

/**
 * Фрагмент для административной панели
 */
class AdminPanelFragment : Fragment() {

    private var _binding: FragmentAdminPanelBinding? = null
    private val binding get() = _binding!!

    private lateinit var app: KidsEncyclopediaApp

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminPanelBinding.inflate(inflater, container, false)
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
        
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        // Карточка управления контентом
        binding.cardContentManagement.setOnClickListener {
            findNavController().navigate(
                AdminPanelFragmentDirections.actionAdminPanelFragmentToContentManagementFragment()
            )
        }
        
        // Карточка управления пользователями
        binding.cardUserManagement.setOnClickListener {
            findNavController().navigate(
                AdminPanelFragmentDirections.actionAdminPanelFragmentToUserManagementFragment()
            )
        }
        
        // Карточка управления тегами
        binding.cardTagManagement.setOnClickListener {
            findNavController().navigate(
                AdminPanelFragmentDirections.actionAdminPanelFragmentToTagManagementFragment()
            )
        }
        
        // Карточка аналитики
        binding.cardAnalytics.setOnClickListener {
            findNavController().navigate(
                AdminPanelFragmentDirections.actionAdminPanelFragmentToAnalyticsFragment()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 