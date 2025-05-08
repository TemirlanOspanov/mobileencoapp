package com.example.myworldapp2.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myworldapp2.KidsEncyclopediaApp
import com.example.myworldapp2.R
import com.example.myworldapp2.data.entity.User
import com.example.myworldapp2.databinding.FragmentUserManagementBinding
import com.example.myworldapp2.ui.adapter.UserAdapter
import com.example.myworldapp2.ui.dialog.EditUserDialog
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Фрагмент для управления пользователями в админ-панели
 */
class UserManagementFragment : Fragment() {

    private var _binding: FragmentUserManagementBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var adapter: UserAdapter
    private lateinit var app: KidsEncyclopediaApp
    
    private var currentFilterRole: String? = null // null - все пользователи
    private var currentSearchQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserManagementBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        app = requireActivity().application as KidsEncyclopediaApp
        
        // Настраиваем RecyclerView и адаптер
        setupRecyclerView()
        
        // Настраиваем поисковую строку
        setupSearchView()
        
        // Настраиваем фильтры по роли
        setupFilterChips()
        
        // Настраиваем FAB для добавления пользователей
        setupFab()
        
        // Загружаем данные
        loadData()
    }
    
    private fun setupRecyclerView() {
        adapter = UserAdapter(
            onUserClick = { user ->
                // Открываем диалог редактирования
                showEditUserDialog(user.id)
            },
            onEditRole = { user ->
                // Показываем диалог изменения роли
                showChangeRoleDialog(user)
            },
            onDeleteUser = { user ->
                // Показываем диалог подтверждения удаления
                showDeleteUserDialog(user)
            }
        )
        
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }
    
    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                filterUsers(query)
                return true
            }
            
            override fun onQueryTextChange(newText: String): Boolean {
                filterUsers(newText)
                return true
            }
        })
    }
    
    private fun filterUsers(query: String) {
        currentSearchQuery = query
        adapter.filter(query)
        
        // Показываем/скрываем пустое состояние
        updateEmptyState()
    }
    
    private fun setupFilterChips() {
        // Обработчик для чипа "Все пользователи"
        binding.chipAllUsers.setOnCheckedChangeListener { chip, isChecked ->
            if (isChecked) {
                currentFilterRole = null
                uncheckOtherChips(chip.id)
                adapter.filterByRole(null)
                updateEmptyState()
            }
        }
        
        // Обработчик для чипа "Пользователи"
        binding.chipUser.setOnCheckedChangeListener { chip, isChecked ->
            if (isChecked) {
                currentFilterRole = "user"
                uncheckOtherChips(chip.id)
                adapter.filterByRole("user")
                updateEmptyState()
            }
        }
        
        // Обработчик для чипа "Администраторы"
        binding.chipAdmin.setOnCheckedChangeListener { chip, isChecked ->
            if (isChecked) {
                currentFilterRole = "admin"
                uncheckOtherChips(chip.id)
                adapter.filterByRole("admin")
                updateEmptyState()
            }
        }
    }
    
    private fun uncheckOtherChips(selectedChipId: Int) {
        val chips = listOf(
            binding.chipAllUsers,
            binding.chipUser,
            binding.chipAdmin
        )
        
        for (chip in chips) {
            if (chip.id != selectedChipId) {
                chip.isChecked = false
            }
        }
    }
    
    private fun setupFab() {
        binding.fabAddUser.setOnClickListener {
            showEditUserDialog()
        }
    }
    
    private fun loadData() {
        // Показываем индикатор загрузки
        binding.progressBar.visibility = View.VISIBLE
        binding.emptyView.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        
        // Наблюдаем за списком пользователей
        app.userRepository.getAllUsers().observe(viewLifecycleOwner) { users ->
            // Обновляем адаптер
            adapter.setUsers(users)
            
            // Применяем текущие фильтры
            if (currentSearchQuery.isNotEmpty()) {
                adapter.filter(currentSearchQuery)
            }
            
            if (currentFilterRole != null) {
                adapter.filterByRole(currentFilterRole)
            }
            
            // Скрываем индикатор загрузки
            binding.progressBar.visibility = View.GONE
            
            // Обновляем пустое состояние
            updateEmptyState()
        }
    }
    
    private fun updateEmptyState() {
        if (adapter.itemCount == 0) {
            binding.recyclerView.visibility = View.GONE
            binding.emptyView.visibility = View.VISIBLE
        } else {
            binding.recyclerView.visibility = View.VISIBLE
            binding.emptyView.visibility = View.GONE
        }
    }
    
    private fun showEditUserDialog(userId: Long = 0) {
        val dialog = EditUserDialog.newInstance(userId)
        dialog.show(childFragmentManager, "EditUserDialog")
    }
    
    private fun showChangeRoleDialog(user: User) {
        val newRole = if (user.role == "admin") "user" else "admin"
        val message = if (newRole == "admin") 
            "Сделать пользователя ${user.name} администратором?" 
        else 
            "Убрать права администратора у ${user.name}?"
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Изменение роли")
            .setMessage(message)
            .setPositiveButton("Да") { _, _ ->
                changeUserRole(user, newRole)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
    
    private fun changeUserRole(user: User, newRole: String) {
        val updatedUser = user.copy(role = newRole)
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                app.userRepository.updateUser(updatedUser)
                
                withContext(Dispatchers.Main) {
                    val message = if (newRole == "admin") 
                        "Пользователь ${user.name} теперь администратор" 
                    else 
                        "Пользователь ${user.name} больше не администратор"
                    
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Ошибка изменения роли: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    
    private fun showDeleteUserDialog(user: User) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Удаление пользователя")
            .setMessage("Вы уверены, что хотите удалить пользователя ${user.name}?")
            .setPositiveButton("Да") { _, _ ->
                deleteUser(user)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
    
    private fun deleteUser(user: User) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                app.userRepository.deleteUser(user)
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Пользователь ${user.name} удален",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Ошибка удаления пользователя: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 