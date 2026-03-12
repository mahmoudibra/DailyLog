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

        // Migration: Add focus_rating to time_entries if not exists
        try {
            val rs = connection!!.createStatement().executeQuery("PRAGMA table_info(time_entries)")
            var hasFocusRating = false
            while (rs.next()) {
                if (rs.getString("name") == "focus_rating") {
                    hasFocusRating = true
                    break
                }
            }
            rs.close()
            if (!hasFocusRating) {
                connection!!.createStatement().executeUpdate("ALTER TABLE time_entries ADD COLUMN focus_rating INTEGER")
            }
        } catch (e: Exception) {
            // Column might already exist
        }

        // Migration: Create time_budgets table if not exists
        try {
            connection!!.createStatement().execute(
                """
                CREATE TABLE IF NOT EXISTS time_budgets (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    category TEXT NOT NULL,
                    target_minutes INTEGER NOT NULL,
                    period_type TEXT NOT NULL CHECK (period_type IN ('WEEKLY', 'MONTHLY')),
                    objective_id INTEGER,
                    created_at TEXT NOT NULL DEFAULT (datetime('now')),
                    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
                    FOREIGN KEY (objective_id) REFERENCES objectives(id) ON DELETE SET NULL
                )
                """.trimIndent()
            )
        } catch (_: Exception) {
            // Table might already exist
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
            "time_entries", "objectives", "daily_logs", "tags", "settings",
            "daily_reviews", "weekly_summaries", "time_budgets"
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
