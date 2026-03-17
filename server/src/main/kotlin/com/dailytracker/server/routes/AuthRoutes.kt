package com.dailytracker.server.routes

import com.dailytracker.server.models.AuthResponse
import com.dailytracker.server.models.ErrorResponse
import com.dailytracker.server.models.LoginRequest
import com.dailytracker.server.models.RegisterRequest
import com.dailytracker.server.plugins.generateToken
import com.dailytracker.server.repository.UserRepository
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(userRepository: UserRepository) {
    route("/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()

            if (request.email.isBlank() || request.password.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Email and password are required"))
                return@post
            }
            if (request.password.length < 6) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Password must be at least 6 characters"))
                return@post
            }

            val existing = userRepository.findByEmail(request.email)
            if (existing != null) {
                call.respond(HttpStatusCode.Conflict, ErrorResponse("Email already registered"))
                return@post
            }

            val user = userRepository.create(request.email, request.password)
            val token = application.generateToken(user.id, user.email)
            call.respond(HttpStatusCode.Created, AuthResponse(token, user.email))
        }

        post("/login") {
            val request = call.receive<LoginRequest>()

            val user = userRepository.findByEmail(request.email)
            if (user == null || !userRepository.verifyPassword(request.password, user.passwordHash)) {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid email or password"))
                return@post
            }

            val token = application.generateToken(user.id, user.email)
            call.respond(AuthResponse(token, user.email))
        }
    }
}
