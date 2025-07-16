plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.busviet"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.busviet"
        minSdk = 24
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
    }
}

dependencies {
    implementation("com.google.firebase:firebase-database")

    implementation("com.google.firebase:firebase-auth:22.3.0")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.google.code.gson:gson:2.8.5")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation(mapOf("name" to "merchant-1.0.25", "ext" to "aar"))

    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.google.android.material:material:1.12.0")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.google.android.libraries.places:places:3.3.0")
    implementation ("com.airbnb.android:lottie:6.1.0")
}
