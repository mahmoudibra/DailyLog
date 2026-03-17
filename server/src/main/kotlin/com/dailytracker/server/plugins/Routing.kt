package com.dailytracker.server.plugins

import com.dailytracker.server.repository.SnapshotRepository
import com.dailytracker.server.repository.UserRepository
import com.dailytracker.server.routes.authRoutes
import com.dailytracker.server.routes.snapshotRoutes
import com.dailytracker.server.storage.FileStorageService
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting(fileStorageService: FileStorageService) {
    val userRepository = UserRepository()
    val snapshotRepository = SnapshotRepository()

    routing {
        authRoutes(userRepository)
        snapshotRoutes(snapshotRepository, fileStorageService)
    }
}
