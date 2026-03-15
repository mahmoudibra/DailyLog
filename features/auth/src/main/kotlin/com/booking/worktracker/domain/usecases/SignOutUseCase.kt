package com.booking.worktracker.domain.usecases

import com.booking.worktracker.data.repository.AuthRepository
import me.tatarka.inject.annotations.Inject

@Inject
class SignOutUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(token: String) {
        authRepository.deleteSession(token)
    }
}
