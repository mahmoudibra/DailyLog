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
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("AuthRoutes")
private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
private const val MAX_EMAIL_LENGTH = 254
private const val MIN_PASSWORD_LENGTH = 6
private const val MAX_PASSWORD_LENGTH = 72

fun Route.authRoutes(userRepository: UserRepository) {
    route("/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()
            val email = request.email.trim()
            val password = request.password

            if (email.isBlank() || password.isBlank()) {
                log.warn("Registration failed: empty email or password")
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Email and password are required"))
                return@post
            }
            if (email.length > MAX_EMAIL_LENGTH) {
                log.warn("Registration failed: email exceeds max length for {}", email)
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Email must not exceed $MAX_EMAIL_LENGTH characters"))
                return@post
            }
            if (!EMAIL_REGEX.matches(email)) {
                log.warn("Registration failed: invalid email format for {}", email)
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid email format"))
                return@post
            }
            if (password.length < MIN_PASSWORD_LENGTH) {
                log.warn("Registration failed: password too short for {}", email)
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Password must be at least $MIN_PASSWORD_LENGTH characters"))
                return@post
            }
            if (password.length > MAX_PASSWORD_LENGTH) {
                log.warn("Registration failed: password too long for {}", email)
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Password must not exceed $MAX_PASSWORD_LENGTH characters"))
                return@post
            }

            val existing = userRepository.findByEmail(email)
            if (existing != null) {
                log.warn("Registration failed: email already registered {}", email)
                call.respond(HttpStatusCode.Conflict, ErrorResponse("Email already registered"))
                return@post
            }

            val user = userRepository.create(email, password)
            val token = application.generateToken(user.id, user.email)
            log.info("User registered successfully: {}", email)
            call.respond(HttpStatusCode.Created, AuthResponse(token, user.email))
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            val email = request.email.trim()

            val user = userRepository.findByEmail(email)
            if (user == null || !userRepository.verifyPassword(request.password, user.passwordHash)) {
                log.warn("Login failed for {}", email)
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid email or password"))
                return@post
            }

            val token = application.generateToken(user.id, user.email)
            log.info("User logged in: {}", email)
            call.respond(AuthResponse(token, user.email))
        }
    }
}
