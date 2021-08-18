plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")

    id("dagger.hilt.android.plugin")
    id("com.squareup.sqldelight")

    id("com.ncorti.ktfmt.gradle") version "0.6.0"
}

android {
    compileSdk = 31

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

        // This check goes against the Material Design guidelines:
        // https://material.io/components/dialogs#actions
        // https://material.io/components/buttons#anatomy
        disable("ButtonCase")
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
        kotlinCompilerExtensionVersion = libs.versions.compose.get()
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.hiltNavigationCompose)
    implementation(libs.androidx.viewmodelCompose)
    implementation(libs.androidx.navigation)
    implementation(libs.androidx.paging)

    implementation(libs.compose.ui)
    implementation(libs.compose.material)
    implementation(libs.compose.uiTooling)

    implementation(libs.accompanist.insets)
    implementation(libs.accompanist.insetsUi)
    implementation(libs.accompanist.systemuicontroller)

    implementation(libs.sqldelight.android)
    implementation(libs.sqldelight.coroutines)
    implementation(libs.sqldelight.paging)
    testImplementation(libs.sqldelight.test)

    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.compose.junit)
    testImplementation(libs.robolectric)
    debugImplementation(libs.compose.testManifest)
}

ktfmt {
    kotlinLangStyle()
}
