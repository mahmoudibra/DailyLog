package com.booking.worktracker.domain.usecases

import me.tatarka.inject.annotations.Inject

@Inject
class ValidatePasswordUseCase {
    operator fun invoke(password: String): ValidationResult {
        if (password.length < 8) return ValidationResult.Error("Password must be at least 8 characters")
        if (!password.any { it.isUpperCase() }) return ValidationResult.Error("Password must contain an uppercase letter")
        if (!password.any { it.isLowerCase() }) return ValidationResult.Error("Password must contain a lowercase letter")
        if (!password.any { it.isDigit() }) return ValidationResult.Error("Password must contain a digit")
        return ValidationResult.Valid
    }
}
