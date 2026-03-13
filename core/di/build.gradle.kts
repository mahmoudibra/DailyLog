plugins {
    kotlin("jvm") version "2.1.0"
}

group = "com.booking.worktracker"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
}

dependencies {
    api("me.tatarka.inject:kotlin-inject-runtime:0.7.2")
    api("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
}
