package com.example.myworldapp2.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.myworldapp2.KidsEncyclopediaApp
import com.example.myworldapp2.R
import com.example.myworldapp2.databinding.FragmentRegisterBinding
import com.example.myworldapp2.ui.viewmodel.AuthViewModel
import com.example.myworldapp2.ui.viewmodel.AuthViewModelFactory
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AuthViewModel
    
    // Минимальная длина пароля
    private val MIN_PASSWORD_LENGTH = 8
    
    private val TAG = "RegisterFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Получаем экземпляр приложения
        val app = requireActivity().application as KidsEncyclopediaApp
        
        // Создаем фабрику для ViewModel с необходимыми зависимостями
        val viewModelFactory = AuthViewModelFactory(app.userRepository)
        
        // Инициализируем ViewModel
        viewModel = ViewModelProvider(this, viewModelFactory)[AuthViewModel::class.java]
        
        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        // Настройка полей ввода для валидации при изменении текста
        binding.etName.doAfterTextChanged { validateForm() }
        binding.etEmail.doAfterTextChanged { validateForm() }
        binding.etPassword.doAfterTextChanged { validateForm() }
        binding.etConfirmPassword.doAfterTextChanged { validateForm() }
        
        // Настройка кнопки регистрации
        binding.btnRegister.setOnClickListener {
            register()
        }
        
        // Секретное сочетание для регистрации администратора
        binding.tvLogin.setOnLongClickListener {
            showAdminRegistrationDialog()
            true
        }
        
        // Ссылка на вход
        binding.tvLogin.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }

    private fun observeViewModel() {
        viewModel.registrationResult.observe(viewLifecycleOwner) { result ->
            binding.progressBar.visibility = View.GONE
            
            if (result) {
                // Успешная регистрация - переходим на главный экран
                Log.d(TAG, "Регистрация успешна, переход на главный экран")
                findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
                Snackbar.make(binding.root, getString(R.string.registration_success), Snackbar.LENGTH_SHORT).show()
            } else {
                // Ошибка регистрации
                Log.d(TAG, "Ошибка регистрации, показываем сообщение об ошибке")
                try {
                    val errorMessage = viewModel.errorMessage.value ?: getString(R.string.registration_error)
                    Log.d(TAG, "Сообщение об ошибке: $errorMessage")
                    Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Log.e(TAG, "Ошибка при показе Snackbar: ${e.message}", e)
                    // Если Snackbar вызывает ошибку, используем простой Toast
                    try {
                        android.widget.Toast.makeText(context, getString(R.string.registration_error), android.widget.Toast.LENGTH_SHORT).show()
                    } catch (e2: Exception) {
                        Log.e(TAG, "Ошибка при показе Toast: ${e2.message}", e2)
                    }
                }
            }
        }
        
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            Log.d(TAG, "Получено новое сообщение об ошибке: $errorMessage")
            if (errorMessage.isNotEmpty() && !viewModel.registrationResult.value!!) {
                try {
                    Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Log.e(TAG, "Ошибка при показе сообщения об ошибке через Snackbar: ${e.message}", e)
                    try {
                        android.widget.Toast.makeText(context, errorMessage, android.widget.Toast.LENGTH_SHORT).show()
                    } catch (e2: Exception) {
                        Log.e(TAG, "Ошибка при показе Toast: ${e2.message}", e2)
                    }
                }
            }
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            Log.d(TAG, "Изменение состояния загрузки: $isLoading")
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnRegister.isEnabled = !isLoading
        }
        
        viewModel.emailExists.observe(viewLifecycleOwner) { exists ->
            Log.d(TAG, "Email существует: $exists")
            if (exists) {
                binding.tilEmail.error = getString(R.string.email_already_exists)
                binding.btnRegister.isEnabled = false
            }
        }
    }

    private fun register() {
        Log.d(TAG, "Вызов метода регистрации")
        if (!validateForm(true)) {
            Log.d(TAG, "Валидация формы не пройдена")
            return
        }
        
        val name = binding.etName.text.toString()
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        
        Log.d(TAG, "Начинаем регистрацию пользователя: $name, $email")
        viewModel.register(name, email, password)
    }

    private fun validateForm(showErrors: Boolean = false): Boolean {
        val name = binding.etName.text.toString()
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()
        
        var isValid = true
        
        // Проверка имени
        if (name.isEmpty()) {
            if (showErrors) {
                binding.tilName.error = getString(R.string.error_field_required)
            }
            isValid = false
        } else {
            binding.tilName.error = null
        }
        
        // Проверка email
        if (email.isEmpty()) {
            if (showErrors) {
                binding.tilEmail.error = getString(R.string.error_field_required)
            }
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (showErrors) {
                binding.tilEmail.error = getString(R.string.error_invalid_email)
            }
            isValid = false
        } else {
            binding.tilEmail.error = null
        }
        
        // Проверка пароля
        if (password.isEmpty()) {
            if (showErrors) {
                binding.tilPassword.error = getString(R.string.error_field_required)
            }
            isValid = false
        } else if (password.length < MIN_PASSWORD_LENGTH) {
            if (showErrors) {
                binding.tilPassword.error = getString(R.string.error_password_too_short, MIN_PASSWORD_LENGTH)
            }
            isValid = false
        } else {
            binding.tilPassword.error = null
        }
        
        // Проверка подтверждения пароля
        if (confirmPassword.isEmpty()) {
            if (showErrors) {
                binding.tilConfirmPassword.error = getString(R.string.error_field_required)
            }
            isValid = false
        } else if (password != confirmPassword) {
            if (showErrors) {
                binding.tilConfirmPassword.error = getString(R.string.password_mismatch)
            }
            isValid = false
        } else {
            binding.tilConfirmPassword.error = null
        }
        
        binding.btnRegister.isEnabled = isValid
        return isValid
    }

    /**
     * Показывает диалог для регистрации администратора
     */
    private fun showAdminRegistrationDialog() {
        val name = binding.etName.text.toString()
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()
        
        // Проверяем, что все поля заполнены и пароли совпадают
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || password != confirmPassword) {
            Snackbar.make(binding.root, "Заполните все поля для регистрации администратора", Snackbar.LENGTH_SHORT).show()
            return
        }
        
        // Показываем диалог подтверждения
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Регистрация администратора")
            .setMessage("Вы уверены, что хотите зарегистрировать этого пользователя как администратора?")
            .setPositiveButton("Да") { _, _ ->
                registerAdmin(name, email, password)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
    
    /**
     * Регистрирует пользователя как администратора
     */
    private fun registerAdmin(name: String, email: String, password: String) {
        Log.d(TAG, "Регистрация администратора: $name, $email")
        viewModel.registerAdmin(name, email, password)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 