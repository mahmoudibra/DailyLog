package com.dailytracker.server

import com.dailytracker.server.plugins.configureAuthentication
import com.dailytracker.server.plugins.configureDatabase
import com.dailytracker.server.plugins.configureRouting
import com.dailytracker.server.plugins.configureSerialization
import com.dailytracker.server.plugins.configureStatusPages
import com.dailytracker.server.storage.FileStorageService
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    val uploadDir = environment.config.propertyOrNull("storage.uploadDir")?.getString() ?: "./uploads"
    val fileStorageService = FileStorageService(uploadDir)

    configureSerialization()
    configureStatusPages()
    configureDatabase()
    configureAuthentication()
    configureRouting(fileStorageService)
}
