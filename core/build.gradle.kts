plugins {
    kotlin("jvm") version "2.1.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0"
    id("org.jetbrains.compose") version "1.10.2"
    id("app.cash.sqldelight")
    id("com.google.devtools.ksp")
}

group = "com.booking.worktracker"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

configurations.all {
    resolutionStrategy {
        force("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
        force("org.jetbrains.kotlinx:kotlinx-datetime-jvm:0.6.1")
    }
}

dependencies {
    // kotlin-inject
    api("me.tatarka.inject:kotlin-inject-runtime:0.7.2")
    ksp("me.tatarka.inject:kotlin-inject-compiler-ksp:0.7.2")

    // Compose Desktop
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(compose.components.resources)

    // Lifecycle ViewModel
    api("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")

    // SQLDelight
    api("app.cash.sqldelight:sqlite-driver:2.0.2")
    api("app.cash.sqldelight:coroutines-extensions-jvm:2.0.2")

    // Date/Time
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.9.0")
}

compose.resources {
    publicResClass = true
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.freeCompilerArgs += "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
}

sqldelight {
    databases {
        create("DailyWorkTrackerDatabase") {
            packageName.set("com.booking.worktracker.data")
            dialect("app.cash.sqldelight:sqlite-3-38-dialect:2.0.2")
        }
    }
}
