plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.assignmentthree"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        manifestPlaceholders["MAPS_API_KEY"] = "AIzaSyBSreMV6eAt8necsd6QPQbKpwyCSf1u6wM"
        applicationId = "com.example.assignmentthree"
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
    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.android.libraries.places:places:3.2.0")
    implementation("com.google.maps.android:android-maps-utils:3.4.0")
    implementation(libs.google.play.services.maps)
    implementation(libs.google.play.services.places)
    implementation(libs.android.volley)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}