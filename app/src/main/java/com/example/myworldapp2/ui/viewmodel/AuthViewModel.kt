package com.example.myworldapp2.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myworldapp2.data.entity.User
import com.example.myworldapp2.data.repository.UserRepository
import kotlinx.coroutines.launch
import java.util.Date

/**
 * ViewModel для обработки аутентификации пользователей (вход и регистрация)
 */
class AuthViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val TAG = "AuthViewModel" // Тег для логирования

    // Флаг загрузки
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Результат входа (успех/неудача)
    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> = _loginResult

    // Результат регистрации (успех/неудача)
    private val _registrationResult = MutableLiveData<Boolean>()
    val registrationResult: LiveData<Boolean> = _registrationResult

    // Флаг существования email
    private val _emailExists = MutableLiveData<Boolean>()
    val emailExists: LiveData<Boolean> = _emailExists

    // Сообщение об ошибке
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    /**
     * Вход пользователя в систему
     */
    fun login(email: String, password: String) {
        Log.d(TAG, "Попытка входа пользователя: $email")
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // Получаем пользователя из репозитория
                val user = userRepository.login(email, password)
                
                if (user != null) {
                    Log.d(TAG, "Успешный вход пользователя: ${user.id}, ${user.email}")
                    // Устанавливаем текущего пользователя
                    userRepository.setCurrentUser(user)
                    _loginResult.value = true
                } else {
                    Log.d(TAG, "Ошибка входа: неверные учетные данные")
                    _loginResult.value = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка входа: ${e.message}", e)
                _loginResult.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Регистрирует нового пользователя
     * @param name Имя пользователя
     * @param email Email пользователя
     * @param password Пароль пользователя
     */
    fun register(name: String, email: String, password: String) {
        _isLoading.value = true
        Log.d(TAG, "Начало регистрации пользователя: $name, $email")
        
        viewModelScope.launch {
            try {
                Log.d(TAG, "Проверка существования пользователя с email: $email")
                // Проверяем, существует ли пользователь с таким email
                val existingUser = userRepository.getUserByEmail(email)
                
                if (existingUser != null) {
                    Log.d(TAG, "Пользователь с email $email уже существует")
                    _emailExists.value = true
                    _registrationResult.value = false
                    _errorMessage.value = "Пользователь с таким email уже существует"
                    return@launch
                }
                
                Log.d(TAG, "Создание нового пользователя")
                // Создаем нового пользователя
                val passwordHash = userRepository.hashPassword(password)
                val newUser = User(
                    email = email,
                    passwordHash = passwordHash,
                    name = name,
                    role = "user", // Роль по умолчанию
                    createdAt = Date()
                )
                
                Log.d(TAG, "Сохранение пользователя в базе данных")
                // Сохраняем пользователя в базе данных
                try {
                    val userId = userRepository.insertUser(newUser)
                    Log.d(TAG, "Пользователь сохранен с ID: $userId")
                    
                    if (userId > 0) {
                        Log.d(TAG, "Регистрация успешна, получение пользователя по ID: $userId")
                        // Успешная регистрация, устанавливаем пользователя как текущего
                        val createdUser = userRepository.getUserById(userId)
                        if (createdUser != null) {
                            Log.d(TAG, "Пользователь установлен как текущий: ${createdUser.name}")
                            userRepository.setCurrentUser(createdUser)
                            _registrationResult.value = true
                        } else {
                            Log.e(TAG, "Не удалось получить созданного пользователя по ID: $userId")
                            _registrationResult.value = false
                            _errorMessage.value = "Не удалось получить данные нового пользователя"
                        }
                    } else {
                        Log.e(TAG, "Ошибка регистрации: ID пользователя <= 0: $userId")
                        // Ошибка регистрации
                        _registrationResult.value = false
                        _errorMessage.value = "Ошибка при создании пользователя в базе данных"
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Исключение при вставке пользователя: ${e.message}", e)
                    throw e
                }
            } catch (e: Exception) {
                // Ошибка регистрации
                Log.e(TAG, "Ошибка регистрации: ${e.message}", e)
                _registrationResult.value = false
                _errorMessage.value = "Ошибка регистрации: ${e.message}"
            } finally {
                _isLoading.value = false
                Log.d(TAG, "Завершение процесса регистрации")
            }
        }
    }

    /**
     * Проверяет, существует ли пользователь с указанным email
     * @param email Email для проверки
     */
    fun checkEmailExists(email: String) {
        viewModelScope.launch {
            try {
                val user = userRepository.getUserByEmail(email)
                _emailExists.value = user != null
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при проверке email: ${e.message}", e)
                _emailExists.value = false
            }
        }
    }

    /**
     * Регистрирует пользователя-администратора (для тестирования)
     */
    fun registerAdmin(name: String, email: String, password: String) {
        _isLoading.value = true
        Log.d(TAG, "Начало регистрации администратора: $name, $email")
        
        viewModelScope.launch {
            try {
                // Проверяем, существует ли пользователь с таким email
                val existingUser = userRepository.getUserByEmail(email)
                
                if (existingUser != null) {
                    Log.d(TAG, "Пользователь с email $email уже существует")
                    _emailExists.value = true
                    _registrationResult.value = false
                    _errorMessage.value = "Пользователь с таким email уже существует"
                    return@launch
                }
                
                // Создаем admin-пользователя
                val userId = userRepository.registerUser(email, password, name, "admin")
                
                if (userId > 0) {
                    // Получаем созданного пользователя
                    val adminUser = userRepository.getUserById(userId)
                    if (adminUser != null) {
                        // Устанавливаем как текущего пользователя
                        userRepository.setCurrentUser(adminUser)
                        _registrationResult.value = true
                    } else {
                        _registrationResult.value = false
                        _errorMessage.value = "Не удалось получить данные администратора"
                    }
                } else {
                    _registrationResult.value = false
                    _errorMessage.value = "Ошибка при создании администратора"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка регистрации администратора: ${e.message}", e)
                _registrationResult.value = false
                _errorMessage.value = "Ошибка регистрации: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
} 