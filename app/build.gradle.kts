plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    // alias(libs.plugins.jetbrains.compose)
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
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
    implementation(platform("androidx.compose:compose-bom:${libs.versions.composeBom.get()}"))
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
    implementation("com.google.android.material:material:1.11.0")

    // Navigation Component (pour Compose)
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Retrofit et OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Moshi (pour la sérialisation JSON)
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.15.0")

    // Docker Api client - Add exclude for redundant Guava dependency
    implementation("com.github.docker-java:docker-java-core:3.3.4") {
        exclude(group = "com.google.guava", module = "guava")
    }
    implementation("com.github.docker-java:docker-java-transport-okhttp:3.3.4") {
        exclude(group = "com.google.guava", module = "guava")
    }

    // Add explicit dependency on a newer Guava version
    implementation("com.google.guava:guava:31.1-android")

    // MPAndroidChart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Lottie pour les animations
    implementation("com.airbnb.android:lottie-compose:6.2.0")

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
}}