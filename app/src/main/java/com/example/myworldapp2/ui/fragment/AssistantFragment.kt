package com.example.myworldapp2.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myworldapp2.R
import com.example.myworldapp2.data.model.Message
import com.example.myworldapp2.data.service.ChatService
import com.example.myworldapp2.ui.adapter.MessageAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

/**
 * Фрагмент для экрана чата с AI-помощником
 */
class AssistantFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MessageAdapter
    private lateinit var editTextMessage: EditText
    private lateinit var buttonSend: FloatingActionButton
    private lateinit var buttonMic: ImageButton
    private lateinit var buttonAttach: ImageButton
    private lateinit var progressBar: ProgressBar

    // ChatService для взаимодействия с Gemini API
    private lateinit var chatService: ChatService

    // История сообщений
    private val messages = mutableListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Инициализация ChatService
        chatService = ChatService(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_assistant, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Скрываем ActionBar из MainActivity
        (activity as? AppCompatActivity)?.supportActionBar?.hide()

        // Инициализация UI
        recyclerView = view.findViewById(R.id.recyclerViewMessages)
        editTextMessage = view.findViewById(R.id.editTextMessage)
        buttonSend = view.findViewById(R.id.buttonSend)
        buttonMic = view.findViewById(R.id.buttonMic)
        buttonAttach = view.findViewById(R.id.buttonAttach)
        progressBar = view.findViewById(R.id.progressBar)

        view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Настройка RecyclerView
        adapter = MessageAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }

        // Обработчики событий
        buttonSend.isEnabled = false
        buttonSend.setOnClickListener {
            val messageText = editTextMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
            }
        }

        editTextMessage.doAfterTextChanged {
            buttonSend.isEnabled = it.toString().trim().isNotEmpty()
        }

        // Проверяем, есть ли сообщения в истории
        if (messages.isEmpty()) {
            addBotMessage("Привет! Я твой AI-помощник по энциклопедии природы. Что ты хочешь узнать?")
        }

        // Обновляем адаптер
        adapter.submitList(messages.toList())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Показываем ActionBar снова при уходе с фрагмента
        (activity as? AppCompatActivity)?.supportActionBar?.show()
    }

    /**
     * Отправка сообщения пользователя
     */
    private fun sendMessage(text: String) {
        // Добавляем сообщение пользователя
        val userMessage = Message(
            text = text,
            sender = Message.SenderType.USER
        )
        messages.add(userMessage)
        adapter.submitList(messages.toList())
        recyclerView.scrollToPosition(messages.size - 1)

        // Очищаем поле ввода
        editTextMessage.setText("")

        // Отправляем запрос в Gemini и получаем ответ
        generateResponse(text)
    }

    /**
     * Получение ответа от Gemini через ChatService
     */
    private fun generateResponse(userMessage: String) {
        // Показываем индикатор загрузки
        progressBar.visibility = View.VISIBLE
        buttonSend.isEnabled = false

        lifecycleScope.launch {
            try {
                // Получаем ответ от Gemini через ChatService
                val response = chatService.getResponse(userMessage)
                
                response.fold(
                    onSuccess = { text ->
                        // Добавляем ответ бота в историю
                        addBotMessage(text)
                    },
                    onFailure = { error ->
                        // Логируем ошибку детально
                        android.util.Log.e("AssistantFragment", "Error generating response: ${error.localizedMessage}", error)
                        // Обрабатываем ошибку
                        addBotMessage(getString(R.string.assistant_error), true)
                        Snackbar.make(
                            requireView(),
                            "Ошибка: ${error.message ?: "Неизвестная ошибка"}",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                )
            } catch (e: Exception) {
                // Логируем ошибку детально
                android.util.Log.e("AssistantFragment", "Error generating response: ${e.localizedMessage}", e)
                // Обрабатываем ошибку
                addBotMessage(getString(R.string.assistant_error), true)
                Snackbar.make(
                    requireView(),
                    "Ошибка: ${e.message ?: "Неизвестная ошибка"}",
                    Snackbar.LENGTH_LONG
                ).show()
            } finally {
                // Скрываем индикатор загрузки
                progressBar.visibility = View.GONE
                buttonSend.isEnabled = true
            }
        }
    }

    /**
     * Добавление сообщения бота в историю
     */
    private fun addBotMessage(text: String, isError: Boolean = false) {
        val botMessage = Message(
            text = text,
            sender = Message.SenderType.BOT,
            isError = isError
        )
        messages.add(botMessage)
        adapter.submitList(messages.toList())
        recyclerView.scrollToPosition(messages.size - 1)
    }
} 