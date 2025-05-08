package com.example.myworldapp2.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.myworldapp2.KidsEncyclopediaApp
import com.example.myworldapp2.R
import com.example.myworldapp2.data.entity.Comment
import com.example.myworldapp2.databinding.FragmentCommentsBinding
import com.example.myworldapp2.ui.adapter.CommentAdapter
import com.example.myworldapp2.ui.viewmodel.CommentsViewModel
import com.example.myworldapp2.ui.viewmodel.CommentsViewModelFactory
import com.google.android.material.snackbar.Snackbar

class CommentsFragment : Fragment() {

    private var _binding: FragmentCommentsBinding? = null
    private val binding get() = _binding!!

    private val args: CommentsFragmentArgs by navArgs()
    private lateinit var viewModel: CommentsViewModel
    private lateinit var commentAdapter: CommentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Получаем экземпляр приложения
        val app = requireActivity().application as KidsEncyclopediaApp
        
        // Создаем фабрику для ViewModel с необходимыми зависимостями
        val viewModelFactory = CommentsViewModelFactory(
            app.commentRepository,
            app.userRepository
        ).apply {
            setEntryId(args.entryId)
        }
        
        // Инициализируем ViewModel
        viewModel = ViewModelProvider(this, viewModelFactory)[CommentsViewModel::class.java]

        setupToolbar()
        setupCommentInput()
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.toolbar.title = getString(R.string.comments)
    }

    private fun setupCommentInput() {
        // Обработка кнопки отправки комментария
        binding.btnSend.setOnClickListener {
            sendComment()
        }

        // Обработка нажатия Enter в поле комментария
        binding.etComment.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                sendComment()
                true
            } else {
                false
            }
        }
    }

    private fun setupRecyclerView() {
        // Идентификатор текущего пользователя (в реальном приложении будет получен из системы аутентификации)
        val currentUserId = 1L

        // Создаем адаптер для комментариев
        commentAdapter = CommentAdapter(
            currentUserId = currentUserId,
            getUserById = { userId ->
                viewModel.getUserById(userId)
            },
            onDeleteComment = { comment ->
                viewModel.deleteComment(comment)
            }
        )

        // Настраиваем RecyclerView
        binding.rvComments.adapter = commentAdapter
    }

    private fun observeViewModel() {
        // Наблюдаем за списком комментариев
        viewModel.comments.observe(viewLifecycleOwner) { comments ->
            val nonNullComments = comments ?: emptyList()
            if (nonNullComments.isEmpty()) {
                binding.emptyView.visibility = View.VISIBLE
                binding.rvComments.visibility = View.GONE
            } else {
                binding.emptyView.visibility = View.GONE
                binding.rvComments.visibility = View.VISIBLE
                commentAdapter.submitList(nonNullComments)
            }
        }

        // Наблюдаем за состоянием загрузки
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (_binding != null) {
                // binding.progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        // Наблюдаем за сообщениями об ошибках
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty()) {
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun sendComment() {
        val commentText = binding.etComment.text.toString().trim()
        if (commentText.isNotEmpty()) {
            viewModel.addComment(commentText)
            binding.etComment.text?.clear()
            
            // Показываем уведомление об успешной отправке комментария
            Snackbar.make(binding.root, R.string.comment_sent_success, Snackbar.LENGTH_SHORT).show()
            
            // Скрываем клавиатуру
            val inputMethodManager = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(binding.etComment.windowToken, 0)
        } else {
            Snackbar.make(binding.root, R.string.empty_comment_error, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 