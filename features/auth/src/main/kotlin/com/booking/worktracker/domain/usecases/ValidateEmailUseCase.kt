package com.booking.worktracker.domain.usecases

import me.tatarka.inject.annotations.Inject

@Inject
class ValidateEmailUseCase {
    operator fun invoke(email: String): ValidationResult {
        if (email.isBlank()) return ValidationResult.Error("Email is required")
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        if (!emailRegex.matches(email)) return ValidationResult.Error("Invalid email format")
        return ValidationResult.Valid
    }
}

sealed class ValidationResult {
    data object Valid : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}
