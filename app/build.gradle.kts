plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")

    id("dagger.hilt.android.plugin")
    id("com.squareup.sqldelight")

    id("com.ncorti.ktfmt.gradle") version "0.6.0"
}

android {
    compileSdk = 30

    defaultConfig {
        applicationId = "io.github.rsookram.srs"

        minSdk = 28
        targetSdk = 30

        versionCode = 1
        versionName = "1.0"

        resourceConfigurations += setOf("en", "anydpi")
    }

    lint {
        // Lint is run on CI
        isCheckReleaseBuilds = false

        textReport = true

        isWarningsAsErrors = true
        isAbortOnError = true
    }

    packagingOptions.resources {
        excludes += setOf(
            "kotlin/**",
            "META-INF/*.version",
        )
    }

    dependenciesInfo {
        includeInApk = false
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }

        if (file("app.keystore").exists()) {
            create("release") {
                storeFile = file("app.keystore")
                storePassword = project.property("STORE_PASSWORD").toString()
                keyAlias = project.property("KEY_ALIAS").toString()
                keyPassword = project.property("KEY_PASSWORD").toString()
            }
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }

        release {
            signingConfig =
                signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")

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
    implementation("androidx.activity:activity-compose:1.3.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0-alpha03")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:1.0.0-alpha07")
    implementation("androidx.navigation:navigation-compose:2.4.0-alpha06")
    implementation("androidx.paging:paging-compose:1.0.0-alpha12")

    implementation("androidx.compose.ui:ui:${rootProject.extra["compose_version"]}")
    implementation("androidx.compose.material:material:${rootProject.extra["compose_version"]}")
    implementation("androidx.compose.ui:ui-tooling:${rootProject.extra["compose_version"]}")

    implementation("com.google.accompanist:accompanist-insets:${rootProject.extra["accompanist_version"]}")
    implementation("com.google.accompanist:accompanist-insets-ui:${rootProject.extra["accompanist_version"]}")
    implementation("com.google.accompanist:accompanist-systemuicontroller:${rootProject.extra["accompanist_version"]}")

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

ktfmt {
    kotlinLangStyle()
}
