plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)   // נדרש עם Kotlin 2.0 + Compose
    alias(libs.plugins.google.services)   // ← חדש

}

android {
    namespace = "com.example.growfit1"          // עדכן/י לשם החבילה בפועל
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.growfit1"  // עדכן/י אם שם החבילה שונה
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        vectorDrawables { useSupportLibrary = true }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    // תיקון חוסר התאימות: גם Java נבנית ל-17 (ולא 1.8)
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // לא מזיק גם עם הפלאגין החדש
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    // Kotlin JVM 17
    kotlinOptions { jvmTarget = "17" }

    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }
}

// נעול את טולצ'יין של קוטלין ל-JDK 17 (אופציונלי אבל מומלץ)
kotlin {
    jvmToolchain(17)
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.compose.material:material-icons-extended")


    // Firebase (Auth)
    implementation(platform("com.google.firebase:firebase-bom:33.3.0"))

    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")


    implementation("io.coil-kt:coil-compose:2.6.0")



    // נדרש ל-Tasks.await()
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

    implementation("io.coil-kt:coil-compose:2.6.0")


    // Material (XML theme) - אם אתה משתמש Theme.Material3.* ב-XML
    implementation("com.google.android.material:material:1.12.0")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // DataStore (נשתמש בו בהמשך ל-PIN)
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // בדיקות / דיבאג
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
