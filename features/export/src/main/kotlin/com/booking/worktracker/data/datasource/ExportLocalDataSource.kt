package com.booking.worktracker.data.datasource

import com.booking.worktracker.data.Database
import com.booking.worktracker.data.models.ExportEntry
import com.booking.worktracker.data.models.ExportObjective

class ExportLocalDataSource {

    fun getEntriesForRange(startDate: String, endDate: String): List<ExportEntry> {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(
            """
            SELECT dl.date, we.content, we.created_at,
                   GROUP_CONCAT(t.name) as tag_names
            FROM daily_logs dl
            INNER JOIN work_entries we ON dl.id = we.daily_log_id
            LEFT JOIN log_tags lt ON dl.id = lt.daily_log_id
            LEFT JOIN tags t ON lt.tag_id = t.id
            WHERE dl.date BETWEEN ? AND ?
            GROUP BY we.id
            ORDER BY dl.date ASC, we.created_at ASC
            """
        )
        stmt.setString(1, startDate)
        stmt.setString(2, endDate)
        val rs = stmt.executeQuery()

        val entries = mutableListOf<ExportEntry>()
        while (rs.next()) {
            val tagNames = rs.getString("tag_names")
            entries.add(
                ExportEntry(
                    date = rs.getString("date"),
                    content = rs.getString("content"),
                    tags = if (tagNames != null) tagNames.split(",").distinct() else emptyList(),
                    createdAt = rs.getString("created_at")
                )
            )
        }
        rs.close()
        stmt.close()
        return entries
    }

    fun getObjectives(): List<ExportObjective> {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(
            """
            SELECT o.title, o.description, o.type, o.status,
                   GROUP_CONCAT(ci.text, '||') as items,
                   SUM(CASE WHEN ci.completed = 1 THEN 1 ELSE 0 END) as completed_count,
                   COUNT(ci.id) as total_count
            FROM objectives o
            LEFT JOIN checklist_items ci ON o.id = ci.objective_id
            GROUP BY o.id
            ORDER BY o.created_at DESC
            """
        )
        val rs = stmt.executeQuery()

        val objectives = mutableListOf<ExportObjective>()
        while (rs.next()) {
            val items = rs.getString("items")
            objectives.add(
                ExportObjective(
                    title = rs.getString("title"),
                    description = rs.getString("description"),
                    type = rs.getString("type"),
                    status = rs.getString("status"),
                    checklistItems = if (items != null) items.split("||") else emptyList(),
                    completedItems = rs.getInt("completed_count"),
                    totalItems = rs.getInt("total_count")
                )
            )
        }
        rs.close()
        stmt.close()
        return objectives
    }

    fun getDaysInRange(startDate: String, endDate: String): Int {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(
            """
            SELECT COUNT(DISTINCT dl.date) as day_count
            FROM daily_logs dl
            INNER JOIN work_entries we ON dl.id = we.daily_log_id
            WHERE dl.date BETWEEN ? AND ?
            """
        )
        stmt.setString(1, startDate)
        stmt.setString(2, endDate)
        val rs = stmt.executeQuery()
        rs.next()
        val count = rs.getInt("day_count")
        rs.close()
        stmt.close()
        return count
    }
}
