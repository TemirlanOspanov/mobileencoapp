package com.example.myworldapp2.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.myworldapp2.KidsEncyclopediaApp
import com.example.myworldapp2.R
import com.example.myworldapp2.databinding.FragmentChangePasswordBinding
import com.example.myworldapp2.ui.viewmodel.ProfileViewModel
import com.example.myworldapp2.ui.viewmodel.ProfileViewModelFactory
import com.google.android.material.snackbar.Snackbar

class ChangePasswordFragment : Fragment() {

    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProfileViewModel

    // Минимальная длина пароля
    private val MIN_PASSWORD_LENGTH = 8

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Настраиваем тулбар
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        // Получаем экземпляр приложения
        val app = requireActivity().application as KidsEncyclopediaApp
        
        // Создаем фабрику для ViewModel с необходимыми зависимостями
        val viewModelFactory = ProfileViewModelFactory(app.userRepository)
        
        // Инициализируем ViewModel
        viewModel = ViewModelProvider(this, viewModelFactory)[ProfileViewModel::class.java]
        
        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        // Настройка полей ввода для валидации при изменении текста
        binding.etCurrentPassword.doAfterTextChanged { validateForm() }
        binding.etNewPassword.doAfterTextChanged { validateForm() }
        binding.etConfirmPassword.doAfterTextChanged { validateForm() }
        
        // Настройка кнопок действия
        binding.btnSave.setOnClickListener {
            changePassword()
        }
        
        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeViewModel() {
        // Наблюдаем за состоянием загрузки
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSave.isEnabled = !isLoading && validateForm(false)
            binding.btnCancel.isEnabled = !isLoading
        }
        
        // Наблюдаем за сообщениями об ошибках
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty()) {
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            }
        }
        
        // Наблюдаем за успешным изменением пароля
        viewModel.passwordChangeSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Snackbar.make(binding.root, R.string.password_changed_successfully, Snackbar.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    private fun validateForm(showErrors: Boolean = true): Boolean {
        val currentPassword = binding.etCurrentPassword.text.toString()
        val newPassword = binding.etNewPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()
        
        var isValid = true
        
        // Проверка текущего пароля
        if (currentPassword.isEmpty()) {
            if (showErrors) {
                binding.tilCurrentPassword.error = getString(R.string.error_field_required)
            }
            isValid = false
        } else {
            binding.tilCurrentPassword.error = null
        }
        
        // Проверка нового пароля
        if (newPassword.isEmpty()) {
            if (showErrors) {
                binding.tilNewPassword.error = getString(R.string.error_field_required)
            }
            isValid = false
        } else if (newPassword.length < MIN_PASSWORD_LENGTH) {
            if (showErrors) {
                binding.tilNewPassword.error = getString(R.string.error_password_too_short, MIN_PASSWORD_LENGTH)
            }
            isValid = false
        } else {
            binding.tilNewPassword.error = null
        }
        
        // Проверка подтверждения пароля
        if (confirmPassword.isEmpty()) {
            if (showErrors) {
                binding.tilConfirmPassword.error = getString(R.string.error_field_required)
            }
            isValid = false
        } else if (newPassword != confirmPassword) {
            if (showErrors) {
                binding.tilConfirmPassword.error = getString(R.string.error_passwords_dont_match)
            }
            isValid = false
        } else {
            binding.tilConfirmPassword.error = null
        }
        
        binding.btnSave.isEnabled = isValid
        return isValid
    }

    private fun changePassword() {
        if (!validateForm()) return
        
        val currentPassword = binding.etCurrentPassword.text.toString()
        val newPassword = binding.etNewPassword.text.toString()
        
        viewModel.changePassword(currentPassword, newPassword)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 