plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // El plugin google-services procesa el archivo google-services.json en app/
    // Si Firebase devuelve "Requests from this Android client are blocked":
    //   1. Descarga google-services.json desde Firebase Console → Configuracion del proyecto → Tu app Android
    //   2. Reemplaza el archivo app/google-services.json con el descargado
    //   3. Registra el SHA-1 del dispositivo en Firebase Console → Tu app Android → Agregar huella digital
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.urumbox"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.urumbox"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // BOM controla versiones de todas las dependencias Firebase — no mezclar versiones explicitas con BOM
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")

    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)
    implementation(libs.mlkit.barcode)
    implementation(libs.zxing.core)
}

// PASOS MANUALES DESPUES DE ESTE MERGE (hacer en Android Studio):
// 1. Build → Clean Project
// 2. Build → Rebuild Project
// 3. File → Sync Project with Gradle Files
// 4. Si el error persiste: Invalidate Caches and Restart (File → Invalidate Caches)
// 5. Registrar el SHA-1 que aparece en Logcat con tag "SHA1_KEY" en:
//    Firebase Console → Configuracion del proyecto → Tu app Android → Agregar huella digital
