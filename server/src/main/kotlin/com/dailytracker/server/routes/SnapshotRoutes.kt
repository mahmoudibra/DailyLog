package com.dailytracker.server.routes

import com.dailytracker.server.models.ErrorResponse
import com.dailytracker.server.models.SnapshotListResponse
import com.dailytracker.server.repository.SnapshotRepository
import com.dailytracker.server.storage.FileStorageService
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.snapshotRoutes(
    snapshotRepository: SnapshotRepository,
    fileStorageService: FileStorageService
) {
    authenticate("auth-jwt") {
        route("/snapshots") {
            post {
                val userId = call.userId()
                val multipart = call.receiveMultipart()

                var name: String? = null
                var schemaVersion: Int? = null
                var filePath: String? = null
                var fileSize: Long = 0

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            when (part.name) {
                                "name" -> name = part.value
                                "schemaVersion" -> schemaVersion = part.value.toIntOrNull()
                            }
                        }
                        is PartData.FileItem -> {
                            @Suppress("DEPRECATION")
                            val result = fileStorageService.save(
                                userId,
                                part.originalFileName ?: "snapshot.db",
                                part.streamProvider()
                            )
                            filePath = result.first
                            fileSize = result.second
                        }
                        else -> {}
                    }
                    part.dispose()
                }

                if (filePath == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("No file uploaded"))
                    return@post
                }

                val snapshotName = name ?: "Snapshot ${java.time.LocalDateTime.now()}"
                val snapshot = snapshotRepository.create(
                    userId = userId,
                    name = snapshotName,
                    filePath = filePath!!,
                    fileSize = fileSize,
                    schemaVersion = schemaVersion
                )

                call.respond(HttpStatusCode.Created, mapOf(
                    "id" to snapshot.id.toString(),
                    "name" to snapshot.name,
                    "fileSize" to snapshot.fileSize,
                    "createdAt" to snapshot.createdAt.toString()
                ))
            }

            get {
                val userId = call.userId()
                val snapshots = snapshotRepository.listByUser(userId)
                call.respond(SnapshotListResponse(snapshots))
            }

            get("/{id}/download") {
                val userId = call.userId()
                val snapshotId = call.parameters["id"]?.let { UUID.fromString(it) }
                    ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid snapshot ID"))

                val snapshot = snapshotRepository.findById(snapshotId, userId)
                    ?: return@get call.respond(HttpStatusCode.NotFound, ErrorResponse("Snapshot not found"))

                val file = fileStorageService.get(snapshot.filePath)
                    ?: return@get call.respond(HttpStatusCode.NotFound, ErrorResponse("Snapshot file not found"))

                call.response.header(
                    HttpHeaders.ContentDisposition,
                    ContentDisposition.Attachment.withParameter(
                        ContentDisposition.Parameters.FileName, "${snapshot.name}.db"
                    ).toString()
                )
                call.respondFile(file)
            }

            delete("/{id}") {
                val userId = call.userId()
                val snapshotId = call.parameters["id"]?.let { UUID.fromString(it) }
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid snapshot ID"))

                val snapshot = snapshotRepository.findById(snapshotId, userId)
                    ?: return@delete call.respond(HttpStatusCode.NotFound, ErrorResponse("Snapshot not found"))

                fileStorageService.delete(snapshot.filePath)
                snapshotRepository.delete(snapshotId, userId)

                call.respond(HttpStatusCode.OK, mapOf("message" to "Snapshot deleted"))
            }
        }
    }
}

private fun RoutingCall.userId(): UUID {
    val principal = principal<JWTPrincipal>()!!
    return UUID.fromString(principal.payload.getClaim("userId").asString())
}
