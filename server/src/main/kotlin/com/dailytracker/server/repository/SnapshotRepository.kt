package com.dailytracker.server.repository

import com.dailytracker.server.models.SnapshotInfo
import com.dailytracker.server.models.Snapshots
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

data class SnapshotRecord(
    val id: UUID,
    val userId: UUID,
    val name: String,
    val filePath: String,
    val fileSize: Long,
    val schemaVersion: Int?,
    val createdAt: OffsetDateTime
)

class SnapshotRepository {

    fun create(
        userId: UUID,
        name: String,
        filePath: String,
        fileSize: Long,
        schemaVersion: Int?
    ): SnapshotRecord = transaction {
        val now = OffsetDateTime.now()
        val newId = UUID.randomUUID()
        Snapshots.insert {
            it[id] = newId
            it[Snapshots.userId] = userId
            it[Snapshots.name] = name
            it[Snapshots.filePath] = filePath
            it[Snapshots.fileSize] = fileSize
            it[Snapshots.schemaVersion] = schemaVersion
            it[Snapshots.createdAt] = now
        }
        SnapshotRecord(newId, userId, name, filePath, fileSize, schemaVersion, now)
    }

    fun listByUser(userId: UUID): List<SnapshotInfo> = transaction {
        Snapshots.selectAll().where { Snapshots.userId eq userId }
            .orderBy(Snapshots.createdAt)
            .map { it.toSnapshotInfo() }
    }

    fun findById(snapshotId: UUID, userId: UUID): SnapshotRecord? = transaction {
        Snapshots.selectAll()
            .where { (Snapshots.id eq snapshotId) and (Snapshots.userId eq userId) }
            .map { it.toSnapshotRecord() }
            .singleOrNull()
    }

    fun delete(snapshotId: UUID, userId: UUID): Boolean = transaction {
        Snapshots.deleteWhere { (id eq snapshotId) and (Snapshots.userId eq userId) } > 0
    }

    private fun ResultRow.toSnapshotInfo() = SnapshotInfo(
        id = this[Snapshots.id].toString(),
        name = this[Snapshots.name],
        fileSize = this[Snapshots.fileSize],
        schemaVersion = this[Snapshots.schemaVersion],
        createdAt = this[Snapshots.createdAt].format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    )

    private fun ResultRow.toSnapshotRecord() = SnapshotRecord(
        id = this[Snapshots.id],
        userId = this[Snapshots.userId],
        name = this[Snapshots.name],
        filePath = this[Snapshots.filePath],
        fileSize = this[Snapshots.fileSize],
        schemaVersion = this[Snapshots.schemaVersion],
        createdAt = this[Snapshots.createdAt]
    )
}
