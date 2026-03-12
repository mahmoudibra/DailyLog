package com.booking.worktracker.notifications

import java.io.IOException

object MacOSNotification {
    fun send(title: String, message: String, soundName: String = "Glass") {
        try {
            val escapedMessage = message.replace("\"", "\\\"")
            val escapedTitle = title.replace("\"", "\\\"")

            val command = """
                osascript -e 'display notification "$escapedMessage" with title "$escapedTitle" sound name "$soundName"'
            """.trimIndent()

            ProcessBuilder("bash", "-c", command)
                .start()
                .waitFor()
        } catch (e: IOException) {
            println("Failed to send notification: ${e.message}")
        }
    }
}
