// Root build file for DailyReminder monorepo.
// Desktop and server are included as composite builds via settings.gradle.kts.
// Use: ./gradlew :desktop:run  or  ./gradlew :server:run

tasks.register("clean") {
    dependsOn(gradle.includedBuilds.map { it.task(":clean") })
}
