package com.example.routes

import com.example.services.storage.StorageService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.time.LocalDateTime
import java.util.*

fun Application.storageRoutes() {
    val storageService: StorageService by inject()

    routing {
        route("/storage") {
            get("/check") {
                call.respond(
                    HttpStatusCode.OK,
                    mapOf(
                        "status" to "OK",
                        "message" to "Storage API REST Ktor. 2º DAM",
                        "createdAt" to LocalDateTime.now().toString()
                    )
                )
            }
            post {
                val readChannel = call.receiveChannel()
                val fileName = UUID.randomUUID().toString()
                val res = storageService.saveFile(fileName, readChannel)
                call.respond(HttpStatusCode.OK, res)
            }
            get("/{fileName}") {
                // Recuperamos el nombre del fichero
                val fileName = call.parameters["fileName"].toString()
                // Recuperamos el fichero
                val file = storageService.getFile(fileName)
                // De esta manera lo podria visiualizar el navegador
                // call.respondFile(file)
                // si lo hago así me pide descargar
                //call.response.header("Content-Disposition", "attachment; filename=\"${file.name}\"")
                // Para hacer lo anterior lo mejor es saber si tiene una extensión
                // y en función de eso devolver un tipo de contenido
                call.respondFile(file)

            }
        }
    }
}