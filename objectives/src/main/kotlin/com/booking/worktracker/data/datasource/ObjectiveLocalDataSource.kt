package com.booking.worktracker.data.datasource

import com.booking.worktracker.data.Database
import com.booking.worktracker.data.models.ChecklistItem
import com.booking.worktracker.data.models.Objective
import com.booking.worktracker.data.models.ObjectiveStatus
import com.booking.worktracker.data.models.ObjectiveType

class ObjectiveLocalDataSource {

    fun getYearlyObjectives(year: Int): List<Objective> {
        return getObjectives("SELECT id, title, description, type, year, quarter, status, created_at, updated_at FROM objectives WHERE type = 'YEARLY' AND year = ? ORDER BY created_at DESC", year)
    }

    fun getQuarterlyObjectives(year: Int, quarter: Int): List<Objective> {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(
            "SELECT id, title, description, type, year, quarter, status, created_at, updated_at FROM objectives WHERE type = 'QUARTERLY' AND year = ? AND quarter = ? ORDER BY created_at DESC"
        )
        stmt.setInt(1, year)
        stmt.setInt(2, quarter)
        val rs = stmt.executeQuery()

        val objectives = mutableListOf<Objective>()
        while (rs.next()) {
            val objId = rs.getInt("id")
            objectives.add(
                Objective(
                    id = objId,
                    title = rs.getString("title"),
                    description = rs.getString("description"),
                    type = ObjectiveType.valueOf(rs.getString("type")),
                    year = rs.getInt("year"),
                    quarter = rs.getObject("quarter") as? Int,
                    status = ObjectiveStatus.valueOf(rs.getString("status")),
                    checklistItems = getChecklistItems(objId),
                    createdAt = rs.getString("created_at"),
                    updatedAt = rs.getString("updated_at")
                )
            )
        }
        rs.close()
        stmt.close()
        return objectives
    }

    fun createObjective(
        title: String,
        description: String,
        type: ObjectiveType,
        year: Int,
        quarter: Int? = null
    ): Objective {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(
            "INSERT INTO objectives (title, description, type, year, quarter) VALUES (?, ?, ?, ?, ?)"
        )
        stmt.setString(1, title)
        stmt.setString(2, description)
        stmt.setString(3, type.name)
        stmt.setInt(4, year)
        if (quarter != null) {
            stmt.setInt(5, quarter)
        } else {
            stmt.setNull(5, java.sql.Types.INTEGER)
        }
        stmt.executeUpdate()
        stmt.close()

        val idRs = conn.createStatement().executeQuery("SELECT last_insert_rowid()")
        idRs.next()
        val objId = idRs.getInt(1)
        idRs.close()

        return getObjectiveById(objId)!!
    }

    fun updateObjective(
        id: Int,
        title: String,
        description: String,
        status: ObjectiveStatus
    ): Objective {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(
            "UPDATE objectives SET title = ?, description = ?, status = ?, updated_at = datetime('now') WHERE id = ?"
        )
        stmt.setString(1, title)
        stmt.setString(2, description)
        stmt.setString(3, status.name)
        stmt.setInt(4, id)
        stmt.executeUpdate()
        stmt.close()

        return getObjectiveById(id)!!
    }

    fun deleteObjective(id: Int) {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement("DELETE FROM objectives WHERE id = ?")
        stmt.setInt(1, id)
        stmt.executeUpdate()
        stmt.close()
    }

    fun addChecklistItem(objectiveId: Int, text: String): ChecklistItem {
        val conn = Database.getConnection()

        val posStmt = conn.prepareStatement("SELECT COALESCE(MAX(position), -1) + 1 as next_pos FROM checklist_items WHERE objective_id = ?")
        posStmt.setInt(1, objectiveId)
        val posRs = posStmt.executeQuery()
        posRs.next()
        val nextPos = posRs.getInt("next_pos")
        posRs.close()
        posStmt.close()

        val stmt = conn.prepareStatement(
            "INSERT INTO checklist_items (objective_id, text, position) VALUES (?, ?, ?)"
        )
        stmt.setInt(1, objectiveId)
        stmt.setString(2, text)
        stmt.setInt(3, nextPos)
        stmt.executeUpdate()
        stmt.close()

        val idRs = conn.createStatement().executeQuery("SELECT last_insert_rowid()")
        idRs.next()
        val itemId = idRs.getInt(1)
        idRs.close()

        val readStmt = conn.prepareStatement("SELECT id, objective_id, text, completed, position, created_at FROM checklist_items WHERE id = ?")
        readStmt.setInt(1, itemId)
        val rs = readStmt.executeQuery()
        rs.next()
        val item = ChecklistItem(
            id = rs.getInt("id"),
            objectiveId = rs.getInt("objective_id"),
            text = rs.getString("text"),
            completed = rs.getInt("completed") == 1,
            position = rs.getInt("position"),
            createdAt = rs.getString("created_at")
        )
        rs.close()
        readStmt.close()
        return item
    }

    fun toggleChecklistItem(itemId: Int): ChecklistItem {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(
            "UPDATE checklist_items SET completed = CASE WHEN completed = 0 THEN 1 ELSE 0 END WHERE id = ?"
        )
        stmt.setInt(1, itemId)
        stmt.executeUpdate()
        stmt.close()

        val readStmt = conn.prepareStatement("SELECT id, objective_id, text, completed, position, created_at FROM checklist_items WHERE id = ?")
        readStmt.setInt(1, itemId)
        val rs = readStmt.executeQuery()
        rs.next()
        val item = ChecklistItem(
            id = rs.getInt("id"),
            objectiveId = rs.getInt("objective_id"),
            text = rs.getString("text"),
            completed = rs.getInt("completed") == 1,
            position = rs.getInt("position"),
            createdAt = rs.getString("created_at")
        )
        rs.close()
        readStmt.close()
        return item
    }

    fun deleteChecklistItem(itemId: Int) {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement("DELETE FROM checklist_items WHERE id = ?")
        stmt.setInt(1, itemId)
        stmt.executeUpdate()
        stmt.close()
    }

    private fun getObjectives(sql: String, year: Int): List<Objective> {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(sql)
        stmt.setInt(1, year)
        val rs = stmt.executeQuery()

        val objectives = mutableListOf<Objective>()
        while (rs.next()) {
            val objId = rs.getInt("id")
            objectives.add(
                Objective(
                    id = objId,
                    title = rs.getString("title"),
                    description = rs.getString("description"),
                    type = ObjectiveType.valueOf(rs.getString("type")),
                    year = rs.getInt("year"),
                    quarter = rs.getObject("quarter") as? Int,
                    status = ObjectiveStatus.valueOf(rs.getString("status")),
                    checklistItems = getChecklistItems(objId),
                    createdAt = rs.getString("created_at"),
                    updatedAt = rs.getString("updated_at")
                )
            )
        }
        rs.close()
        stmt.close()
        return objectives
    }

    private fun getObjectiveById(id: Int): Objective? {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(
            "SELECT id, title, description, type, year, quarter, status, created_at, updated_at FROM objectives WHERE id = ?"
        )
        stmt.setInt(1, id)
        val rs = stmt.executeQuery()

        if (!rs.next()) {
            rs.close()
            stmt.close()
            return null
        }

        val objective = Objective(
            id = rs.getInt("id"),
            title = rs.getString("title"),
            description = rs.getString("description"),
            type = ObjectiveType.valueOf(rs.getString("type")),
            year = rs.getInt("year"),
            quarter = rs.getObject("quarter") as? Int,
            status = ObjectiveStatus.valueOf(rs.getString("status")),
            checklistItems = getChecklistItems(id),
            createdAt = rs.getString("created_at"),
            updatedAt = rs.getString("updated_at")
        )
        rs.close()
        stmt.close()
        return objective
    }

    private fun getChecklistItems(objectiveId: Int): List<ChecklistItem> {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(
            "SELECT id, objective_id, text, completed, position, created_at FROM checklist_items WHERE objective_id = ? ORDER BY position ASC"
        )
        stmt.setInt(1, objectiveId)
        val rs = stmt.executeQuery()

        val items = mutableListOf<ChecklistItem>()
        while (rs.next()) {
            items.add(
                ChecklistItem(
                    id = rs.getInt("id"),
                    objectiveId = rs.getInt("objective_id"),
                    text = rs.getString("text"),
                    completed = rs.getInt("completed") == 1,
                    position = rs.getInt("position"),
                    createdAt = rs.getString("created_at")
                )
            )
        }
        rs.close()
        stmt.close()
        return items
    }
}
