package com.dailytracker.server

import com.dailytracker.server.models.AuthResponse
import com.dailytracker.server.models.ErrorResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthRoutesTest {

    private fun ApplicationTestBuilder.jsonClient(): HttpClient {
        return createClient {
            install(ContentNegotiation) { json() }
        }
    }

    private fun ApplicationTestBuilder.setup() {
        environment { config = ApplicationConfig("application-test.conf") }
    }

    // --- Register: Happy Path ---

    @Test
    fun `register with valid credentials returns 201 and token`() = testApplication {
        setup()
        val client = jsonClient()

        val response = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"test@example.com","password":"password123"}""")
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val body = response.body<AuthResponse>()
        assertEquals("test@example.com", body.email)
        assertTrue(body.token.isNotBlank())
    }

    // --- Register: Validation Errors ---

    @Test
    fun `register with blank email returns 400`() = testApplication {
        setup()
        val client = jsonClient()

        val response = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"","password":"password123"}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val body = response.body<ErrorResponse>()
        assertEquals("Email and password are required", body.message)
    }

    @Test
    fun `register with blank password returns 400`() = testApplication {
        setup()
        val client = jsonClient()

        val response = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"test@example.com","password":""}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val body = response.body<ErrorResponse>()
        assertEquals("Email and password are required", body.message)
    }

    @Test
    fun `register with short password returns 400`() = testApplication {
        setup()
        val client = jsonClient()

        val response = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"test@example.com","password":"12345"}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val body = response.body<ErrorResponse>()
        assertEquals("Password must be at least 6 characters", body.message)
    }

    @Test
    fun `register with password exceeding 72 chars returns 400`() = testApplication {
        setup()
        val client = jsonClient()

        val longPassword = "a".repeat(73)
        val response = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"test@example.com","password":"$longPassword"}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val body = response.body<ErrorResponse>()
        assertEquals("Password must not exceed 72 characters", body.message)
    }

    @Test
    fun `register with invalid email format returns 400`() = testApplication {
        setup()
        val client = jsonClient()

        val response = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"not-an-email","password":"password123"}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val body = response.body<ErrorResponse>()
        assertEquals("Invalid email format", body.message)
    }

    @Test
    fun `register with email exceeding 254 chars returns 400`() = testApplication {
        setup()
        val client = jsonClient()

        val longEmail = "a".repeat(243) + "@example.com" // 255 chars
        val response = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"$longEmail","password":"password123"}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val body = response.body<ErrorResponse>()
        assertEquals("Email must not exceed 254 characters", body.message)
    }

    // --- Register: Duplicate ---

    @Test
    fun `register with duplicate email returns 409`() = testApplication {
        setup()
        val client = jsonClient()

        // First registration
        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"duplicate@example.com","password":"password123"}""")
        }

        // Second registration with same email
        val response = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"duplicate@example.com","password":"password456"}""")
        }

        assertEquals(HttpStatusCode.Conflict, response.status)
        val body = response.body<ErrorResponse>()
        assertEquals("Email already registered", body.message)
    }

    // --- Login: Happy Path ---

    @Test
    fun `login with valid credentials returns 200 and token`() = testApplication {
        setup()
        val client = jsonClient()

        // Register first
        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"login@example.com","password":"password123"}""")
        }

        // Login
        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"login@example.com","password":"password123"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<AuthResponse>()
        assertEquals("login@example.com", body.email)
        assertTrue(body.token.isNotBlank())
    }

    // --- Login: Failures ---

    @Test
    fun `login with wrong password returns 401`() = testApplication {
        setup()
        val client = jsonClient()

        // Register first
        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"wrongpass@example.com","password":"password123"}""")
        }

        // Login with wrong password
        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"wrongpass@example.com","password":"wrongpassword"}""")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        val body = response.body<ErrorResponse>()
        assertEquals("Invalid email or password", body.message)
    }

    @Test
    fun `login with nonexistent user returns 401`() = testApplication {
        setup()
        val client = jsonClient()

        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"nobody@example.com","password":"password123"}""")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        val body = response.body<ErrorResponse>()
        assertEquals("Invalid email or password", body.message)
    }
}
