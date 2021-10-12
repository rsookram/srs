import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath(libs.agp)
        classpath(libs.kotlin.gradle)

        classpath(libs.hilt.gradle)
        classpath(libs.sqldelight.gradle)
    }
}

allprojects {
    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            allWarningsAsErrors = true

            freeCompilerArgs +=
                listOf(
                    "-Xopt-in=androidx.compose.animation.ExperimentalAnimationApi", // AnimatedVisibility
                    "-Xopt-in=androidx.compose.foundation.ExperimentalFoundationApi", // Modifier.combinedClickable
                    "-Xopt-in=androidx.compose.material.ExperimentalMaterialApi", // BackdropScaffold
                    "-Xopt-in=kotlinx.coroutines.FlowPreview", // Flow.debounce
                    "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi", // Flow.flatMapLatest
                )
        }
    }
}

tasks.register("clean", Delete::class) { delete(rootProject.buildDir) }
