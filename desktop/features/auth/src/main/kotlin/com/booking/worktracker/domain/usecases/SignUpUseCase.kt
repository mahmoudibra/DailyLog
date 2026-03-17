package com.booking.worktracker.domain.usecases

import com.booking.worktracker.data.models.User
import com.booking.worktracker.data.repository.AuthRepository
import me.tatarka.inject.annotations.Inject

@Inject
class SignUpUseCase(
    private val authRepository: AuthRepository,
    private val passwordHasher: PasswordHasher,
    private val validateEmail: ValidateEmailUseCase,
    private val validatePassword: ValidatePasswordUseCase
) {
    sealed class Result {
        data class Success(val user: User) : Result()
        data class Error(val message: String) : Result()
    }

    suspend operator fun invoke(email: String, displayName: String, password: String, confirmPassword: String): Result {
        val validationError = validate(email, displayName, password, confirmPassword)
        if (validationError != null) return Result.Error(validationError)

        val trimmedEmail = email.trim().lowercase()
        if (authRepository.getUserByEmail(trimmedEmail) != null) {
            return Result.Error("An account with this email already exists")
        }

        val salt = passwordHasher.generateSalt()
        val hash = passwordHasher.hashPassword(password, salt)
        val userId = authRepository.insertUser(trimmedEmail, displayName.trim(), hash, salt)

        val user = authRepository.getUserById(userId) ?: return Result.Error("Failed to create account")
        return Result.Success(user)
    }

    private fun validate(email: String, displayName: String, password: String, confirmPassword: String): String? {
        val emailValidation = validateEmail(email.trim())
        if (emailValidation is ValidationResult.Error) return emailValidation.message
        if (displayName.isBlank()) return "Display name is required"
        val passwordValidation = validatePassword(password)
        if (passwordValidation is ValidationResult.Error) return passwordValidation.message
        if (password != confirmPassword) return "Passwords do not match"
        return null
    }
}
