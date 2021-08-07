import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {

    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        // TODO: Remove this when on Gradle 7.2. https://github.com/gradle/gradle/pull/17394
        val libs = project.extensions.getByType<VersionCatalogsExtension>().named("libs") as org.gradle.accessors.dm.LibrariesForLibs

        classpath(libs.agp)
        classpath(libs.kotlin.gradlePlugin)

        classpath(libs.hilt.gradlePlugin)
        classpath(libs.sqldelight.gradlePlugin)
    }
}

allprojects {
    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            allWarningsAsErrors = true

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
