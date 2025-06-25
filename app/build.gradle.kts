plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Este plugin é essencial para o Jetpack Compose
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.pomodo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.pomodo"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        compose = true
    }
    composeOptions {
        // ESSENCIAL: Defina a versão da extensão do compilador Kotlin para Compose
        // Use uma versão compatível com seu Android Studio e Kotlin
        kotlinCompilerExtensionVersion = "1.5.1" // Pode precisar ser atualizado conforme sua versão do Compose BOM
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    // BOM (Bill of Materials) do Compose para gerenciar as versões das bibliotecas Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Dependências do Compose Navigation - Essas são cruciais para NavHost e rememberNavController
    implementation("androidx.navigation:navigation-compose:2.7.0") // Mantenha esta dependência explicitamente

    // Dependência do Lifecycle ViewModel para Compose (já estava lá, mas reconfirmando)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}