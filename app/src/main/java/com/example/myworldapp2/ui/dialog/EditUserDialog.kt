package com.example.myworldapp2.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.myworldapp2.KidsEncyclopediaApp
import com.example.myworldapp2.R
import com.example.myworldapp2.data.entity.User
import com.example.myworldapp2.databinding.DialogEditUserBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditUserDialog : DialogFragment() {

    private var _binding: DialogEditUserBinding? = null
    private val binding get() = _binding!!

    private var user: User? = null
    private var userId: Long = 0
    private var isEditMode = false

    companion object {
        private const val ARG_USER_ID = "user_id"

        fun newInstance(userId: Long = 0): EditUserDialog {
            val fragment = EditUserDialog()
            val args = Bundle().apply {
                putLong(ARG_USER_ID, userId)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getLong(ARG_USER_ID, 0)
            isEditMode = userId > 0
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogEditUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Настраиваем заголовок диалога
        binding.tvDialogTitle.text = if (isEditMode) "Редактирование пользователя" else "Добавление пользователя"

        // Настраиваем кнопки
        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnSave.setOnClickListener { saveUser() }

        // Если режим редактирования, загружаем данные пользователя
        if (isEditMode) {
            loadUserData()
            // В режиме редактирования скрываем поле пароля (изменение пароля отдельной функцией)
            binding.passwordLayout.visibility = View.GONE
        }
    }

    private fun loadUserData() {
        val app = requireActivity().application as KidsEncyclopediaApp
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Получаем данные пользователя
                user = app.userRepository.getUserById(userId)
                
                // Обновляем UI в главном потоке
                withContext(Dispatchers.Main) {
                    user?.let { populateUserData(it) }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Ошибка загрузки данных: ${e.message}", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
            }
        }
    }

    private fun populateUserData(user: User) {
        binding.etName.setText(user.name)
        binding.etEmail.setText(user.email)
        
        // Выбираем соответствующую роль
        when (user.role) {
            "admin" -> binding.rbAdmin.isChecked = true
            else -> binding.rbUser.isChecked = true
        }
    }

    private fun saveUser() {
        // Получаем значения из полей ввода
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val role = if (binding.rbAdmin.isChecked) "admin" else "user"

        // Проверяем заполнение обязательных полей
        if (name.isEmpty()) {
            binding.nameLayout.error = "Введите имя"
            return
        }

        if (email.isEmpty()) {
            binding.emailLayout.error = "Введите email"
            return
        }

        if (!isEditMode && password.isEmpty()) {
            binding.passwordLayout.error = "Введите пароль"
            return
        }

        // Получаем доступ к репозиторию пользователей
        val app = requireActivity().application as KidsEncyclopediaApp
        
        // Запускаем корутину для сохранения пользователя
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (isEditMode) {
                    // Обновляем существующего пользователя
                    user?.let {
                        val updatedUser = it.copy(
                            name = name,
                            email = email,
                            role = role
                        )
                        app.userRepository.updateUser(updatedUser)
                    }
                } else {
                    // Создаем нового пользователя
                    app.userRepository.registerUser(
                        email = email,
                        password = password,
                        name = name,
                        role = role
                    )
                }
                
                // Показываем сообщение об успехе и закрываем диалог
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        if (isEditMode) "Пользователь обновлен" else "Пользователь добавлен",
                        Toast.LENGTH_SHORT
                    ).show()
                    dismiss()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Ошибка сохранения: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 