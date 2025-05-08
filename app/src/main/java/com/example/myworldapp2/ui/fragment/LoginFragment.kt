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
import com.example.myworldapp2.databinding.FragmentLoginBinding
import com.example.myworldapp2.ui.viewmodel.AuthViewModel
import com.example.myworldapp2.ui.viewmodel.AuthViewModelFactory
import com.google.android.material.snackbar.Snackbar

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
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
        
        // Проверяем, есть ли сохраненная сессия, и автоматически переходим на главный экран
        checkSavedSession(app)
        
        setupViews()
        observeViewModel()
    }

    /**
     * Проверяет наличие сохраненной сессии и автоматически переходит на главный экран при необходимости
     */
    private fun checkSavedSession(app: KidsEncyclopediaApp) {
        if (app.sessionManager.isLoggedIn()) {
            val userId = app.sessionManager.getUserId()
            val userEmail = app.sessionManager.getUserEmail()
            
            Log.d("LoginFragment", "Обнаружена сохраненная сессия для пользователя: $userId ($userEmail)")
            
            // Проверяем, что мы действительно находимся на экране LoginFragment перед навигацией
            val currentDestination = findNavController().currentDestination?.id
            if (currentDestination == R.id.loginFragment) {
                Log.d("LoginFragment", "Переходим на главный экран")
                // Если сессия найдена, переходим на главный экран
                findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
            } else {
                Log.d("LoginFragment", "Уже не на экране логина, пропускаем навигацию")
            }
        } else {
            Log.d("LoginFragment", "Сохраненная сессия не найдена, остаемся на экране входа")
        }
    }

    private fun setupViews() {
        // Настройка полей ввода для валидации при изменении текста
        binding.etEmail.doAfterTextChanged { validateForm() }
        binding.etPassword.doAfterTextChanged { validateForm() }
        
        // Настройка кнопки входа
        binding.btnLogin.setOnClickListener {
            login()
        }
        
        // Ссылка на регистрацию
        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
        
        // Ссылка на восстановление пароля
        binding.tvForgotPassword.setOnClickListener {
            // TODO: Реализовать восстановление пароля
            Snackbar.make(binding.root, "Функция восстановления пароля будет доступна позже", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        viewModel.loginResult.observe(viewLifecycleOwner) { result ->
            binding.progressBar.visibility = View.GONE
            
            if (result) {
                // Успешный вход - переходим на главный экран
                findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
            } else {
                // Ошибка входа
                Snackbar.make(binding.root, getString(R.string.login_error), Snackbar.LENGTH_SHORT).show()
            }
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnLogin.isEnabled = !isLoading
        }
    }

    private fun login() {
        if (!validateForm(true)) return
        
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        
        viewModel.login(email, password)
    }

    private fun validateForm(showErrors: Boolean = false): Boolean {
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        
        var isValid = true
        
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
        } else {
            binding.tilPassword.error = null
        }
        
        binding.btnLogin.isEnabled = isValid
        return isValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 