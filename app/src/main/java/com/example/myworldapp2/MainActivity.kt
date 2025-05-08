package com.example.myworldapp2

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.example.myworldapp2.data.entity.User
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.myworldapp2.data.database.AppDatabase

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: Toolbar
    private lateinit var navView: NavigationView
    private lateinit var bottomNavView: BottomNavigationView
    private lateinit var fab: FloatingActionButton
    
    // Текущий пользователь (null, если не авторизован)
    private var currentUser: User? = null

    // Экземпляр приложения
    private lateinit var app: KidsEncyclopediaApp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Получение экземпляра приложения
        app = application as KidsEncyclopediaApp
        
        // Запускаем проверку данных при старте
        checkDatabaseAndInitialize()
        
        // Инициализируем базовые UI компоненты
        initializeViews()
        
        // Настройка Navigation Component
        setupNavigation()
        
        // Завершаем настройку UI, которая зависит от navController
        setupUIActions()
        
        // Наблюдение за статусом авторизации пользователя
        observeAuthStatus()
        
        // Проверяем, нужно ли направить пользователя на экран входа или домашний экран
        checkAuthStatusAndNavigate()
    }
    
    /**
     * Инициализация базовых UI компонентов
     */
    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        bottomNavView = findViewById(R.id.bottom_nav_view)
        fab = findViewById(R.id.fab)
    }
    
    /**
     * Настройка обработчиков событий UI
     */
    private fun setupUIActions() {
        // Настройка FAB для поиска
        fab.setOnClickListener {
            navController.navigate(R.id.searchFragment)
        }
    }
    
    /**
     * Настройка Navigation Component и бокового меню
     */
    private fun setupNavigation() {
        // Получаем NavController через FragmentContainerView
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        
        // Настройка верхнего уровня навигации (не показывает кнопку "Назад")
        val topLevelDestinations = setOf(
            R.id.homeFragment,
            R.id.categoriesFragment,
            R.id.bookmarksFragment,
            R.id.quizListFragment,
            R.id.assistantFragment
        )
        
        appBarConfiguration = AppBarConfiguration(topLevelDestinations, drawerLayout)
        
        // Настройка ActionBar
        setupActionBarWithNavController(navController, appBarConfiguration)
        
        // Настройка Navigation Drawer
        navView.setupWithNavController(navController)
        navView.setNavigationItemSelectedListener(this)
        
        // Настройка Bottom Navigation
        bottomNavView.setupWithNavController(navController)
        
        val mainActivityToolbar = findViewById<Toolbar>(R.id.toolbar)
        
        // Обработка изменения пункта назначения навигации
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.loginFragment || destination.id == R.id.registerFragment) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                bottomNavView.visibility = View.GONE
                fab.visibility = View.GONE
                mainActivityToolbar.visibility = View.GONE
            } else if (destination.id == R.id.assistantFragment) {
                mainActivityToolbar.visibility = View.GONE
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                bottomNavView.visibility = View.VISIBLE
                fab.visibility = View.GONE
            } else {
                mainActivityToolbar.visibility = View.VISIBLE
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                bottomNavView.visibility = View.VISIBLE
                supportActionBar?.show()
                
                when (destination.id) {
                    R.id.homeFragment,
                    R.id.categoriesFragment,
                    R.id.bookmarksFragment -> {
                        fab.visibility = View.VISIBLE
                    }
                    R.id.profileFragment -> { 
                        fab.visibility = View.GONE
                    }
                    else -> {
                        fab.visibility = View.GONE
                    }
                }
            }
        }
    }
    
    /**
     * Наблюдение за статусом авторизации пользователя
     */
    private fun observeAuthStatus() {
        app.userRepository.currentUser.observe(this) { user ->
            // Если пользователь не авторизован и мы не на экране входа/регистрации
            if (user == null) {
                val currentDestination = navController.currentDestination?.id
                if (currentDestination != R.id.loginFragment && currentDestination != R.id.registerFragment) {
                    navController.navigate(R.id.loginFragment)
                }
            }
            
            // Обновляем информацию о пользователе в боковом меню
            updateNavigationHeader(user)
            updateAdminMenu(user)
        }
    }
    
    /**
     * Обновление заголовка бокового меню с информацией о пользователе
     */
    private fun updateNavigationHeader(user: User?) {
        val headerView = navView.getHeaderView(0)
        val userName = headerView.findViewById<TextView>(R.id.tv_user_name)
        val userEmail = headerView.findViewById<TextView>(R.id.tv_user_email)
        
        if (user != null) {
            userName.text = user.name
            userEmail.text = user.email
        } else {
            userName.text = getString(R.string.app_name)
            userEmail.text = getString(R.string.login)
        }
    }
    
    /**
     * Показать/скрыть меню администратора в зависимости от роли пользователя
     */
    private fun updateAdminMenu(user: User?) {
        val adminMenuItem = navView.menu.findItem(R.id.adminMenuItem)
        adminMenuItem?.isVisible = user?.role == "admin"
    }
    
    /**
     * Выход из учетной записи
     */
    private fun logout() {
        Log.d("MainActivity", "Выход из системы")
        
        // Выполняем выход через userRepository, который очистит SessionManager
        app.userRepository.logout()
        
        // Обновляем UI
        currentUser = null
        updateNavigationHeader(null)
        
        // Переходим на экран входа
        navController.navigate(R.id.loginFragment)
        
        Snackbar.make(
            findViewById(android.R.id.content),
            getString(R.string.logout),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                navController.navigate(R.id.searchFragment)
                true
            }
            R.id.action_settings -> {
                navController.navigate(R.id.settingsFragment)
                true
            }
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
    
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // Стандартная навигация через Navigation Component
            R.id.homeFragment,
            R.id.categoriesFragment,
            R.id.bookmarksFragment,
            R.id.quizzesFragment,
            R.id.achievementsFragment,
            R.id.profileFragment,
            R.id.settingsFragment,
            R.id.adminPanelFragment -> {
                drawerLayout.closeDrawer(GravityCompat.START)
                navController.navigate(item.itemId)
                true
            }
            else -> false
        }
    }

    /**
     * Проверяет статус аутентификации и перенаправляет пользователя на нужный экран
     */
    private fun checkAuthStatusAndNavigate() {
        // Получаем текущий экран
        val currentDestination = navController.currentDestination?.id
        
        // Проверяем, авторизован ли пользователь через SessionManager
        val isLoggedIn = app.sessionManager.isLoggedIn()
        
        Log.d("MainActivity", "Проверка статуса аутентификации - Текущий экран: $currentDestination, isLoggedIn: $isLoggedIn")
        
        if (isLoggedIn) {
            // Если пользователь авторизован и находится на логин или регистрации,
            // перенаправляем на домашний экран
            if (currentDestination == R.id.loginFragment || currentDestination == R.id.registerFragment) {
                Log.d("MainActivity", "Перенаправление на домашний экран (пользователь уже авторизован)")
                navController.navigate(R.id.action_loginFragment_to_homeFragment)
            }
        } else {
            Log.d("MainActivity", "Пользователь не авторизован, остаемся на экране входа")
        }
    }

    /**
     * Проверка данных в базе данных и их инициализация при необходимости
     */
    private fun checkDatabaseAndInitialize() {
        Log.d("MainActivity", "Проверка данных в базе")
        
        // Запускаем корутину для проверки и инициализации данных
        // Но не выполняем повторную инициализацию, если данные уже есть
        app.applicationScope.launch(Dispatchers.IO) {
            try {
                // Проверяем пользователей
                val userCount = app.userRepository.getUserCount()
                Log.d("MainActivity", "Количество пользователей в базе: $userCount")
                
                // Проверяем категории
                val categoriesCount = app.categoryRepository.getCategoryCount()
                Log.d("MainActivity", "Количество категорий в базе: $categoriesCount")
                
                // Выполняем инициализацию только если база абсолютно пуста
                if (userCount == 0 && categoriesCount == 0) {
                    Log.d("MainActivity", "База данных пуста. Запускаем инициализацию данных...")
                    AppDatabase.populateDatabase(app.database)
                    Log.d("MainActivity", "Инициализация данных завершена")
                    
                    // Обновляем UI в главном потоке
                    withContext(Dispatchers.Main) {
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "База данных успешно инициализирована",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Log.d("MainActivity", "База данных уже содержит необходимые данные. Пропускаем инициализацию.")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Ошибка при проверке базы данных", e)
            }
        }
    }
}