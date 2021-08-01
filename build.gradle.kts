import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {

    val accompanist_version by extra("0.15.0")
    val compose_version by extra("1.0.0")
    val coroutines_version by extra("1.5.0")
    val hilt_version by extra("2.38.1")
    val sqldelight_version by extra("1.5.1")

    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.0.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.10")

        classpath("com.google.dagger:hilt-android-gradle-plugin:$hilt_version")
        classpath("com.squareup.sqldelight:gradle-plugin:$sqldelight_version")
    }
}

allprojects {
    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            freeCompilerArgs += listOf(
                "-Xopt-in=androidx.compose.foundation.ExperimentalFoundationApi", // Modifier.combinedClickable
                "-Xopt-in=androidx.compose.material.ExperimentalMaterialApi", // BackdropScaffold
                "-Xopt-in=kotlinx.coroutines.FlowPreview", // Flow.debounce
                "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi", // Flow.flatMapLatest
            )
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
