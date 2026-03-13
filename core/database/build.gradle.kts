plugins {
    kotlin("jvm") version "2.1.0"
    id("app.cash.sqldelight")
    id("com.google.devtools.ksp")
}

group = "com.booking.worktracker"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
}

configurations.all {
    resolutionStrategy {
        force("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
        force("org.jetbrains.kotlinx:kotlinx-datetime-jvm:0.6.1")
    }
}

dependencies {
    api(project(":core:di"))
    ksp("me.tatarka.inject:kotlin-inject-compiler-ksp:0.7.2")
    api("app.cash.sqldelight:sqlite-driver:2.0.2")
    api("app.cash.sqldelight:coroutines-extensions-jvm:2.0.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
}

sqldelight {
    databases {
        create("DailyWorkTrackerDatabase") {
            packageName.set("com.booking.worktracker.data")
            dialect("app.cash.sqldelight:sqlite-3-38-dialect:2.0.2")
        }
    }
}
