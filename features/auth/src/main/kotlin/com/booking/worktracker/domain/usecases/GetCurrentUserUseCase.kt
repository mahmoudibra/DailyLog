package com.booking.worktracker.domain.usecases

import com.booking.worktracker.data.models.User
import com.booking.worktracker.data.repository.AuthRepository
import me.tatarka.inject.annotations.Inject

@Inject
class GetCurrentUserUseCase(
    private val authRepository: AuthRepository
) {
    data class AuthState(val user: User, val token: String)

    suspend operator fun invoke(token: String?): AuthState? {
        if (token.isNullOrBlank()) return null
        val session = authRepository.getSessionByToken(token) ?: return null
        val user = authRepository.getUserById(session.userId) ?: return null
        return AuthState(user, token)
    }
}
