plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.finalproject"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.finalproject"
        minSdk = 24
        targetSdk = 36
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
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // ⭐ REQUIRED FOR REAL DEVICES
    implementation("com.google.android.gms:play-services-base:18.5.0")
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // ⭐ Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.6.0"))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    // OKHTTP (REQUIRED FOR PDF UPLOAD)
    implementation("com.squareup.okhttp3:okhttp:4.10.0")

    // ⭐ ROOM Database
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    // ⭐ GLIDE (IMAGE LOADING)  *** MAIN FIX ***
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
