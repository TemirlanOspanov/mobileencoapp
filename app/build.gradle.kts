plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.kapt")
    id("org.jetbrains.kotlin.plugin.parcelize")
    alias(libs.plugins.navigation.safeargs)
}

android {
    namespace = "com.example.myworldapp2"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myworldapp2"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Конфигурация для Room - экспорт схемы базы данных
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
                arguments["room.incremental"] = "true"
                arguments["room.expandProjection"] = "true"
                // Completely disable Room schema validation
                arguments["room.schemaValidator"] = "false"
                arguments["room.verifyTableNames"] = "false"
                arguments["room.autoMigration"] = "false"
                // Add fallback to destructive migration
                arguments["room.fallbackToDestructiveMigration"] = "true"
            }
        }

        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }

    lint {
        abortOnError = false
    }

    configurations.all {
        // Remove these exclusions as they're needed for viewModelScope
        // exclude(group = "androidx.lifecycle", module = "lifecycle-viewmodel-ktx")
        // exclude(group = "androidx.lifecycle", module = "lifecycle-livedata-ktx")
    }
}

// Room KAPT configuration
kapt {
   correctErrorTypes = true
   useBuildCache = true
   arguments {
       arg("room.schemaLocation", "$projectDir/schemas")
       arg("room.incremental", "true")
       arg("room.expandProjection", "true")
       arg("room.schemaValidator", "false")
   }
}

dependencies {
    // Core и UI
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.swiperefreshlayout)

    // OkHttp для HTTP-запросов
    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    // Google AI и Gemini
    implementation("com.google.ai.client.generativeai:generativeai:0.2.0")

    // Room компоненты - restored
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    // Lifecycle компоненты
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.livedata)

    // Navigation компоненты
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)

    // Preferences
    implementation(libs.androidx.preference)

    // ViewPager2
    implementation("androidx.viewpager2:viewpager2:1.0.0")

    // Glide для работы с изображениями
    implementation(libs.glide)

    // CircleImageView для отображения круглых аватаров
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // Тестирование
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")

    // Добавить явное указание более старой версии activity
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.activity:activity:1.8.2")
}

configurations.all {
    resolutionStrategy {
        // Принудительно используйте конкретные версии для разрешения конфликтов
        force("androidx.core:core-ktx:1.12.0")
        force("androidx.fragment:fragment-ktx:1.6.2")
        
        // Добавьте новые
        force("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
        force("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
        force("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
        force("org.jetbrains.kotlin:kotlin-stdlib:2.0.20")
        force("org.jetbrains.kotlin:kotlin-stdlib-common:2.0.20")
        
        // Игнорировать все версии библиотек, которые могут конфликтовать
        eachDependency {
            if (requested.group == "org.jetbrains.kotlin") {
                useVersion("2.0.20")
            }
        }

        // Принудительно использовать совместимые версии
        force("androidx.activity:activity-ktx:1.8.2")
        force("androidx.activity:activity:1.8.2")
    }
}