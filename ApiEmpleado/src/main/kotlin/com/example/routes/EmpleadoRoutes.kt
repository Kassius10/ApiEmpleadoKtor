package com.example.routes

import com.example.dto.EmpleadoCreateDto
import com.example.dto.EmpleadoUpdateDto
import com.example.exceptions.EmpleadoException
import com.example.exceptions.EmpleadoNotFoundException
import com.example.mappers.toEmpleado
import com.example.models.Empleado
import com.example.repositories.EmpleadoRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.util.pipeline.*
import io.ktor.websocket.*
import org.koin.ktor.ext.inject

private const val ENDPOINT = "/empleados"

fun Application.empleadoRoutes() {
    val repository: EmpleadoRepository by inject()

    routing {
        route(ENDPOINT) {
            get {
                val empleados = mutableListOf<Empleado>()

                repository.findAll().collect {
                    empleados.add(it)
                }
                call.respond(HttpStatusCode.OK, empleados)
            }
            get("/{id}") {
                try {
                    val id = call.parameters["id"]!!.toLong()
                    val result = repository.findById(id)
                    result?.let {
                        call.respond(HttpStatusCode.OK, it)
                    } ?: call.respond(HttpStatusCode.NotFound, "No existe el empleado con id: $id")
                } catch (e: NumberFormatException) {
                    call.respond(HttpStatusCode.BadRequest, e.message.toString())
                }
            }
            authenticate {
                post {
                    try {
                        validateRol()
                        val empleado = call.receive<EmpleadoCreateDto>()
                        val res = repository.save(empleado.toEmpleado())
                        call.respond(HttpStatusCode.Created, res)
                    } catch (e: EmpleadoException) {
                        call.respond(HttpStatusCode.BadRequest, e.message.toString())
                    } catch (e: RequestValidationException) {
                        call.respond(HttpStatusCode.BadRequest, e.reasons)
                    }
                }
                delete("/{id}") {
                    try {
                        validateRol()
                        val id = call.parameters["id"]!!.toLong()
                        val result = repository.findById(id)
                        result?.let {
                            repository.delete(it)
                            call.respond(HttpStatusCode.NoContent)
                        } ?: call.respond(HttpStatusCode.NotFound, "No existe el empleado con id: $id")
                    } catch (e: NumberFormatException) {
                        call.respond(HttpStatusCode.BadRequest, e.message.toString())
                    }
                }
                put("/{id}") {
                    try {
                        validateRol()
                        val id = call.parameters["id"]!!.toLong()
                        val empleado = call.receive<EmpleadoUpdateDto>()
                        val result = repository.update(id, empleado.toEmpleado())
                        call.respond(HttpStatusCode.OK, result)
                    } catch (e: NumberFormatException) {
                        call.respond(HttpStatusCode.BadRequest, e.message.toString())
                    } catch (e: EmpleadoNotFoundException) {
                        call.respond(HttpStatusCode.NotFound, e.message.toString())
                    } catch (e: RequestValidationException) {
                        call.respond(HttpStatusCode.BadRequest, e.reasons)
                    }
                }
            }
        }

        webSocket("/updates/empleados") {
            try {
                repository.addSuscriptor(this.hashCode()) {
                    sendSerialized(it)
                }
                sendSerialized("Updates Web Socket: Empleado")
                for (frame in incoming) {
                    if (frame.frameType == FrameType.CLOSE) {
                        break
                    } else if (frame is Frame.Text) {
                        println("Mensaje recibido: ${frame.readText()}")
                    }
                }
            } finally {
                repository.removeSuscriptor(this.hashCode())
            }
        }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.validateRol() {
    val jwt = call.principal<JWTPrincipal>()
    val rol = jwt?.payload?.getClaim("rol").toString().replace("\"", "")
    if (rol != "ADMIN") {
        call.respond(HttpStatusCode.Forbidden, "No tienes permisos para realizar un post.")
    }
}