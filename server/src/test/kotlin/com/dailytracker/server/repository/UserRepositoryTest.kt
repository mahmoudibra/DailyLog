package com.dailytracker.server.repository

import com.dailytracker.server.models.Users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class UserRepositoryTest {

    private lateinit var repository: UserRepository

    @BeforeTest
    fun setup() {
        Database.connect(
            url = "jdbc:h2:mem:test_repo_${System.nanoTime()};MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        )
        transaction {
            SchemaUtils.create(Users)
        }
        repository = UserRepository()
    }

    // --- create ---

    @Test
    fun `create stores user and returns UserRecord`() {
        val user = repository.create("test@example.com", "password123")

        assertNotNull(user.id)
        assertEquals("test@example.com", user.email)
        assertTrue(user.passwordHash.isNotBlank())
    }

    @Test
    fun `create hashes the password with BCrypt`() {
        val user = repository.create("test@example.com", "password123")

        // BCrypt hashes start with $2a$
        assertTrue(user.passwordHash.startsWith("\$2a\$"))
    }

    @Test
    fun `create generates unique ids for different users`() {
        val user1 = repository.create("user1@example.com", "password123")
        val user2 = repository.create("user2@example.com", "password123")

        assertTrue(user1.id != user2.id)
    }

    // --- findByEmail ---

    @Test
    fun `findByEmail returns user when exists`() {
        repository.create("find@example.com", "password123")

        val found = repository.findByEmail("find@example.com")

        assertNotNull(found)
        assertEquals("find@example.com", found.email)
    }

    @Test
    fun `findByEmail returns null when user does not exist`() {
        val found = repository.findByEmail("nonexistent@example.com")

        assertNull(found)
    }

    @Test
    fun `findByEmail is case sensitive`() {
        repository.create("Test@Example.com", "password123")

        assertNull(repository.findByEmail("test@example.com"))
        assertNotNull(repository.findByEmail("Test@Example.com"))
    }

    // --- verifyPassword ---

    @Test
    fun `verifyPassword returns true for correct password`() {
        val user = repository.create("verify@example.com", "correctpassword")

        assertTrue(repository.verifyPassword("correctpassword", user.passwordHash))
    }

    @Test
    fun `verifyPassword returns false for wrong password`() {
        val user = repository.create("verify@example.com", "correctpassword")

        assertFalse(repository.verifyPassword("wrongpassword", user.passwordHash))
    }

    @Test
    fun `verifyPassword returns false for empty password`() {
        val user = repository.create("verify@example.com", "correctpassword")

        assertFalse(repository.verifyPassword("", user.passwordHash))
    }
}
