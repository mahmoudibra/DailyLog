package com.booking.worktracker.data.datasource

import com.booking.worktracker.data.Database
import com.booking.worktracker.data.models.PeriodType
import com.booking.worktracker.data.models.TimeBudget
import kotlinx.datetime.LocalDate

class TimeBudgetLocalDataSource {

    fun getAll(): List<TimeBudget> {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(
            "SELECT id, category, target_minutes, period_type, objective_id, created_at, updated_at FROM time_budgets ORDER BY category"
        )
        val rs = stmt.executeQuery()
        val budgets = mutableListOf<TimeBudget>()
        while (rs.next()) {
            budgets.add(mapBudget(rs))
        }
        rs.close()
        stmt.close()
        return budgets
    }

    fun getById(id: Int): TimeBudget? {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(
            "SELECT id, category, target_minutes, period_type, objective_id, created_at, updated_at FROM time_budgets WHERE id = ?"
        )
        stmt.setInt(1, id)
        val rs = stmt.executeQuery()
        val budget = if (rs.next()) mapBudget(rs) else null
        rs.close()
        stmt.close()
        return budget
    }

    fun create(category: String, targetMinutes: Int, periodType: PeriodType, objectiveId: Int?): TimeBudget {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(
            "INSERT INTO time_budgets (category, target_minutes, period_type, objective_id) VALUES (?, ?, ?, ?)"
        )
        stmt.setString(1, category)
        stmt.setInt(2, targetMinutes)
        stmt.setString(3, periodType.name)
        if (objectiveId != null) stmt.setInt(4, objectiveId) else stmt.setNull(4, java.sql.Types.INTEGER)
        stmt.executeUpdate()
        stmt.close()

        val idRs = conn.createStatement().executeQuery("SELECT last_insert_rowid()")
        idRs.next()
        val id = idRs.getInt(1)
        idRs.close()

        return getById(id)!!
    }

    fun update(id: Int, category: String, targetMinutes: Int, periodType: PeriodType, objectiveId: Int?): TimeBudget {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(
            "UPDATE time_budgets SET category = ?, target_minutes = ?, period_type = ?, objective_id = ?, updated_at = datetime('now') WHERE id = ?"
        )
        stmt.setString(1, category)
        stmt.setInt(2, targetMinutes)
        stmt.setString(3, periodType.name)
        if (objectiveId != null) stmt.setInt(4, objectiveId) else stmt.setNull(4, java.sql.Types.INTEGER)
        stmt.setInt(5, id)
        stmt.executeUpdate()
        stmt.close()

        return getById(id)!!
    }

    fun delete(id: Int) {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement("DELETE FROM time_budgets WHERE id = ?")
        stmt.setInt(1, id)
        stmt.executeUpdate()
        stmt.close()
    }

    fun getActualMinutesForCategory(category: String, startDate: LocalDate, endDate: LocalDate): Int {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(
            "SELECT COALESCE(SUM(duration_minutes), 0) as total FROM time_entries WHERE category = ? AND date >= ? AND date <= ? AND end_time IS NOT NULL"
        )
        stmt.setString(1, category)
        stmt.setString(2, startDate.toString())
        stmt.setString(3, endDate.toString())
        val rs = stmt.executeQuery()
        rs.next()
        val total = rs.getInt("total")
        rs.close()
        stmt.close()
        return total
    }

    fun getObjectiveTitle(objectiveId: Int): String? {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement("SELECT title FROM objectives WHERE id = ?")
        stmt.setInt(1, objectiveId)
        val rs = stmt.executeQuery()
        val title = if (rs.next()) rs.getString("title") else null
        rs.close()
        stmt.close()
        return title
    }

    fun getActiveObjectives(): List<Pair<Int, String>> {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(
            "SELECT id, title FROM objectives WHERE status = 'IN_PROGRESS' ORDER BY title"
        )
        val rs = stmt.executeQuery()
        val objectives = mutableListOf<Pair<Int, String>>()
        while (rs.next()) {
            objectives.add(Pair(rs.getInt("id"), rs.getString("title")))
        }
        rs.close()
        stmt.close()
        return objectives
    }

    private fun mapBudget(rs: java.sql.ResultSet): TimeBudget {
        return TimeBudget(
            id = rs.getInt("id"),
            category = rs.getString("category"),
            targetMinutes = rs.getInt("target_minutes"),
            periodType = PeriodType.valueOf(rs.getString("period_type")),
            objectiveId = rs.getObject("objective_id") as? Int,
            createdAt = rs.getString("created_at"),
            updatedAt = rs.getString("updated_at")
        )
    }
}
