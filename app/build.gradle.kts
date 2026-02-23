plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.drive_kit"

    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.drive_kit"
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

configurations.configureEach {
    if (name.contains("AndroidTest", ignoreCase = true)) {
        exclude(group = "com.google.protobuf", module = "protobuf-lite")
    }
}

dependencies {

    // AndroidX / UI
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.gridlayout:gridlayout:1.0.0")
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Firebase (BOM פעם אחת)
    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")

    // Work / utils
    implementation("androidx.work:work-runtime:2.9.0")
    implementation("com.google.guava:guava:31.1-android")
    implementation("com.google.code.gson:gson:2.10.1")

    // Google services
    implementation("com.google.android.gms:play-services-auth:21.5.0")
    implementation("com.google.android.gms:play-services-maps:20.0.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.libraries.places:places:5.1.1")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.firebase.crashlytics.buildtools)
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // osmdroid
    implementation("org.osmdroid:osmdroid-android:6.1.18")

    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // ✅ PDF Viewer (רק זה!)
    implementation("com.github.mhiew:android-pdf-viewer:3.2.0-beta.1") {
        exclude(group = "com.android.support")
    }

    // Tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.11.0")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1") {
        exclude(group = "com.google.protobuf", module = "protobuf-lite")
    }
    implementation("com.tom-roush:pdfbox-android:2.0.27.0")
}
