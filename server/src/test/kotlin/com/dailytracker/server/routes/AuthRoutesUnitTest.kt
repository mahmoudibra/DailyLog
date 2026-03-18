package com.dailytracker.server.routes

import com.dailytracker.server.models.AuthResponse
import com.dailytracker.server.models.ErrorResponse
import com.dailytracker.server.plugins.configureAuthentication
import com.dailytracker.server.plugins.configureSerialization
import com.dailytracker.server.plugins.configureStatusPages
import com.dailytracker.server.repository.UserRecord
import com.dailytracker.server.repository.UserRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthRoutesUnitTest {

    private val mockRepo = mockk<UserRepository>()
    private val testUserId = UUID.randomUUID()
    private val testPasswordHash = "\$2a\$12\$hashedpasswordvalue"

    private fun ApplicationTestBuilder.setupMinimalApp() {
        environment { config = ApplicationConfig("application-unit-test.conf") }
        application {
            configureSerialization()
            configureStatusPages()
            configureAuthentication()
            routing {
                authRoutes(mockRepo)
            }
        }
    }

    private fun ApplicationTestBuilder.jsonClient(): HttpClient {
        return createClient {
            install(ContentNegotiation) { json() }
        }
    }

    // --- Register: route logic unit tests ---

    @Test
    fun `register calls repository create on valid input`() = testApplication {
        setupMinimalApp()
        val client = jsonClient()

        every { mockRepo.findByEmail("new@example.com") } returns null
        every { mockRepo.create("new@example.com", "password123") } returns UserRecord(testUserId, "new@example.com", testPasswordHash)

        val response = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"new@example.com","password":"password123"}""")
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val body = response.body<AuthResponse>()
        assertEquals("new@example.com", body.email)
        assertTrue(body.token.isNotBlank())

        verify(exactly = 1) { mockRepo.findByEmail("new@example.com") }
        verify(exactly = 1) { mockRepo.create("new@example.com", "password123") }
    }

    @Test
    fun `register does not call repository on validation failure`() = testApplication {
        setupMinimalApp()
        val client = jsonClient()

        val response = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"bad-email","password":"password123"}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)

        verify(exactly = 0) { mockRepo.findByEmail(any()) }
        verify(exactly = 0) { mockRepo.create(any(), any()) }
    }

    @Test
    fun `register trims email before checking`() = testApplication {
        setupMinimalApp()
        val client = jsonClient()

        every { mockRepo.findByEmail("trimmed@example.com") } returns null
        every { mockRepo.create("trimmed@example.com", "password123") } returns UserRecord(testUserId, "trimmed@example.com", testPasswordHash)

        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"  trimmed@example.com  ","password":"password123"}""")
        }

        verify { mockRepo.findByEmail("trimmed@example.com") }
        verify { mockRepo.create("trimmed@example.com", "password123") }
    }

    @Test
    fun `register returns conflict when email exists`() = testApplication {
        setupMinimalApp()
        val client = jsonClient()

        every { mockRepo.findByEmail("exists@example.com") } returns UserRecord(testUserId, "exists@example.com", testPasswordHash)

        val response = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"exists@example.com","password":"password123"}""")
        }

        assertEquals(HttpStatusCode.Conflict, response.status)
        verify(exactly = 0) { mockRepo.create(any(), any()) }
    }

    // --- Login: route logic unit tests ---

    @Test
    fun `login calls repository findByEmail and verifyPassword`() = testApplication {
        setupMinimalApp()
        val client = jsonClient()

        every { mockRepo.findByEmail("user@example.com") } returns UserRecord(testUserId, "user@example.com", testPasswordHash)
        every { mockRepo.verifyPassword("password123", testPasswordHash) } returns true

        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"user@example.com","password":"password123"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<AuthResponse>()
        assertEquals("user@example.com", body.email)

        verify(exactly = 1) { mockRepo.findByEmail("user@example.com") }
        verify(exactly = 1) { mockRepo.verifyPassword("password123", testPasswordHash) }
    }

    @Test
    fun `login returns 401 when user not found`() = testApplication {
        setupMinimalApp()
        val client = jsonClient()

        every { mockRepo.findByEmail("nobody@example.com") } returns null

        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"nobody@example.com","password":"password123"}""")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        verify(exactly = 0) { mockRepo.verifyPassword(any(), any()) }
    }

    @Test
    fun `login returns 401 when password is wrong`() = testApplication {
        setupMinimalApp()
        val client = jsonClient()

        every { mockRepo.findByEmail("user@example.com") } returns UserRecord(testUserId, "user@example.com", testPasswordHash)
        every { mockRepo.verifyPassword("wrongpass", testPasswordHash) } returns false

        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"user@example.com","password":"wrongpass"}""")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        val body = response.body<ErrorResponse>()
        assertEquals("Invalid email or password", body.message)
    }

    @Test
    fun `login trims email before lookup`() = testApplication {
        setupMinimalApp()
        val client = jsonClient()

        every { mockRepo.findByEmail("user@example.com") } returns UserRecord(testUserId, "user@example.com", testPasswordHash)
        every { mockRepo.verifyPassword("password123", testPasswordHash) } returns true

        client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"  user@example.com  ","password":"password123"}""")
        }

        verify { mockRepo.findByEmail("user@example.com") }
    }
}
