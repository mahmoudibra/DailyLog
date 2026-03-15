plugins {
    kotlin("jvm") version "2.1.0"
    id("com.google.devtools.ksp")
}

group = "com.booking.worktracker"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
}

dependencies {
    api("me.tatarka.inject:kotlin-inject-runtime:0.7.2")
    ksp("me.tatarka.inject:kotlin-inject-compiler-ksp:0.7.2")
    api("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
}
