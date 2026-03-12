package com.booking.worktracker.data

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver

object DatabaseProvider {
    private var database: DailyWorkTrackerDatabase? = null
    private var driver: JdbcSqliteDriver? = null

    fun init() {
        val jdbcDriver = JdbcSqliteDriver("jdbc:sqlite:daily_work_tracker.db")

        // Check user_version via mapper-based executeQuery
        val userVersion = jdbcDriver.executeQuery(
            identifier = null,
            sql = "PRAGMA user_version",
            mapper = { cursor ->
                cursor.next()
                QueryResult.Value(cursor.getLong(0) ?: 0L)
            },
            parameters = 0
        ).value

        if (userVersion == 0L) {
            // Check if tables already exist (pre-SQLDelight database)
            val tablesExist = jdbcDriver.executeQuery(
                identifier = null,
                sql = "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='daily_logs'",
                mapper = { cursor ->
                    cursor.next()
                    QueryResult.Value((cursor.getLong(0) ?: 0L) > 0)
                },
                parameters = 0
            ).value

            if (tablesExist) {
                // Existing database — set user_version to current schema version
                // so SQLDelight doesn't try to recreate tables
                jdbcDriver.execute(
                    null,
                    "PRAGMA user_version = ${DailyWorkTrackerDatabase.Schema.version}",
                    0
                )
            } else {
                // Fresh database — create schema
                DailyWorkTrackerDatabase.Schema.create(jdbcDriver)
            }
        } else {
            // Run any pending migrations
            if (userVersion < DailyWorkTrackerDatabase.Schema.version) {
                DailyWorkTrackerDatabase.Schema.migrate(
                    driver = jdbcDriver,
                    oldVersion = userVersion,
                    newVersion = DailyWorkTrackerDatabase.Schema.version
                )
            }
        }

        // Enable foreign keys
        jdbcDriver.execute(null, "PRAGMA foreign_keys = ON", 0)

        driver = jdbcDriver
        database = DailyWorkTrackerDatabase(jdbcDriver)
    }

    fun getDatabase(): DailyWorkTrackerDatabase {
        return database ?: throw IllegalStateException("Database not initialized. Call init() first.")
    }

    fun close() {
        driver?.close()
        driver = null
        database = null
    }

    fun deleteAllData() {
        val drv = driver ?: return
        drv.execute(null, "PRAGMA foreign_keys = OFF", 0)
        val tables = listOf(
            "checklist_items", "work_entries", "log_tags",
            "time_entries", "objectives", "daily_logs", "tags", "settings",
            "daily_reviews", "weekly_summaries", "time_budgets"
        )
        tables.forEach { table ->
            try { drv.execute(null, "DELETE FROM $table", 0) } catch (_: Exception) {}
        }
        drv.execute(null, "PRAGMA foreign_keys = ON", 0)
    }
}
