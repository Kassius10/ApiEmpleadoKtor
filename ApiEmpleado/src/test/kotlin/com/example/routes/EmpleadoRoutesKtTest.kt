package com.example.routes

import com.example.dto.EmpleadoCreateDto
import com.example.dto.EmpleadoUpdateDto
import com.example.dto.UserLoginDto
import com.example.models.Empleado
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import java.util.*

private val json = Json { ignoreUnknownKeys = true }

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class EmpleadoRoutesKtTest {
    private val empleado = Empleado(
        100,
        "044e6ec7-aa6c-46bb-9433-8094ef4ae8bc",
        "empleado",
        true
    )
    private val create = EmpleadoCreateDto(
        empleado.id!!,
        empleado.name
    )
    private val update = EmpleadoUpdateDto(
        name = "nuevo",
        available = false
    )
    private val loginDto = UserLoginDto(
        username = "pepito123",
        password = "1234",
    )

    @org.junit.Test
    @Order(1)
    fun getAll() = testApplication {
        environment { config }
        val response = client.get("/empleados")

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @org.junit.Test
    @Order(2)
    fun getIdNotFound() = testApplication {
        environment { config }
        val response = client.get("/empleados/1")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @org.junit.Test
    @Order(3)
    fun post() = testApplication {
        environment { config }
        var client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        var response = client.post("/login") {
            contentType(ContentType.Application.Json)
            setBody(loginDto)
        }
        val token = response.bodyAsText()
        client = createClient {
            install(ContentNegotiation) {
                json()
            }
            install(Auth) {
                bearer {
                    loadTokens {
                        BearerTokens(token, token)
                    }
                }
            }
        }
        response = client.post("/empleados") {
            contentType(ContentType.Application.Json)
            setBody(create)
        }
        assertEquals(response.status, HttpStatusCode.Created)
        val res = json.decodeFromString<Empleado>(response.bodyAsText())

        assertAll(
            { assertEquals(empleado.name, res.name) },
            { assertEquals(empleado.available, res.available) }
        )
    }

    @org.junit.Test
    @Order(4)
    fun put() = testApplication {
        environment { config }
        var client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        var response = client.post("/login") {
            contentType(ContentType.Application.Json)
            setBody(loginDto)
        }
        val token = response.bodyAsText()
        client = createClient {
            install(ContentNegotiation) {
                json()
            }
            install(Auth) {
                bearer {
                    loadTokens {
                        BearerTokens(token, token)
                    }
                }
            }
        }
        response = client.post("/empleados") {
            contentType(ContentType.Application.Json)
            setBody(create)
        }
        val res = json.decodeFromString<Empleado>(response.bodyAsText())

        response = client.put("/empleados/${res.id}") {
            contentType(ContentType.Application.Json)
            setBody(update)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val result = response.bodyAsText()

        val dto = json.decodeFromString<Empleado>(result)
        assertAll(
            { assertEquals(update.name, dto.name) },
            { assertEquals(update.available, dto.available) }
        )

    }

    @org.junit.Test
    @Order(5)
    fun putNotFound() = testApplication {
        environment { config }
        var client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        var response = client.post("/login") {
            contentType(ContentType.Application.Json)
            setBody(loginDto)
        }
        val token = response.bodyAsText()
        client = createClient {
            install(ContentNegotiation) {
                json()
            }
            install(Auth) {
                bearer {
                    loadTokens {
                        BearerTokens(token, token)
                    }
                }
            }
        }
        response = client.put("/empleados/-1") {
            contentType(ContentType.Application.Json)
            setBody(update)
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @org.junit.Test
    @Order(6)
    fun delete() = testApplication {
        environment { config }
        var client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        var response = client.post("/login") {
            contentType(ContentType.Application.Json)
            setBody(loginDto)
        }
        val token = response.bodyAsText()
        client = createClient {
            install(ContentNegotiation) {
                json()
            }
            install(Auth) {
                bearer {
                    loadTokens {
                        BearerTokens(token, token)
                    }
                }
            }
        }
        response = client.post("/empleados") {
            contentType(ContentType.Application.Json)
            setBody(create)
        }
        val res = json.decodeFromString<Empleado>(response.bodyAsText())

        response = client.delete("/empleados/${res.id}")

        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @org.junit.Test
    @Order(7)
    fun deleteNotFound() = testApplication {
        environment { config }
        var client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        var response = client.post("/login") {
            contentType(ContentType.Application.Json)
            setBody(loginDto)
        }
        val token = response.bodyAsText()
        client = createClient {
            install(ContentNegotiation) {
                json()
            }
            install(Auth) {
                bearer {
                    loadTokens {
                        BearerTokens(token, token)
                    }
                }
            }
        }
        response = client.delete("/empleados/-1")

        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}