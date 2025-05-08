package com.example.myworldapp2.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myworldapp2.data.dao.*
import com.example.myworldapp2.data.entity.*
import com.example.myworldapp2.util.DateConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.room.migration.Migration
import android.util.Log

/**
 * Основной класс базы данных приложения
 */
@Database(
    entities = [
        User::class,
        Category::class,
        Entry::class,
        UserProgress::class,
        Comment::class,
        Bookmark::class,
        Achievement::class,
        UserAchievement::class,
        Tag::class,
        EntryTag::class,
        Quiz::class,
        QuizQuestion::class,
        QuizAnswer::class,
        UserQuizResult::class,
        Like::class
    ],
    version = 6,
    exportSchema = true
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    // DAO
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun entryDao(): EntryDao
    abstract fun userProgressDao(): UserProgressDao
    abstract fun commentDao(): CommentDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun achievementDao(): AchievementDao
    abstract fun userAchievementDao(): UserAchievementDao
    abstract fun tagDao(): TagDao
    abstract fun entryTagDao(): EntryTagDao
    abstract fun quizDao(): QuizDao
    abstract fun quizQuestionDao(): QuizQuestionDao
    abstract fun quizAnswerDao(): QuizAnswerDao
    abstract fun userQuizResultDao(): UserQuizResultDao
    abstract fun likeDao(): LikeDao

    companion object {
        // Migration from version 1 to 2 - Add videoUrl column to entries table
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // SQLite syntax to add a column to an existing table
                database.execSQL("ALTER TABLE entries ADD COLUMN videoUrl TEXT")
            }
        }

        // Migration from version 2 to 3
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Пустая миграция, чтобы предотвратить удаление данных
                // Если были добавлены новые таблицы или столбцы, их нужно добавить здесь
            }
        }

        // Migration from version 3 to 4 - Add color column to tags table
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Добавляем столбец color в таблицу tags с значением по умолчанию
                database.execSQL("ALTER TABLE tags ADD COLUMN color TEXT DEFAULT '#4CAF50' NOT NULL")
                
                Log.d("AppDatabase", "Миграция с версии 3 на 4 успешно выполнена")
            }
        }
        
        // Migration from version 4 to 5 - Add likes table
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Создаем таблицу лайков
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `likes` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `entryId` INTEGER NOT NULL,
                        `userId` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        FOREIGN KEY(`entryId`) REFERENCES `entries`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """
                )
                
                // Создаем индексы для таблицы лайков
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_likes_entryId` ON `likes` (`entryId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_likes_userId` ON `likes` (`userId`)")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_likes_entryId_userId` ON `likes` (`entryId`, `userId`)")
                
                Log.d("AppDatabase", "Миграция с версии 4 на 5 успешно выполнена")
            }
        }

        // Migration from version 5 to 6 - Update Achievement and UserAchievement tables
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    // Проверяем структуру таблицы achievements перед миграцией
                    val cursor = database.query("PRAGMA table_info(achievements)")
                    val columnNames = mutableListOf<String>()
                    while (cursor.moveToNext()) {
                        val columnName = cursor.getString(cursor.getColumnIndex("name"))
                        columnNames.add(columnName)
                    }
                    cursor.close()
                    
                    Log.d("AppDatabase", "Существующие колонки в таблице achievements: $columnNames")
                    
                    // Для Achievement: переименование name -> title, icon -> iconName, добавление points и targetProgress
                    database.execSQL("CREATE TABLE IF NOT EXISTS `achievements_new` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`title` TEXT NOT NULL, " +
                        "`description` TEXT NOT NULL, " +
                        "`icon_name` TEXT NOT NULL, " +
                        "`points` INTEGER NOT NULL, " +
                        "`type` TEXT NOT NULL, " +
                        "`current_progress` INTEGER NOT NULL, " +
                        "`target_progress` INTEGER NOT NULL, " +
                        "`created_at` INTEGER NOT NULL, " +
                        "`updated_at` INTEGER NOT NULL)")
                    
                    // Копируем данные из старой таблицы в новую с нужными преобразованиями
                    // Используем текущее время для created_at и updated_at
                    val currentTime = System.currentTimeMillis()
                    database.execSQL("INSERT INTO `achievements_new` (`id`, `title`, `description`, " +
                        "`icon_name`, `points`, `type`, `current_progress`, `target_progress`, " +
                        "`created_at`, `updated_at`) " +
                        "SELECT `id`, `name`, `description`, `icon`, 50, `type`, 0, " +
                        "CASE WHEN `requiredCount` IS NULL THEN 1 ELSE `requiredCount` END, " +
                        "$currentTime, $currentTime FROM `achievements`")
                    
                    // Удаляем старую таблицу и переименовываем новую
                    database.execSQL("DROP TABLE `achievements`")
                    database.execSQL("ALTER TABLE `achievements_new` RENAME TO `achievements`")
                    
                    // Проверяем структуру таблицы user_achievements перед миграцией
                    val userAchievementCursor = database.query("PRAGMA table_info(user_achievements)")
                    val userAchievementColumnNames = mutableListOf<String>()
                    while (userAchievementCursor.moveToNext()) {
                        val columnName = userAchievementCursor.getString(userAchievementCursor.getColumnIndex("name"))
                        userAchievementColumnNames.add(columnName)
                    }
                    userAchievementCursor.close()
                    
                    Log.d("AppDatabase", "Существующие колонки в таблице user_achievements: $userAchievementColumnNames")
                    
                    // Для UserAchievement: переименование earnedAt -> completedAt (если такие колонки есть)
                    database.execSQL("CREATE TABLE IF NOT EXISTS `user_achievements_new` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`userId` INTEGER NOT NULL, " +
                        "`achievementId` INTEGER NOT NULL, " +
                        "`progress` INTEGER NOT NULL, " +
                        "`completed_at` INTEGER, " +
                        "`notification_seen` INTEGER NOT NULL, " +
                        "`created_at` INTEGER NOT NULL, " +
                        "`updated_at` INTEGER NOT NULL, " +
                        "FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, " +
                        "FOREIGN KEY(`achievementId`) REFERENCES `achievements`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)")
                    
                    // Проверяем, есть ли колонка earnedAt в старой таблице
                    if (userAchievementColumnNames.contains("earnedAt")) {
                        database.execSQL("INSERT INTO `user_achievements_new` (`id`, `userId`, `achievementId`, " +
                            "`progress`, `completed_at`, `notification_seen`, `created_at`, `updated_at`) " +
                            "SELECT `id`, `userId`, `achievementId`, `progress`, `earnedAt`, " +
                            "`notification_seen`, $currentTime, $currentTime FROM `user_achievements`")
                    } else {
                        // Если нет колонки earnedAt, просто используем NULL для completed_at и ставим progress = 0
                        database.execSQL("INSERT INTO `user_achievements_new` (`id`, `userId`, `achievementId`, " +
                            "`progress`, `completed_at`, `notification_seen`, `created_at`, `updated_at`) " +
                            "SELECT `id`, `userId`, `achievementId`, 0, `earnedAt`, " +
                            "0, $currentTime, $currentTime FROM `user_achievements`")
                    }
                    
                    // Удаляем старую таблицу и переименовываем новую
                    database.execSQL("DROP TABLE `user_achievements`")
                    database.execSQL("ALTER TABLE `user_achievements_new` RENAME TO `user_achievements`")
                    
                    // Создаем индексы заново
                    database.execSQL("CREATE INDEX IF NOT EXISTS `index_user_achievements_userId` ON `user_achievements` (`userId`)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS `index_user_achievements_achievementId` ON `user_achievements` (`achievementId`)")
                    database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_user_achievements_userId_achievementId` ON `user_achievements` (`userId`, `achievementId`)")
                    
                    Log.d("AppDatabase", "Миграция с версии 5 на 6 успешно выполнена")
                } catch (e: Exception) {
                    Log.e("AppDatabase", "Ошибка миграции с версии 5 на 6: ${e.message}", e)
                    // Если миграция не сработала, сохраняем ошибку и пробуем fallback
                    database.execSQL("PRAGMA foreign_keys = OFF")
                    
                    // Создаем новые таблицы с новой структурой
                    database.execSQL("CREATE TABLE IF NOT EXISTS `achievements_new` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`title` TEXT NOT NULL, " +
                        "`description` TEXT NOT NULL, " +
                        "`icon_name` TEXT NOT NULL, " +
                        "`points` INTEGER NOT NULL, " +
                        "`type` TEXT NOT NULL, " +
                        "`current_progress` INTEGER NOT NULL DEFAULT 0, " +
                        "`target_progress` INTEGER NOT NULL, " +
                        "`created_at` INTEGER NOT NULL, " +
                        "`updated_at` INTEGER NOT NULL)")
                    
                    database.execSQL("CREATE TABLE IF NOT EXISTS `user_achievements_new` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`userId` INTEGER NOT NULL, " +
                        "`achievementId` INTEGER NOT NULL, " +
                        "`progress` INTEGER NOT NULL DEFAULT 0, " +
                        "`completed_at` INTEGER, " +
                        "`notification_seen` INTEGER NOT NULL DEFAULT 0, " +
                        "`created_at` INTEGER NOT NULL, " +
                        "`updated_at` INTEGER NOT NULL, " +
                        "FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, " +
                        "FOREIGN KEY(`achievementId`) REFERENCES `achievements`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)")
                    
                    // Удалить старые таблицы, если они существуют
                    database.execSQL("DROP TABLE IF EXISTS `achievements`")
                    database.execSQL("DROP TABLE IF EXISTS `user_achievements`")
                    
                    // Переименовать новые таблицы
                    database.execSQL("ALTER TABLE `achievements_new` RENAME TO `achievements`")
                    database.execSQL("ALTER TABLE `user_achievements_new` RENAME TO `user_achievements`")
                    
                    // Создаем индексы
                    database.execSQL("CREATE INDEX IF NOT EXISTS `index_user_achievements_userId` ON `user_achievements` (`userId`)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS `index_user_achievements_achievementId` ON `user_achievements` (`achievementId`)")
                    database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_user_achievements_userId_achievementId` ON `user_achievements` (`userId`, `achievementId`)")
                    
                    database.execSQL("PRAGMA foreign_keys = ON")
                    
                    Log.d("AppDatabase", "Выполнен fallback миграции с версии 5 на 6")
                }
            }
        }

        // Синглтон для доступа к базе данных
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kids_encyclopedia_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6) // Add all migrations
                    .fallbackToDestructiveMigration() // Add this line to handle schema changes during development
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Колбэк для заполнения базы начальными данными при создании
        private class AppDatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        // Здесь можно предзаполнить базу начальными данными
                        // Например:
                        populateDatabase(database)
                    }
                }
            }
        }

        // Метод для заполнения базы тестовыми данными
        suspend fun populateDatabase(database: AppDatabase) {
            try {
                Log.d("AppDatabase", "Начинаем заполнение базы данных тестовыми данными")
                
                // Проверяем сколько уже существует объектов в базе
                val userCount = database.userDao().getUserCount()
                val categoryCount = database.categoryDao().getCategoryCount()
                val quizCount = database.quizDao().getQuizCount() 
                
                Log.d("AppDatabase", "Текущее состояние базы: пользователей=$userCount, категорий=$categoryCount, викторин=$quizCount")
                
                // Если база не пуста, то полностью очищаем её перед заполнением
                if (userCount > 0 || categoryCount > 0 || quizCount > 0) {
                    Log.d("AppDatabase", "База данных уже содержит данные, очищаем перед заполнением")
                    
                    // Удаляем данные в обратном порядке зависимостей
                    database.userQuizResultDao().deleteAll()
                    database.quizAnswerDao().deleteAll()
                    database.quizQuestionDao().deleteAll()
                    database.quizDao().deleteAll()
                    database.likeDao().deleteAll()
                    database.bookmarkDao().deleteAll()
                    database.commentDao().deleteAll()
                    database.entryTagDao().deleteAll()
                    database.userAchievementDao().deleteAll()
                    database.userProgressDao().deleteAll()
                    database.achievementDao().deleteAll()
                    database.entryDao().deleteAll()
                    database.tagDao().deleteAll()
                    database.categoryDao().deleteAll()
                    database.userDao().deleteAll()
                    
                    Log.d("AppDatabase", "База данных очищена")
                }
                
                // Добавляем администратора
                val adminId = database.userDao().insert(
                    User(
                        email = "admin@example.com",
                        passwordHash = "5f4dcc3b5aa765d61d8327deb882cf99", // "password" в MD5
                        name = "Администратор",
                        role = "admin"
                    )
                )
                Log.d("AppDatabase", "Добавлен администратор с ID: $adminId")

                // Добавляем тестового пользователя
                val userId = database.userDao().insert(
                    User(
                        email = "user@example.com",
                        passwordHash = "5f4dcc3b5aa765d61d8327deb882cf99", // "password" в MD5
                        name = "Тестовый пользователь",
                        role = "user"
                    )
                )
                Log.d("AppDatabase", "Добавлен тестовый пользователь с ID: $userId")

                // Добавляем тестовые категории
                val categoryIds = database.categoryDao().insertAll(
                    listOf(
                        Category(
                            name = "Животные",
                            description = "Узнай всё о животных нашей планеты",
                            icon = "ic_animals",
                            color = "#4CAF50"
                        ),
                        Category(
                            name = "Космос",
                            description = "Путешествие к звёздам и планетам",
                            icon = "ic_space",
                            color = "#3F51B5"
                        ),
                        Category(
                            name = "Наука",
                            description = "Интересные научные факты и открытия",
                            icon = "ic_science",
                            color = "#FF9800"
                        ),
                        Category(
                            name = "Растения",
                            description = "Удивительный мир растений",
                            icon = "ic_plants",
                            color = "#009688"
                        ),
                        Category(
                            name = "История",
                            description = "Исторические события и личности",
                            icon = "ic_history",
                            color = "#795548"
                        ),
                        Category(
                            name = "География",
                            description = "Страны, города и природные явления",
                            icon = "ic_geography",
                            color = "#2196F3"
                        )
                    )
                )
                
                Log.d("AppDatabase", "Добавлено ${categoryIds.size} категорий")

                // Добавляем теги
                val tagIds = database.tagDao().insertAll(
                    listOf(
                        Tag(name = "Млекопитающие", color = "#4CAF50"),
                        Tag(name = "Птицы", color = "#2196F3"),
                        Tag(name = "Насекомые", color = "#F44336"),
                        Tag(name = "Планеты", color = "#9C27B0"),
                        Tag(name = "Звезды", color = "#FF9800"),
                        Tag(name = "Эксперименты", color = "#4CAF50"),
                        Tag(name = "Деревья", color = "#4CAF50"),
                        Tag(name = "Цветы", color = "#E91E63"),
                        Tag(name = "Древний мир", color = "#795548"),
                        Tag(name = "Средние века", color = "#795548"),
                        Tag(name = "Континенты", color = "#2196F3"),
                        Tag(name = "Океаны", color = "#2196F3"),
                        Tag(name = "Наука", color = "#FF9800")
                    )
                )
                
                // Добавляем статьи для каждой категории (по 7 штук)
                val entryIds = mutableListOf<Long>()
                
                // Статьи о животных (Категория 1)
                val animalsEntries = listOf(
                    Entry(
                        title = "Слон",
                        content = "Слоны — самые крупные наземные животные на Земле. Они отличаются наличием хобота, бивней, большими ушами и сильными ногами. Слоны очень умные и социальные животные, живут большими семейными группами.",
                        categoryId = categoryIds[0],
                        imageUrl = "https://wallpapercrafter.com/desktop1/693322-elephant-africa-african-bush-elephant-proboscis.jpg"
                    ),
                    Entry(
                        title = "Лев",
                        content = "Лев — крупное хищное животное семейства кошачьих, один из четырёх представителей рода пантер. Львы живут в Африке, где они являются вершиной пищевой цепи. Самцы имеют характерную гриву вокруг головы.",
                        categoryId = categoryIds[0],
                        imageUrl = "https://example.com/images/lion.jpg"
                    ),
                    Entry(
                        title = "Жираф",
                        content = "Жираф — самое высокое наземное животное. Его длинная шея и ноги позволяют ему достигать высоты до 5-6 метров. Жирафы питаются листьями деревьев, до которых другие животные не могут дотянуться.",
                        categoryId = categoryIds[0],
                        imageUrl = "https://example.com/images/giraffe.jpg"
                    ),
                    Entry(
                        title = "Тигр",
                        content = "Тигр — самая крупная кошка на планете. Эти полосатые хищники обладают невероятной силой и ловкостью. Каждый тигр имеет уникальный узор полос, как отпечатки пальцев у людей.",
                        categoryId = categoryIds[0],
                        imageUrl = "https://example.com/images/tiger.jpg"
                    ),
                    Entry(
                        title = "Пингвин",
                        content = "Пингвины — нелетающие птицы, прекрасно приспособленные к жизни в холодных водах. Их тело покрыто плотным слоем перьев, а под кожей есть жировой слой, который защищает от холода.",
                        categoryId = categoryIds[0],
                        imageUrl = "https://example.com/images/penguin.jpg"
                    ),
                    Entry(
                        title = "Дельфин",
                        content = "Дельфины — очень умные морские млекопитающие. Они общаются друг с другом с помощью сложной системы звуков и могут узнавать себя в зеркале, что свидетельствует о высоком интеллекте.",
                        categoryId = categoryIds[0],
                        imageUrl = "https://example.com/images/dolphin.jpg"
                    ),
                    Entry(
                        title = "Коала",
                        content = "Коала — сумчатое животное, обитающее в Австралии. Они проводят большую часть жизни на эвкалиптовых деревьях, питаясь их листьями. Детеныши коал рождаются крошечными и развиваются в сумке матери.",
                        categoryId = categoryIds[0],
                        imageUrl = "https://example.com/images/koala.jpg"
                    )
                )
                
                // Статьи о космосе (Категория 2)
                val spaceEntries = listOf(
                    Entry(
                        title = "Солнечная система",
                        content = "Солнечная система — планетная система, включающая в себя центральную звезду Солнце и все естественные космические объекты, вращающиеся вокруг него. В Солнечной системе есть восемь планет, множество карликовых планет, астероидов и комет.",
                        categoryId = categoryIds[1],
                        imageUrl = "https://example.com/images/solar_system.jpg",
                        videoUrl = "https://example.com/videos/solar_system_tour.mp4"
                    ),
                    Entry(
                        title = "Марс",
                        content = "Марс — четвёртая по удалённости от Солнца планета Солнечной системы. Названа в честь древнеримского бога войны Марса. Иногда Марс называют «красной планетой» из-за красноватого оттенка поверхности, придаваемого ей оксидом железа.",
                        categoryId = categoryIds[1],
                        imageUrl = "https://example.com/images/mars.jpg"
                    ),
                    Entry(
                        title = "Чёрные дыры",
                        content = "Чёрная дыра — область пространства-времени, гравитационное притяжение которой настолько велико, что покинуть её не могут даже объекты, движущиеся со скоростью света. Граница этой области называется горизонтом событий.",
                        categoryId = categoryIds[1],
                        imageUrl = "https://example.com/images/black_hole.jpg"
                    ),
                    Entry(
                        title = "Международная космическая станция",
                        content = "МКС — пилотируемая орбитальная станция, используемая как многоцелевой космический исследовательский комплекс. Это крупнейший искусственный спутник Земли, который можно увидеть невооруженным глазом.",
                        categoryId = categoryIds[1],
                        imageUrl = "https://example.com/images/iss.jpg"
                    ),
                    Entry(
                        title = "Млечный Путь",
                        content = "Млечный Путь — галактика, в которой находится наша Солнечная система. Это спиральная галактика с перемычкой, содержащая от 200 до 400 миллиардов звёзд и имеющая диаметр около 100 000 световых лет.",
                        categoryId = categoryIds[1],
                        imageUrl = "https://example.com/images/milky_way.jpg"
                    ),
                    Entry(
                        title = "Комета Галлея",
                        content = "Комета Галлея — яркая комета, которая возвращается к Солнцу каждые 75-76 лет. Это одна из самых известных комет, она была замечена многократно на протяжении истории, начиная с древних времен.",
                        categoryId = categoryIds[1],
                        imageUrl = "https://example.com/images/halley_comet.jpg"
                    ),
                    Entry(
                        title = "Сатурн и его кольца",
                        content = "Сатурн — шестая планета от Солнца и вторая по размерам в Солнечной системе. Сатурн известен своими впечатляющими кольцами, состоящими из частиц льда и камня. У Сатурна более 80 известных спутников.",
                        categoryId = categoryIds[1],
                        imageUrl = "https://example.com/images/saturn.jpg"
                    )
                )
                
                // Статьи о науке (Категория 3)
                val scienceEntries = listOf(
                    Entry(
                        title = "Как работает магнит",
                        content = "Магниты – это объекты, которые создают магнитное поле и могут притягивать железо, никель и кобальт. Магниты имеют два полюса: северный и южный. Противоположные полюса притягиваются, а одинаковые отталкиваются.",
                        categoryId = categoryIds[2],
                        imageUrl = "https://example.com/images/magnet.jpg"
                    ),
                    Entry(
                        title = "ДНК — молекула наследственности",
                        content = "ДНК (дезоксирибонуклеиновая кислота) — это молекула, которая содержит генетические инструкции, необходимые для развития и функционирования всех живых организмов. Она имеет форму двойной спирали.",
                        categoryId = categoryIds[2],
                        imageUrl = "https://example.com/images/dna.jpg"
                    ),
                    Entry(
                        title = "Что такое гравитация",
                        content = "Гравитация — это фундаментальное взаимодействие, которое заставляет объекты с массой притягиваться друг к другу. Именно гравитация удерживает нас на земле и планеты на их орбитах вокруг Солнца.",
                        categoryId = categoryIds[2],
                        imageUrl = "https://example.com/images/gravity.jpg"
                    ),
                    Entry(
                        title = "Атомы и молекулы",
                        content = "Атомы — это базовые строительные блоки всего вещества. Они состоят из ядра (протоны и нейтроны) и электронов, которые вращаются вокруг ядра. Молекулы образуются, когда два или более атома соединяются вместе.",
                        categoryId = categoryIds[2],
                        imageUrl = "https://example.com/images/atoms.jpg"
                    ),
                    Entry(
                        title = "Радуга: как она образуется",
                        content = "Радуга — это оптическое и метеорологическое явление, которое проявляется как разноцветная дуга на небе. Она образуется, когда солнечный свет преломляется и отражается в капельках воды в атмосфере.",
                        categoryId = categoryIds[2],
                        imageUrl = "https://example.com/images/rainbow.jpg"
                    ),
                    Entry(
                        title = "Электричество: что это такое",
                        content = "Электричество — это форма энергии, связанная с движением электронов. Оно является основой для работы большинства современных технологий, от простых фонариков до сложных компьютеров.",
                        categoryId = categoryIds[2],
                        imageUrl = "https://example.com/images/electricity.jpg"
                    ),
                    Entry(
                        title = "Человеческий мозг",
                        content = "Мозг — это самый сложный орган человеческого тела. Он контролирует все функции тела, интерпретирует информацию от органов чувств и является центром мышления, эмоций и памяти.",
                        categoryId = categoryIds[2],
                        imageUrl = "https://example.com/images/brain.jpg"
                    )
                )
                
                // Статьи о растениях (Категория 4)
                val plantsEntries = listOf(
                    Entry(
                        title = "Фотосинтез: как растения производят пищу",
                        content = "Фотосинтез — это процесс, при котором растения используют солнечный свет, воду и углекислый газ для производства кислорода и глюкозы. Это один из самых важных процессов на Земле, обеспечивающий кислород для дыхания.",
                        categoryId = categoryIds[3],
                        imageUrl = "https://example.com/images/photosynthesis.jpg"
                    ),
                    Entry(
                        title = "Баобаб — дерево жизни",
                        content = "Баобаб — это массивное дерево, растущее в Африке. Его ствол может достигать 9-10 метров в диаметре. Баобабы могут хранить огромное количество воды в своих стволах, что позволяет им выживать в сухие сезоны.",
                        categoryId = categoryIds[3],
                        imageUrl = "https://example.com/images/baobab.jpg"
                    ),
                    Entry(
                        title = "Кактусы: выживание в пустыне",
                        content = "Кактусы — это растения, хорошо приспособленные к жизни в пустыне. Они имеют толстые стебли для хранения воды, а их колючки — это на самом деле модифицированные листья, которые минимизируют потерю воды и защищают от животных.",
                        categoryId = categoryIds[3],
                        imageUrl = "https://example.com/images/cactus.jpg"
                    ),
                    Entry(
                        title = "Секвойя: самое высокое дерево",
                        content = "Секвойя, или красное дерево, — это вечнозеленое дерево, которое может достигать высоты более 100 метров. Это одно из самых высоких деревьев в мире, и некоторые экземпляры живут более 2000 лет.",
                        categoryId = categoryIds[3],
                        imageUrl = "https://example.com/images/sequoia.jpg"
                    ),
                    Entry(
                        title = "Хищные растения",
                        content = "Хищные растения, такие как венерина мухоловка и непентес, развили способность ловить и переваривать насекомых для получения дополнительных питательных веществ, особенно азота, который отсутствует в бедных почвах их естественной среды обитания.",
                        categoryId = categoryIds[3],
                        imageUrl = "https://example.com/images/carnivorous_plants.jpg"
                    ),
                    Entry(
                        title = "Бамбук: самое быстрорастущее растение",
                        content = "Бамбук — это одно из самых быстрорастущих растений в мире. Некоторые виды могут вырасти до 91 см за один день. Несмотря на древовидный вид, бамбук на самом деле является травой.",
                        categoryId = categoryIds[3],
                        imageUrl = "https://example.com/images/bamboo.jpg"
                    ),
                    Entry(
                        title = "Амазонская водяная лилия",
                        content = "Амазонская водяная лилия (Victoria amazonica) — это крупнейшая водяная лилия в мире. Ее листья могут достигать 3 метров в диаметре и способны выдержать вес до 45 килограммов, если вес равномерно распределен.",
                        categoryId = categoryIds[3],
                        imageUrl = "https://example.com/images/water_lily.jpg"
                    )
                )
                
                // Статьи об истории (Категория 5)
                val historyEntries = listOf(
                    Entry(
                        title = "Древний Египет: цивилизация фараонов",
                        content = "Древнеегипетская цивилизация процветала вдоль реки Нил более 3000 лет. Египтяне создали одну из первых великих цивилизаций с централизованным правительством, организованной религией и впечатляющими монументами, такими как пирамиды.",
                        categoryId = categoryIds[4],
                        imageUrl = "https://example.com/images/ancient_egypt.jpg"
                    ),
                    Entry(
                        title = "Римская империя",
                        content = "Римская империя была одной из крупнейших империй древнего мира. В период своего расцвета она контролировала территорию вокруг Средиземного моря в Европе, Северной Африке и Западной Азии.",
                        categoryId = categoryIds[4],
                        imageUrl = "https://example.com/images/roman_empire.jpg"
                    ),
                    Entry(
                        title = "Средневековые рыцари",
                        content = "Рыцари были воинами в тяжелых доспехах, которые сражались верхом на лошадях в средневековой Европе. Они следовали кодексу рыцарства, который включал такие ценности, как храбрость, вежливость, честь и защита слабых.",
                        categoryId = categoryIds[4],
                        imageUrl = "https://example.com/images/knights.jpg"
                    ),
                    Entry(
                        title = "Великие географические открытия",
                        content = "Период великих географических открытий начался в начале 15 века и продолжался до 17 века. В это время европейские корабли путешествовали по всему миру, открывая новые торговые пути и устанавливая контакты с другими цивилизациями.",
                        categoryId = categoryIds[4],
                        imageUrl = "https://example.com/images/explorations.jpg"
                    ),
                    Entry(
                        title = "Промышленная революция",
                        content = "Промышленная революция была периодом быстрых социальных и экономических изменений, начавшимся в Великобритании в конце 18 века. Она ознаменовала переход от ручного труда к машинному производству и развитие фабричной системы.",
                        categoryId = categoryIds[4],
                        imageUrl = "https://example.com/images/industrial_revolution.jpg"
                    ),
                    Entry(
                        title = "Первая мировая война",
                        content = "Первая мировая война (1914-1918) была одним из самых разрушительных конфликтов в истории человечества. Она началась из-за сложной сети альянсов и конфликтов между крупными европейскими державами.",
                        categoryId = categoryIds[4],
                        imageUrl = "https://example.com/images/world_war_1.jpg"
                    ),
                    Entry(
                        title = "Покорение космоса",
                        content = "Космическая гонка была соревнованием между США и СССР в освоении космоса, которое началось после запуска первого искусственного спутника Земли, Спутника-1, в 1957 году. Она привела к многочисленным технологическим достижениям и высадке человека на Луну в 1969 году.",
                        categoryId = categoryIds[4],
                        imageUrl = "https://example.com/images/space_race.jpg"
                    )
                )
                
                // Статьи о географии (Категория 6)
                val geographyEntries = listOf(
                    Entry(
                        title = "Амазонка: самая полноводная река",
                        content = "Река Амазонка, протекающая через Бразилию, Перу и другие страны Южной Америки, является самой полноводной рекой в мире. Она несет в океан больше воды, чем следующие семь крупнейших рек вместе взятые.",
                        categoryId = categoryIds[5],
                        imageUrl = "https://example.com/images/amazon_river.jpg"
                    ),
                    Entry(
                        title = "Эверест: крыша мира",
                        content = "Гора Эверест, расположенная в Гималаях на границе между Непалом и Тибетом, является самой высокой точкой на Земле, достигая высоты 8848 метров над уровнем моря.",
                        categoryId = categoryIds[5],
                        imageUrl = "https://example.com/images/everest.jpg"
                    ),
                    Entry(
                        title = "Большой Барьерный риф",
                        content = "Большой Барьерный риф у побережья Австралии является крупнейшей коралловой системой в мире. Это единственное живое образование на Земле, видимое из космоса, и дом для тысяч видов морских организмов.",
                        categoryId = categoryIds[5],
                        imageUrl = "https://example.com/images/great_barrier_reef.jpg"
                    ),
                    Entry(
                        title = "Сахара: крупнейшая жаркая пустыня",
                        content = "Сахара в Северной Африке является крупнейшей жаркой пустыней в мире, занимая площадь, сравнимую с площадью США. Несмотря на суровые условия, в Сахаре обитают различные растения и животные.",
                        categoryId = categoryIds[5],
                        imageUrl = "https://example.com/images/sahara.jpg"
                    ),
                    Entry(
                        title = "Гранд-Каньон",
                        content = "Гранд-Каньон в штате Аризона, США, — это огромный каньон, вырезанный рекой Колорадо. Он достигает до 29 км в ширину и 1.6 км в глубину, и является одним из самых впечатляющих природных чудес мира.",
                        categoryId = categoryIds[5],
                        imageUrl = "https://example.com/images/grand_canyon.jpg"
                    ),
                    Entry(
                        title = "Северное сияние",
                        content = "Северное сияние (Aurora Borealis) — это природное световое шоу, которое можно увидеть в высоких широтах, ближе к полюсам. Оно возникает, когда заряженные частицы от Солнца взаимодействуют с атмосферой Земли.",
                        categoryId = categoryIds[5],
                        imageUrl = "https://example.com/images/northern_lights.jpg"
                    ),
                    Entry(
                        title = "Венеция: город на воде",
                        content = "Венеция — это уникальный город в Италии, построенный на 118 маленьких островах, разделенных каналами и соединенных мостами. Вместо дорог в Венеции используются водные пути, а основным транспортом являются лодки.",
                        categoryId = categoryIds[5],
                        imageUrl = "https://example.com/images/venice.jpg"
                    )
                )
                
                // Добавляем все статьи в базу данных
                for (entry in animalsEntries + spaceEntries + scienceEntries + plantsEntries + historyEntries + geographyEntries) {
                    val entryId = database.entryDao().insert(entry)
                    entryIds.add(entryId)
                }
                
                // Связываем статьи с тегами
                // Животные
                database.entryTagDao().insert(EntryTag(entryId = entryIds[0], tagId = tagIds[0])) // Слон - Млекопитающие
                database.entryTagDao().insert(EntryTag(entryId = entryIds[1], tagId = tagIds[0])) // Лев - Млекопитающие
                database.entryTagDao().insert(EntryTag(entryId = entryIds[2], tagId = tagIds[0])) // Жираф - Млекопитающие
                database.entryTagDao().insert(EntryTag(entryId = entryIds[3], tagId = tagIds[0])) // Тигр - Млекопитающие
                database.entryTagDao().insert(EntryTag(entryId = entryIds[4], tagId = tagIds[1])) // Пингвин - Птицы
                database.entryTagDao().insert(EntryTag(entryId = entryIds[5], tagId = tagIds[0])) // Дельфин - Млекопитающие
                database.entryTagDao().insert(EntryTag(entryId = entryIds[6], tagId = tagIds[0])) // Коала - Млекопитающие
                
                // Космос
                database.entryTagDao().insert(EntryTag(entryId = entryIds[7], tagId = tagIds[3])) // Солнечная система - Планеты
                database.entryTagDao().insert(EntryTag(entryId = entryIds[7], tagId = tagIds[4])) // Солнечная система - Звезды
                database.entryTagDao().insert(EntryTag(entryId = entryIds[8], tagId = tagIds[3])) // Марс - Планеты
                database.entryTagDao().insert(EntryTag(entryId = entryIds[9], tagId = tagIds[12])) // Чёрные дыры - Наука
                database.entryTagDao().insert(EntryTag(entryId = entryIds[11], tagId = tagIds[4])) // Млечный Путь - Звезды
                database.entryTagDao().insert(EntryTag(entryId = entryIds[13], tagId = tagIds[3])) // Сатурн - Планеты
                
                // Добавляем викторины
                val quizIds = mutableListOf<Long>()
                
                quizIds.add(database.quizDao().insert(
                    Quiz(
                        title = "Животные мира",
                        description = "Проверь свои знания о животных",
                        entryId = entryIds[0]
                    )
                ))
                
                quizIds.add(database.quizDao().insert(
                    Quiz(
                        title = "Космические исследования",
                        description = "Что ты знаешь о космосе?",
                        entryId = entryIds[7]
                    )
                ))
                
                // Добавляем вопросы к викторине о животных
                val questionIds = mutableListOf<Long>()
                
                questionIds.add(database.quizQuestionDao().insert(
                    QuizQuestion(
                        quizId = quizIds[0],
                        questionText = "Какое животное самое быстрое на суше?"
                    )
                ))
                
                questionIds.add(database.quizQuestionDao().insert(
                    QuizQuestion(
                        quizId = quizIds[0],
                        questionText = "Сколько ног у паука?"
                    )
                ))
                
                // Добавляем варианты ответов
                database.quizAnswerDao().insert(
                    QuizAnswer(
                        questionId = questionIds[0],
                        answerText = "Гепард",
                        isCorrect = true
                    )
                )
                
                database.quizAnswerDao().insert(
                    QuizAnswer(
                        questionId = questionIds[0],
                        answerText = "Слон",
                        isCorrect = false
                    )
                )
                
                database.quizAnswerDao().insert(
                    QuizAnswer(
                        questionId = questionIds[0],
                        answerText = "Лев",
                        isCorrect = false
                    )
                )
                
                database.quizAnswerDao().insert(
                    QuizAnswer(
                        questionId = questionIds[1],
                        answerText = "6",
                        isCorrect = false
                    )
                )
                
                database.quizAnswerDao().insert(
                    QuizAnswer(
                        questionId = questionIds[1],
                        answerText = "8",
                        isCorrect = true
                    )
                )
                
                database.quizAnswerDao().insert(
                    QuizAnswer(
                        questionId = questionIds[1],
                        answerText = "10",
                        isCorrect = false
                    )
                )
                
                // Добавляем достижения
                database.achievementDao().insertAll(
                    listOf(
                        Achievement(
                            title = "Первые шаги",
                            description = "Прочитай 5 статей",
                            iconName = "ic_achievement_reading",
                            points = 50,
                            targetProgress = 5,
                            type = "READ_ENTRIES"
                        ),
                        Achievement(
                            title = "Знаток животных",
                            description = "Пройди викторину о животных на 100%",
                            iconName = "ic_achievement_animals",
                            points = 100,
                            targetProgress = 1,
                            type = "COMPLETE_QUIZ"
                        ),
                        Achievement(
                            title = "Любознательный",
                            description = "Изучи статьи в 3 разных категориях",
                            iconName = "ic_achievement_categories",
                            points = 75,
                            targetProgress = 3,
                            type = "EXPLORE_CATEGORIES"
                        ),
                        Achievement(
                            title = "Книжный червь",
                            description = "Прочитай 20 статей",
                            iconName = "ic_achievement_bookworm",
                            points = 100,
                            targetProgress = 20,
                            type = "READ_ENTRIES"
                        ),
                        Achievement(
                            title = "Космонавт",
                            description = "Изучи все статьи о космосе",
                            iconName = "ic_achievement_space",
                            points = 150,
                            targetProgress = 5,
                            type = "EXPLORE_SPACE"
                        ),
                        Achievement(
                            title = "Эрудит",
                            description = "Пройди 5 различных викторин",
                            iconName = "ic_achievement_quiz",
                            points = 200,
                            targetProgress = 5,
                            type = "COMPLETE_QUIZ"
                        ),
                        Achievement(
                            title = "Коллекционер закладок",
                            description = "Добавь 10 статей в закладки",
                            iconName = "ic_achievement_bookmark",
                            points = 50,
                            targetProgress = 10,
                            type = "ADD_BOOKMARKS"
                        ),
                        Achievement(
                            title = "Активный комментатор",
                            description = "Оставь 15 комментариев к статьям",
                            iconName = "ic_achievement_comment",
                            points = 75,
                            targetProgress = 15,
                            type = "ADD_COMMENTS"
                        ),
                        Achievement(
                            title = "Исследователь природы",
                            description = "Изучи все статьи о животных и растениях",
                            iconName = "ic_achievement_nature",
                            points = 150,
                            targetProgress = 10,
                            type = "EXPLORE_NATURE"
                        ),
                        Achievement(
                            title = "Постоянный читатель",
                            description = "Заходи в приложение 7 дней подряд",
                            iconName = "ic_achievement_streak",
                            points = 100,
                            targetProgress = 7,
                            type = "DAILY_LOGIN"
                        )
                    )
                )
                
                Log.d("AppDatabase", "База данных успешно заполнена")
            } catch (e: Exception) {
                Log.e("AppDatabase", "Ошибка при заполнении базы данных: ${e.message}", e)
                throw e
            }
        }
    }
} 