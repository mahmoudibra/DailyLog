import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "2.1.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0"
    id("org.jetbrains.compose") version "1.10.2"
    id("io.gitlab.arturbosch.detekt") version "1.23.7"
    id("app.cash.sqldelight") version "2.0.2" apply false
    id("com.google.devtools.ksp") version "2.1.0-1.0.29" apply false
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    detekt {
        buildUponDefaultConfig = true
        config.setFrom(files("$rootDir/detekt.yml"))
    }
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(files("$rootDir/detekt.yml"))
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
        html.required.set(true)
        xml.required.set(false)
        txt.required.set(false)
    }
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
    // Feature modules
    implementation(project(":core:database"))
    implementation(project(":core:designsystem"))
    implementation(project(":features:dailylog"))
    implementation(project(":features:objectives"))
    implementation(project(":features:settings"))
    implementation(project(":features:timetracking"))
    implementation(project(":features:analytics"))
    implementation(project(":features:export"))
    implementation(project(":features:reviews"))
    implementation(project(":features:focuszones"))
    implementation(project(":features:timebudgets"))
    implementation(project(":features:habits"))
    implementation(project(":features:splash"))
    implementation(project(":features:achievements"))
    implementation(project(":features:auth"))

    // Compose Desktop
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(compose.components.resources)

    // Date/Time
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.9.0")

    // Logging (silence SLF4J warning from JDBC driver)
    implementation("org.slf4j:slf4j-nop:2.0.9")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.freeCompilerArgs += "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
}

compose.desktop {
    application {
        mainClass = "com.booking.worktracker.MainKt"

        jvmArgs += listOf("-Djavax.accessibility.assistive_technologies= ")

        nativeDistributions {
            targetFormats(TargetFormat.Dmg)
            packageName = "DailyTracker"
            packageVersion = "1.0.0"

            macOS {
                val icon = project.file("src/main/resources/icon.icns")
                if (icon.exists()) {
                    iconFile.set(icon)
                }
            }
        }
    }
}
