package com.booking.worktracker.data.repository

import com.booking.worktracker.data.datasource.AuthLocalDataSource
import com.booking.worktracker.data.models.RawUser
import com.booking.worktracker.data.models.Session
import com.booking.worktracker.data.models.User
import com.booking.worktracker.di.Singleton
import me.tatarka.inject.annotations.Inject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Inject
@Singleton
class AuthRepository(private val localDataSource: AuthLocalDataSource) {

    suspend fun getUserByEmail(email: String): User? {
        return localDataSource.getUserByEmail(email)
    }

    suspend fun getUserById(id: Long): User? {
        return localDataSource.getUserById(id)
    }

    suspend fun getRawUserByEmail(email: String): RawUser? {
        return localDataSource.getRawUserByEmail(email)
    }

    suspend fun createSession(userId: Long, token: String, hoursValid: Long): Session? {
        val expiresAt = LocalDateTime.now().plusHours(hoursValid)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        localDataSource.insertSession(userId, token, expiresAt)
        return localDataSource.getSessionByToken(token)
    }

    suspend fun insertUser(email: String, displayName: String, passwordHash: String, salt: String): Long {
        return localDataSource.insertUser(email, displayName, passwordHash, salt)
    }

    suspend fun updateDisplayName(userId: Long, displayName: String) {
        localDataSource.updateDisplayName(userId, displayName)
    }

    suspend fun updatePassword(userId: Long, passwordHash: String, salt: String) {
        localDataSource.updatePassword(userId, passwordHash, salt)
    }

    suspend fun deleteUser(userId: Long) {
        localDataSource.deleteUser(userId)
    }

    suspend fun getSessionByToken(token: String): Session? {
        return localDataSource.getSessionByToken(token)
    }

    suspend fun deleteSession(token: String) {
        localDataSource.deleteSession(token)
    }

    suspend fun deleteUserSessions(userId: Long) {
        localDataSource.deleteUserSessions(userId)
    }

    suspend fun deleteExpiredSessions() {
        localDataSource.deleteExpiredSessions()
    }
}
