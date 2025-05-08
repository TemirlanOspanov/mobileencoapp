package com.example.myworldapp2.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.myworldapp2.KidsEncyclopediaApp
import com.example.myworldapp2.R
import com.example.myworldapp2.databinding.FragmentEntryDetailBinding
import com.example.myworldapp2.ui.adapter.TagAdapter
import com.example.myworldapp2.ui.adapter.CommentAdapter
import com.example.myworldapp2.ui.viewmodel.EntryDetailViewModel
import com.example.myworldapp2.ui.viewmodel.EntryDetailViewModelFactory
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import androidx.appcompat.content.res.AppCompatResources
import kotlinx.coroutines.launch

class EntryDetailFragment : Fragment() {

    private var _binding: FragmentEntryDetailBinding? = null
    private val binding get() = _binding!!

    private val args: EntryDetailFragmentArgs by navArgs()
    private lateinit var viewModel: EntryDetailViewModel
    private lateinit var tagAdapter: TagAdapter
    private lateinit var commentAdapter: CommentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEntryDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get application instance
        val app = requireActivity().application as KidsEncyclopediaApp
        
        // Create ViewModel factory with dependencies from app
        val viewModelFactory = EntryDetailViewModelFactory(
            app.entryRepository,
            app.categoryRepository,
            app.userRepository,
            app.bookmarkRepository,
            app.tagRepository,
            app.likeRepository,
            app.commentRepository
        ).apply {
            setEntryId(args.entryId)
        }
        
        // Initialize ViewModel
        viewModel = ViewModelProvider(this, viewModelFactory)[EntryDetailViewModel::class.java]

        setupToolbar()
        setupTagsRecyclerView()
        setupActionButtons()
        setupCommentSection()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupActionButtons() {
        // Bookmark button внизу
        binding.btnBookmark.setOnClickListener {
            // Временно блокируем кнопку чтобы избежать двойных нажатий
            binding.btnBookmark.isEnabled = false
            binding.btnBookmarkContent.isEnabled = false
            
            // Вызываем toggle с обработкой завершения
            viewLifecycleOwner.lifecycleScope.launch {
            viewModel.toggleBookmark()
                
                // Небольшая задержка перед повторным включением кнопок
                kotlinx.coroutines.delay(300)
                
                // Включаем кнопки обратно
                binding.btnBookmark.isEnabled = true
                binding.btnBookmarkContent.isEnabled = true 
            }
        }
        
        // Bookmark button в контенте
        binding.btnBookmarkContent.setOnClickListener {
            // Временно блокируем кнопку чтобы избежать двойных нажатий
            binding.btnBookmark.isEnabled = false
            binding.btnBookmarkContent.isEnabled = false
            
            // Вызываем toggle с обработкой завершения 
            viewLifecycleOwner.lifecycleScope.launch {
            viewModel.toggleBookmark()
                
                // Небольшая задержка перед повторным включением кнопок
                kotlinx.coroutines.delay(300)
                
                // Включаем кнопки обратно
                binding.btnBookmark.isEnabled = true
                binding.btnBookmarkContent.isEnabled = true
            }
        }
        
        // Like button внизу
        binding.btnLike.setOnClickListener {
            viewModel.toggleLike()
        }
        
        // Like button в контенте
        binding.btnLikeContent.setOnClickListener {
            viewModel.toggleLike()
        }

        // Comments button
        binding.btnComments.setOnClickListener {
            navigateToComments()
        }

        // Quiz button
        binding.btnQuiz.setOnClickListener {
            navigateToQuiz()
        }

        // Share button
        binding.btnShare.setOnClickListener {
            shareEntry()
        }
    }

    private fun setupCommentSection() {
        // Настраиваем кнопку отправки комментария
        binding.btnSendComment.setOnClickListener {
            sendComment()
        }
        
        // Настраиваем обработку нажатия Enter в поле комментария
        binding.etComment.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                sendComment()
                true
            } else {
                false
            }
        }
        
        // Настраиваем RecyclerView для комментариев
        // Получаем идентификатор текущего пользователя из ViewModel
        val currentUserId = viewModel.getCurrentUserId()
        
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
        
        // Устанавливаем адаптер в RecyclerView
        binding.rvComments.adapter = commentAdapter
        
        // Кнопка "Показать все комментарии"
        binding.btnViewAllComments.setOnClickListener {
            navigateToComments()
        }
    }

    private fun setupTagsRecyclerView() {
        tagAdapter = TagAdapter()
        binding.rvEntryTags.adapter = tagAdapter
    }

    private fun observeViewModel() {
        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe entry
        viewModel.entry.observe(viewLifecycleOwner) { entry ->
            if (entry != null) {
                binding.tvEntryTitle.text = entry.title
                binding.tvEntryContent.text = entry.content
                binding.collapsingToolbar.title = entry.title
                
                // Load image if available
                if (!entry.imageUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(entry.imageUrl)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .into(binding.imgEntryHeader)
                } else {
                    binding.imgEntryHeader.setImageResource(R.drawable.ic_launcher_foreground)
                }
                
                // Setup video if available
                setupVideo(entry.videoUrl)
            }
        }

        // Observe category
        viewModel.category.observe(viewLifecycleOwner) { category ->
            if (category != null) {
                binding.tvEntryCategory.text = category.name
                
                // Set category tag color
                val drawable = binding.tvEntryCategory.background.mutate()
                drawable.setTint(resources.getColor(getCategoryColorResourceId(category.id), null))
                binding.tvEntryCategory.background = drawable
            }
        }

        // Observe formatted date
        viewModel.formattedDate.observe(viewLifecycleOwner) { date ->
            binding.tvEntryDate.text = date
        }

        // Observe bookmark status
        viewModel.isBookmarked.observe(viewLifecycleOwner) { isBookmarked ->
            updateBookmarkButtonState(isBookmarked)
        }
        
        // Observe like status
        viewModel.isLiked.observe(viewLifecycleOwner) { isLiked ->
            updateLikeButtonState(isLiked)
        }
        
        // Observe like count
        viewModel.likeCount.observe(viewLifecycleOwner) { count ->
            // Создаем строку с количеством лайков
            val likesText = getString(R.string.likes_format, count)
            
            // Обновляем текст кнопки лайка, добавляя к ней информацию о количестве
            val buttonText = getString(if (viewModel.isLiked.value == true) R.string.unlike else R.string.like)
            binding.btnLikeContent.text = "$buttonText ($count)"
        }
        
        // Observe comment count
        viewModel.commentCount.observe(viewLifecycleOwner) { count ->
            binding.tvCommentCount.text = getString(R.string.comments_format, count)
        }

        // Observe tags
        viewModel.tags.observe(viewLifecycleOwner) { tags ->
            if (tags.isNotEmpty()) {
                tagAdapter.submitList(tags)
                binding.rvEntryTags.visibility = View.VISIBLE
            } else {
                binding.rvEntryTags.visibility = View.GONE
            }
        }

        // Наблюдаем за списком комментариев
        viewModel.comments.observe(viewLifecycleOwner) { comments ->
            if (comments.isNullOrEmpty()) {
                binding.rvComments.visibility = View.GONE
                binding.emptyCommentsView.visibility = View.VISIBLE
                binding.btnViewAllComments.visibility = View.GONE
            } else {
                binding.rvComments.visibility = View.VISIBLE
                binding.emptyCommentsView.visibility = View.GONE
                
                // Показываем только первые 3 комментария
                val topComments = if (comments.size > 3) comments.take(3) else comments
                commentAdapter.submitList(topComments)
                
                // Показываем кнопку "Показать все", если комментариев больше 3
                binding.btnViewAllComments.visibility = if (comments.size > 3) View.VISIBLE else View.GONE
            }
        }
    }

    private fun updateBookmarkButtonState(isBookmarked: Boolean) {
        // Обновление нижней кнопки закладки
        if (isBookmarked) {
            binding.btnBookmark.text = getString(R.string.remove_bookmark)
            binding.btnBookmark.setCompoundDrawablesWithIntrinsicBounds(
                0, R.drawable.ic_bookmark, 0, 0
            )
            // Показываем уведомление при добавлении в закладки
            android.widget.Toast.makeText(
                requireContext(),
                R.string.bookmark_added,
                android.widget.Toast.LENGTH_SHORT
            ).show()
        } else {
            binding.btnBookmark.text = getString(R.string.bookmark)
            binding.btnBookmark.setCompoundDrawablesWithIntrinsicBounds(
                0, R.drawable.ic_bookmark_outline, 0, 0
            )
            // Показываем уведомление при удалении из закладок только если это действительно удаление
            // (а не первоначальная загрузка)
            if (viewModel.isBookmarked.value != null) {
            android.widget.Toast.makeText(
                requireContext(),
                R.string.bookmark_removed,
                android.widget.Toast.LENGTH_SHORT
            ).show()
            }
        }
        
        // Обновление кнопки закладки в контенте
        val bookmarkIcon = AppCompatResources.getDrawable(
            requireContext(),
            if (isBookmarked) R.drawable.ic_bookmark else R.drawable.ic_bookmark_outline
        )
        (binding.btnBookmarkContent as? MaterialButton)?.icon = bookmarkIcon
        binding.btnBookmarkContent.text = getString(
            if (isBookmarked) R.string.remove_bookmark else R.string.bookmark
        )
        
        // Если закладка была добавлена, предложим перейти к списку закладок
        if (isBookmarked) {
            // Показываем Snackbar с предложением перейти к закладкам
            val snackbar = com.google.android.material.snackbar.Snackbar.make(
                binding.root,
                "Статья добавлена в закладки",
                com.google.android.material.snackbar.Snackbar.LENGTH_LONG
            )
            snackbar.setAction("Перейти к закладкам") {
                // Прежде чем перейти, делаем паузу, чтобы данные успели сохраниться
                viewLifecycleOwner.lifecycleScope.launch {
                    android.util.Log.d("EntryDetailFragment", "Готовимся к переходу на экран закладок...")
                    
                    // Небольшая задержка для завершения всех транзакций
                    kotlinx.coroutines.delay(500)
                    
                    // Дополнительная проверка через viewModel
                    val isActuallyBookmarked = viewModel.isBookmarked.value == true
                    android.util.Log.d("EntryDetailFragment", "Перед навигацией: isBookmarked=$isActuallyBookmarked")
                    
                    // Навигация к экрану закладок с принудительным аргументом для обновления
                    val navOptions = androidx.navigation.NavOptions.Builder()
                        .setPopUpTo(R.id.bookmarksFragment, true) // Очищаем стек навигации
                        .build()
                    findNavController().navigate(R.id.bookmarksFragment, null, navOptions)
                }
            }
            snackbar.show()
        }
    }

    private fun updateLikeButtonState(isLiked: Boolean) {
        // Получаем текущее количество лайков
        val likeCount = viewModel.likeCount.value ?: 0
        val likeCountText = "($likeCount)"
        
        // Обновление нижней кнопки лайка
        if (isLiked) {
            binding.btnLike.text = getString(R.string.unlike)
            binding.btnLike.setCompoundDrawablesWithIntrinsicBounds(
                0, R.drawable.ic_heart, 0, 0
            )
        } else {
            binding.btnLike.text = getString(R.string.like)
            binding.btnLike.setCompoundDrawablesWithIntrinsicBounds(
                0, R.drawable.ic_heart_outline, 0, 0
            )
        }
        
        // Обновление кнопки лайка в контенте
        val likeIcon = AppCompatResources.getDrawable(
            requireContext(),
            if (isLiked) R.drawable.ic_heart else R.drawable.ic_heart_outline
        )
        (binding.btnLikeContent as? MaterialButton)?.icon = likeIcon
        
        // Устанавливаем текст с учетом состояния и количества лайков
        val buttonText = getString(if (isLiked) R.string.unlike else R.string.like)
        binding.btnLikeContent.text = "$buttonText $likeCountText"
    }

    private fun navigateToComments() {
        val action = EntryDetailFragmentDirections
            .actionEntryDetailFragmentToCommentsFragment(args.entryId)
        findNavController().navigate(action)
    }

    private fun navigateToQuiz() {
        // Проверяем наличие викторины для этой статьи
        // Это заглушка, в реальном приложении здесь будет логика проверки
        val quizId = args.entryId // Временная заглушка
        
        val action = EntryDetailFragmentDirections
            .actionEntryDetailFragmentToQuizFragment(quizId)
        findNavController().navigate(action)
    }

    private fun shareEntry() {
        val entry = viewModel.entry.value ?: return
        
        // Create sharing intent with a message that includes the article title
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, entry.title)
            
            // Use the predefined string resource for sharing text
            val shareText = getString(R.string.share_text, entry.title)
            
            // Add a short excerpt if the content is available
            val contentPreview = if (!entry.content.isNullOrBlank()) {
                "\n\n${entry.content.take(100)}..."
            } else {
                ""
            }
            
            putExtra(Intent.EXTRA_TEXT, shareText + contentPreview)
        }
        
        // Show the chooser dialog for sharing
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_entry)))
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

    private fun getCategoryColorResourceId(categoryId: Long): Int {
        // Return different colors based on category ID
        return when (categoryId % 6) {
            0L -> R.color.category_animals
            1L -> R.color.category_plants
            2L -> R.color.category_space
            3L -> R.color.category_history
            4L -> R.color.category_technology
            else -> R.color.category_science
        }
    }

    /**
     * Sets up the video player if a video URL is available
     */
    private fun setupVideo(videoUrl: String?) {
        if (videoUrl.isNullOrEmpty()) {
            binding.videoContainer.visibility = View.GONE
            return
        }
        
        // Video URL exists, show the container
        binding.videoContainer.visibility = View.VISIBLE
        
        // Configure the WebView
        with(binding.videoWebView) {
            settings.javaScriptEnabled = true
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            webChromeClient = WebChromeClient()
            webViewClient = WebViewClient()
            
            // Check if it's a YouTube URL and convert to embed format if needed
            val embedUrl = getEmbedUrl(videoUrl)
            
            // Load the embedded video
            loadUrl(embedUrl)
        }
    }
    
    /**
     * Converts standard video URLs to embed format
     */
    private fun getEmbedUrl(videoUrl: String): String {
        return when {
            // YouTube URL handling
            videoUrl.contains("youtube.com/watch") -> {
                // Extract video ID from standard YouTube URL
                val videoId = videoUrl.split("v=")[1].split("&")[0]
                "https://www.youtube.com/embed/$videoId"
            }
            videoUrl.contains("youtu.be/") -> {
                // Extract video ID from short YouTube URL
                val videoId = videoUrl.split("youtu.be/")[1]
                "https://www.youtube.com/embed/$videoId"
            }
            // If not recognized as YouTube, just return the original URL
            else -> videoUrl
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 