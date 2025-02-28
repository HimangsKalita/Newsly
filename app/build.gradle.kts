import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
    kotlin("kapt") version "2.1.0"
    id("com.google.dagger.hilt.android")
    id("androidx.navigation.safeargs")
    id("kotlin-parcelize")
}

android {
    namespace = "com.himangskalita.newsly"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.himangskalita.newsly"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        //Fetching API key from local.properties
        val envFile = project.rootProject.file("local.properties")
        val properties = Properties()
        properties.load(envFile.inputStream())
        val apiKey = properties.getProperty("API_KEY") ?: ""
        buildConfigField(
            type = "String",
            name = "API_KEY",
            value = apiKey
        )

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
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {

        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.webkit)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    //Hilt
    implementation(libs.hilt.android)
    annotationProcessor(libs.dagger.hilt.compiler)
    kapt(libs.dagger.hilt.android.compiler)

    //GSON
    implementation(libs.gson)

    //Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit2.converter.gson)
    implementation(libs.logging.interceptor)

    //Room database
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$roomVersion")
    // optional - Paging 3 Integration
    implementation("androidx.room:room-paging:$roomVersion")

    //Coil
    implementation(libs.coil)
    implementation(libs.coil3.coil.network.okhttp)

    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    // LiveData
    implementation(libs.lifecycle.livedata.ktx)
    // Saved state module for ViewModel
    implementation(libs.lifecycle.viewmodel.savedstate)
    // Annotation processor
    implementation(libs.lifecycle.common.java8)

    //kotlin Metadata
    //implementation(libs.kotlinx.metadata.jvm)
    implementation("org.jetbrains.kotlin:kotlin-metadata-jvm:2.1.0")

    // Facebook shimmer loading effect
    implementation("com.facebook.shimmer:shimmer:0.5.0")

    //Leakcanary
//    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")

    //swipt to refresh
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01")

    // Material 3
//    implementation(libs.material3)
    implementation("com.google.android.material:material:1.11.0")

    //androidx preference
    implementation("androidx.preference:preference-ktx:1.2.1")

    //Splash screen
    implementation("androidx.core:core-splashscreen:1.0.1t")
}


kapt {
    correctErrorTypes = true
}