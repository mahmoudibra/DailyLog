package com.dailytracker.server.routes

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class HealthResponse(val status: String)

fun Route.healthRoutes() {
    get("/health") {
        call.respond(HttpStatusCode.OK, HealthResponse(status = "ok"))
    }
}
