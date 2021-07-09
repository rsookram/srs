plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")

    id("dagger.hilt.android.plugin")
    id("com.squareup.sqldelight")
}

android {
    compileSdk = 30

    defaultConfig {
        applicationId = "io.github.rsookram.srs"
        minSdk = 28
        targetSdk = 30
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles("proguard-rules.pro")
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = rootProject.extra["compose_version"] as String
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.6.0")
    implementation("androidx.activity:activity-compose:1.3.0-rc01")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:1.0.0-alpha07")
    implementation("androidx.navigation:navigation-compose:2.4.0-alpha04")
    implementation("androidx.paging:paging-compose:1.0.0-alpha11")

    implementation("androidx.compose.ui:ui:${rootProject.extra["compose_version"]}")
    implementation("androidx.compose.material:material:${rootProject.extra["compose_version"]}")
    implementation("androidx.compose.ui:ui-tooling:${rootProject.extra["compose_version"]}")

    implementation("com.squareup.sqldelight:android-driver:${rootProject.extra["sqldelight_version"]}")
    implementation("com.squareup.sqldelight:coroutines-extensions-jvm:${rootProject.extra["sqldelight_version"]}")
    implementation("com.squareup.sqldelight:android-paging3-extensions:${rootProject.extra["sqldelight_version"]}")
    testImplementation("com.squareup.sqldelight:sqlite-driver:${rootProject.extra["sqldelight_version"]}")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${rootProject.extra["coroutines_version"]}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${rootProject.extra["coroutines_version"]}")

    implementation("com.google.dagger:hilt-android:${rootProject.extra["hilt_version"]}")
    kapt("com.google.dagger:hilt-compiler:${rootProject.extra["hilt_version"]}")

    testImplementation("junit:junit:4.13.2")
}
