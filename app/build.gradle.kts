plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
}

android {
    namespace = "com.example.evchargingapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.evchargingapp"
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.maps)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(platform("com.google.firebase:firebase-bom:32.3.1"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    // ama bazen doğrudan eklemek sorunu çözer:
    implementation("com.google.code.gson:gson:2.8.9")

    // Retrofit çekirdeği
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // JSON parsing için GSON converter
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // (Opsiyonel) HTTP loglama
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.0")
}
apply(plugin = "com.google.gms.google-services")