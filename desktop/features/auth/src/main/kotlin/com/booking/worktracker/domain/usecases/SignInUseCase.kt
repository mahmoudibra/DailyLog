package com.booking.worktracker.domain.usecases

import com.booking.worktracker.data.models.Session
import com.booking.worktracker.data.models.User
import com.booking.worktracker.data.repository.AuthRepository
import me.tatarka.inject.annotations.Inject
import java.util.UUID

@Inject
class SignInUseCase(
    private val authRepository: AuthRepository,
    private val passwordHasher: PasswordHasher
) {
    data class AuthResult(val user: User, val session: Session)

    sealed class Result {
        data class Success(val authResult: AuthResult) : Result()
        data class Error(val message: String) : Result()
    }

    suspend operator fun invoke(email: String, password: String, rememberMe: Boolean): Result {
        if (email.isBlank() || password.isBlank()) return Result.Error("Email and password are required")

        val trimmedEmail = email.trim().lowercase()
        val authenticatedUser = authenticate(trimmedEmail, password)
            ?: return Result.Error("Invalid email or password")

        authRepository.deleteExpiredSessions()

        val token = UUID.randomUUID().toString()
        val hoursValid = if (rememberMe) 720L else 24L
        val session = authRepository.createSession(authenticatedUser.id, token, hoursValid)
            ?: return Result.Error("Failed to create session")

        return Result.Success(AuthResult(authenticatedUser, session))
    }

    private suspend fun authenticate(email: String, password: String): User? {
        val rawUser = authRepository.getRawUserByEmail(email) ?: return null
        if (!passwordHasher.verifyPassword(password, rawUser.salt, rawUser.passwordHash)) return null
        return authRepository.getUserByEmail(email)
    }
}
