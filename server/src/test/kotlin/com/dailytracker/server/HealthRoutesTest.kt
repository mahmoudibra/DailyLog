package com.dailytracker.server

import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import com.dailytracker.server.routes.HealthResponse
import kotlin.test.Test
import kotlin.test.assertEquals

class HealthRoutesTest {

    @Test
    fun `GET health returns 200 and ok status`() = testApplication {
        environment { config = ApplicationConfig("application-test.conf") }

        val client = createClient {
            install(ContentNegotiation) { json() }
        }

        val response = client.get("/health")
        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.body<HealthResponse>()
        assertEquals("ok", body.status)
    }
}
