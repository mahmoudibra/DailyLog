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
