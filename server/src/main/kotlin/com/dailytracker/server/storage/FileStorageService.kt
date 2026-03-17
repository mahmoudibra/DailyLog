package com.dailytracker.server.storage

import java.io.File
import java.io.InputStream
import java.util.UUID

class FileStorageService(private val uploadDir: String) {

    init {
        File(uploadDir).mkdirs()
    }

    fun save(userId: UUID, fileName: String, inputStream: InputStream): Pair<String, Long> {
        val userDir = File(uploadDir, userId.toString())
        userDir.mkdirs()

        val storedFileName = "${UUID.randomUUID()}_$fileName"
        val file = File(userDir, storedFileName)
        inputStream.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        val relativePath = "${userId}/$storedFileName"
        return relativePath to file.length()
    }

    fun get(filePath: String): File? {
        val file = File(uploadDir, filePath)
        return if (file.exists()) file else null
    }

    fun delete(filePath: String): Boolean {
        val file = File(uploadDir, filePath)
        return file.delete()
    }
}
