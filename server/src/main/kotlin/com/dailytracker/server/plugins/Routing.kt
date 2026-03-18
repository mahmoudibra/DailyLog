package com.dailytracker.server.plugins

import com.dailytracker.server.repository.UserRepository
import com.dailytracker.server.routes.authRoutes
import com.dailytracker.server.routes.healthRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val userRepository = UserRepository()

    routing {
        healthRoutes()
        authRoutes(userRepository)
    }
}
