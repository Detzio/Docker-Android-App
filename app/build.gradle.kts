plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.dockerapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.dockerapp"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    // Add packaging configuration to handle duplicate files
    packaging {
        resources {
            excludes += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
            // Also exclude other common conflicting files that might cause issues
            excludes += "META-INF/LICENSE*"
            excludes += "META-INF/NOTICE*"
            excludes += "META-INF/*.SF"
            excludes += "META-INF/*.DSA"
            excludes += "META-INF/*.RSA"
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
        compose = true
    }

    // Correction de la syntaxe des sourceSets
    sourceSets {
        getByName("main") {
            res.srcDirs(
                "src/main/res",
                "src/main/res/font"
            )
        }
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
}

configurations.all {
    resolutionStrategy {
        // Forces the newer version of Guava
        force("com.google.guava:guava:31.1-android")
        // Exclude the standalone listenablefuture artifact
        exclude(group = "com.google.guava", module = "listenablefuture")
    }
}

dependencies {
    // Compose
    implementation(platform("androidx.compose:compose-bom:2025.03.01"))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity.compose)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Core et lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Material Design Components
    implementation("com.google.android.material:material:1.12.0")

    // Navigation Component (pour Compose)
    implementation("androidx.navigation:navigation-compose:2.8.9")

    // Retrofit et OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Moshi (pour la sérialisation JSON)
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.2")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.2")

    // Docker Api client - Add exclude for redundant Guava dependency
    implementation("com.github.docker-java:docker-java-core:3.5.0") {
        exclude(group = "com.google.guava", module = "guava")
    }
    implementation("com.github.docker-java:docker-java-transport-okhttp:3.5.0") {
        exclude(group = "com.google.guava", module = "guava")
    }

    // Add explicit dependency on a newer Guava version
    implementation("com.google.guava:guava:33.4.6-android")

    // MPAndroidChart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Lottie pour les animations
    implementation("com.airbnb.android:lottie-compose:6.6.4")

    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Network
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.4")

    // SystemUiController pour la gestion de la barre d'état
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.32.0")

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}