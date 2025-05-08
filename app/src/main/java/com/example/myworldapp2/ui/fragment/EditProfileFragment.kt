package com.example.myworldapp2.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.myworldapp2.KidsEncyclopediaApp
import com.example.myworldapp2.R
import com.example.myworldapp2.databinding.FragmentEditProfileBinding
import com.example.myworldapp2.ui.viewmodel.ProfileViewModel
import com.example.myworldapp2.ui.viewmodel.ProfileViewModelFactory
import com.google.android.material.snackbar.Snackbar

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProfileViewModel
    private var selectedImageUri: Uri? = null

    // Регистрируем запуск активности для выбора изображения
    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                binding.ivAvatar.setImageURI(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
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
        // Настройка кнопки изменения аватара
        binding.btnChangeAvatar.setOnClickListener {
            openImageSelector()
        }
        
        // Настройка полей ввода
        binding.etName.doAfterTextChanged { text ->
            validateForm()
        }
        
        binding.etEmail.doAfterTextChanged { text ->
            validateForm()
        }
        
        // Настройка кнопок действия
        binding.btnSave.setOnClickListener {
            saveChanges()
        }
        
        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeViewModel() {
        // Наблюдаем за данными пользователя
        viewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let { // Use safe call or check for null
                // Заполняем поля формы данными пользователя
                binding.etName.setText(it.name)
                binding.etEmail.setText(it.email)
                binding.tvRole.text = when (it.role) {
                    "admin" -> getString(R.string.role_admin)
                    "editor" -> getString(R.string.role_editor)
                    else -> getString(R.string.role_user)
                }
                
                // TODO: Загрузка аватара пользователя
                // Glide.with(this).load(user.avatarUrl).into(binding.ivAvatar)
            }
        }
        
        // Наблюдаем за состоянием загрузки
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // binding.progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
             if (_binding != null) { // Check if binding is not null
                // binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
            binding.btnSave.isEnabled = !isLoading
            binding.btnCancel.isEnabled = !isLoading
        }
        
        // Наблюдаем за сообщениями об ошибках
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty()) {
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            }
        }
        
        // Наблюдаем за успешным обновлением профиля
        viewModel.profileUpdateSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Snackbar.make(binding.root, R.string.profile_updated_successfully, Snackbar.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    private fun validateForm(): Boolean {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        
        var isValid = true
        
        // Проверка имени
        if (name.isEmpty()) {
            binding.tilName.error = getString(R.string.error_field_required)
            isValid = false
        } else {
            binding.tilName.error = null
        }
        
        // Проверка email
        if (email.isEmpty()) {
            binding.tilEmail.error = getString(R.string.error_field_required)
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.error_invalid_email)
            isValid = false
        } else {
            binding.tilEmail.error = null
        }
        
        binding.btnSave.isEnabled = isValid
        return isValid
    }

    private fun saveChanges() {
        if (!validateForm()) return
        
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        
        viewModel.updateUserProfile(name, email, selectedImageUri)
    }

    private fun openImageSelector() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        getContent.launch(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 