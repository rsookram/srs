buildscript {

    val compose_version by extra("1.0.0-beta08")
    val sqldelight_version by extra("1.5.0")

    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.0.0-beta03")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.10")

        classpath("com.squareup.sqldelight:gradle-plugin:$sqldelight_version")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
