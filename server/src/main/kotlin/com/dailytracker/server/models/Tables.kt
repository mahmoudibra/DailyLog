package com.dailytracker.server.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object Users : Table("users") {
    val id = uuid("id")
    val email = text("email").uniqueIndex()
    val passwordHash = text("password_hash")
    val createdAt = timestampWithTimeZone("created_at")

    override val primaryKey = PrimaryKey(id)
}

object Snapshots : Table("snapshots") {
    val id = uuid("id")
    val userId = uuid("user_id").references(Users.id)
    val name = text("name")
    val filePath = text("file_path")
    val fileSize = long("file_size")
    val schemaVersion = integer("schema_version").nullable()
    val createdAt = timestampWithTimeZone("created_at")

    override val primaryKey = PrimaryKey(id)
}
