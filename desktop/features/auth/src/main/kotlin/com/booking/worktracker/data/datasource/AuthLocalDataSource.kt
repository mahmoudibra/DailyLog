package com.booking.worktracker.data.datasource

import com.booking.worktracker.data.DailyTrackerDatabase
import com.booking.worktracker.data.models.RawUser
import com.booking.worktracker.data.models.Session
import com.booking.worktracker.data.models.User
import com.booking.worktracker.di.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
@Singleton
class AuthLocalDataSource(
    db: DailyTrackerDatabase,
    private val ioDispatcher: CoroutineDispatcher
) {

    private val authQueries = db.authQueries

    suspend fun getUserByEmail(email: String): User? = withContext(ioDispatcher) {
        val row = authQueries.getUserByEmail(email).executeAsOneOrNull() ?: return@withContext null
        User(
            id = row.id,
            email = row.email,
            displayName = row.display_name,
            createdAt = row.created_at,
            updatedAt = row.updated_at
        )
    }

    suspend fun getUserById(id: Long): User? = withContext(ioDispatcher) {
        val row = authQueries.getUserById(id).executeAsOneOrNull() ?: return@withContext null
        User(
            id = row.id,
            email = row.email,
            displayName = row.display_name,
            createdAt = row.created_at,
            updatedAt = row.updated_at
        )
    }

    suspend fun getRawUserByEmail(email: String): RawUser? = withContext(ioDispatcher) {
        val row = authQueries.getUserByEmail(email).executeAsOneOrNull() ?: return@withContext null
        RawUser(
            id = row.id,
            email = row.email,
            displayName = row.display_name,
            passwordHash = row.password_hash,
            salt = row.salt
        )
    }

    suspend fun insertUser(email: String, displayName: String, passwordHash: String, salt: String): Long =
        withContext(ioDispatcher) {
            authQueries.transactionWithResult {
                authQueries.insertUser(email, displayName, passwordHash, salt)
                authQueries.lastInsertRowId().executeAsOne()
            }
        }

    suspend fun updateDisplayName(userId: Long, displayName: String) = withContext(ioDispatcher) {
        authQueries.updateUserDisplayName(displayName, userId)
    }

    suspend fun updatePassword(userId: Long, passwordHash: String, salt: String) = withContext(ioDispatcher) {
        authQueries.updateUserPassword(passwordHash, salt, userId)
    }

    suspend fun deleteUser(userId: Long) = withContext(ioDispatcher) {
        authQueries.deleteUser(userId)
    }

    suspend fun getSessionByToken(token: String): Session? = withContext(ioDispatcher) {
        val row = authQueries.getSessionByToken(token).executeAsOneOrNull() ?: return@withContext null
        Session(
            id = row.id,
            userId = row.user_id,
            token = row.token,
            expiresAt = row.expires_at,
            createdAt = row.created_at
        )
    }

    suspend fun insertSession(userId: Long, token: String, expiresAt: String) = withContext(ioDispatcher) {
        authQueries.insertSession(userId, token, expiresAt)
    }

    suspend fun deleteSession(token: String) = withContext(ioDispatcher) {
        authQueries.deleteSession(token)
    }

    suspend fun deleteUserSessions(userId: Long) = withContext(ioDispatcher) {
        authQueries.deleteUserSessions(userId)
    }

    suspend fun deleteExpiredSessions() = withContext(ioDispatcher) {
        authQueries.deleteExpiredSessions()
    }
}
