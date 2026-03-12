package com.booking.worktracker.data

import java.sql.Connection
import java.sql.DriverManager

object Database {
    private const val DB_URL = "jdbc:sqlite:daily_work_tracker.db"
    private var connection: Connection? = null

    fun init() {
        Class.forName("org.sqlite.JDBC")
        connection = DriverManager.getConnection(DB_URL)
        connection?.let { conn ->
            conn.createStatement().execute("PRAGMA foreign_keys = ON")
            createTables(conn)
        }
    }

    fun getConnection(): Connection {
        return connection ?: throw IllegalStateException("Database not initialized. Call init() first.")
    }

    fun close() {
        connection?.close()
        connection = null
    }

    fun deleteAllData() {
        val conn = getConnection()
        val tables = listOf(
            "checklist_items", "work_entries", "log_tags",
            "time_entries", "objectives", "daily_logs", "tags", "settings"
        )
        conn.createStatement().execute("PRAGMA foreign_keys = OFF")
        tables.forEach { table ->
            try { conn.createStatement().execute("DELETE FROM $table") } catch (_: Exception) {}
        }
        conn.createStatement().execute("PRAGMA foreign_keys = ON")
    }

    private fun createTables(conn: Connection) {
        val schema = Database::class.java.classLoader
            .getResourceAsStream("database/schema.sql")
            ?.bufferedReader()
            ?.readText()
            ?: throw IllegalStateException("Could not load schema.sql")

        // Split by semicolons and execute each statement
        schema.split(";")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { statement ->
                // Remove SQL comments before executing
                statement.lines()
                    .filter { !it.trimStart().startsWith("--") }
                    .joinToString("\n")
                    .trim()
            }
            .filter { it.isNotEmpty() }
            .forEach { statement ->
                conn.createStatement().execute(statement)
            }
    }
}
