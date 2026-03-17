package com.dailytracker.server.repository

import at.favre.lib.crypto.bcrypt.BCrypt
import com.dailytracker.server.models.Users
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.OffsetDateTime
import java.util.UUID

data class UserRecord(
    val id: UUID,
    val email: String,
    val passwordHash: String
)

class UserRepository {

    fun findByEmail(email: String): UserRecord? = transaction {
        Users.selectAll().where { Users.email eq email }
            .map { it.toUserRecord() }
            .singleOrNull()
    }

    fun create(email: String, password: String): UserRecord = transaction {
        val hash = BCrypt.withDefaults().hashToString(12, password.toCharArray())
        val newId = UUID.randomUUID()
        Users.insert {
            it[id] = newId
            it[Users.email] = email
            it[passwordHash] = hash
            it[createdAt] = OffsetDateTime.now()
        }
        UserRecord(newId, email, hash)
    }

    fun verifyPassword(plainPassword: String, hashedPassword: String): Boolean {
        return BCrypt.verifyer().verify(plainPassword.toCharArray(), hashedPassword).verified
    }

    private fun ResultRow.toUserRecord() = UserRecord(
        id = this[Users.id],
        email = this[Users.email],
        passwordHash = this[Users.passwordHash]
    )
}
