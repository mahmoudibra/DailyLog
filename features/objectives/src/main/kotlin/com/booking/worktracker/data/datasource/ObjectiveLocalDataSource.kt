package com.booking.worktracker.data.datasource

import com.booking.worktracker.data.DatabaseProvider
import com.booking.worktracker.data.DailyWorkTrackerDatabase
import com.booking.worktracker.data.models.ChecklistItem
import com.booking.worktracker.data.models.Objective
import com.booking.worktracker.data.models.ObjectiveStatus
import com.booking.worktracker.data.models.ObjectiveType

class ObjectiveLocalDataSource(db: DailyWorkTrackerDatabase = DatabaseProvider.getDatabase()) {

    private val objectivesQueries = db.objectivesQueries
    private val dailyLogsQueries = db.dailyLogsQueries

    fun getYearlyObjectives(year: Int): List<Objective> {
        return objectivesQueries.getYearlyObjectives(year.toLong()).executeAsList().map { row ->
            Objective(
                id = row.id.toInt(),
                title = row.title,
                description = row.description,
                type = ObjectiveType.valueOf(row.type),
                year = row.year.toInt(),
                quarter = row.quarter?.toInt(),
                status = ObjectiveStatus.valueOf(row.status),
                checklistItems = getChecklistItems(row.id.toInt()),
                createdAt = row.created_at,
                updatedAt = row.updated_at
            )
        }
    }

    fun getQuarterlyObjectives(year: Int, quarter: Int): List<Objective> {
        return objectivesQueries.getQuarterlyObjectives(year.toLong(), quarter.toLong()).executeAsList().map { row ->
            Objective(
                id = row.id.toInt(),
                title = row.title,
                description = row.description,
                type = ObjectiveType.valueOf(row.type),
                year = row.year.toInt(),
                quarter = row.quarter?.toInt(),
                status = ObjectiveStatus.valueOf(row.status),
                checklistItems = getChecklistItems(row.id.toInt()),
                createdAt = row.created_at,
                updatedAt = row.updated_at
            )
        }
    }

    fun createObjective(
        title: String,
        description: String,
        type: ObjectiveType,
        year: Int,
        quarter: Int? = null
    ): Objective {
        val objId = objectivesQueries.transactionWithResult {
            objectivesQueries.insertObjective(title, description, type.name, year.toLong(), quarter?.toLong())
            dailyLogsQueries.lastInsertRowId().executeAsOne()
        }
        return getObjectiveById(objId.toInt())!!
    }

    fun updateObjective(
        id: Int,
        title: String,
        description: String,
        status: ObjectiveStatus
    ): Objective {
        objectivesQueries.updateObjective(title, description, status.name, id.toLong())
        return getObjectiveById(id)!!
    }

    fun deleteObjective(id: Int) {
        objectivesQueries.deleteObjective(id.toLong())
    }

    fun addChecklistItem(objectiveId: Int, text: String): ChecklistItem {
        val nextPos = objectivesQueries.getNextChecklistPosition(objectiveId.toLong()).executeAsOne()
        return objectivesQueries.transactionWithResult {
            objectivesQueries.insertChecklistItem(objectiveId.toLong(), text, nextPos)
            val itemId = dailyLogsQueries.lastInsertRowId().executeAsOne()
            val row = objectivesQueries.getChecklistItemById(itemId).executeAsOne()
            ChecklistItem(
                id = row.id.toInt(),
                objectiveId = row.objective_id.toInt(),
                text = row.text,
                completed = row.completed == 1L,
                position = row.position.toInt(),
                createdAt = row.created_at
            )
        }
    }

    fun toggleChecklistItem(itemId: Int): ChecklistItem {
        objectivesQueries.toggleChecklistItem(itemId.toLong())
        val row = objectivesQueries.getChecklistItemById(itemId.toLong()).executeAsOne()
        return ChecklistItem(
            id = row.id.toInt(),
            objectiveId = row.objective_id.toInt(),
            text = row.text,
            completed = row.completed == 1L,
            position = row.position.toInt(),
            createdAt = row.created_at
        )
    }

    fun deleteChecklistItem(itemId: Int) {
        objectivesQueries.deleteChecklistItem(itemId.toLong())
    }

    private fun getObjectiveById(id: Int): Objective? {
        val row = objectivesQueries.getObjectiveById(id.toLong()).executeAsOneOrNull() ?: return null
        return Objective(
            id = row.id.toInt(),
            title = row.title,
            description = row.description,
            type = ObjectiveType.valueOf(row.type),
            year = row.year.toInt(),
            quarter = row.quarter?.toInt(),
            status = ObjectiveStatus.valueOf(row.status),
            checklistItems = getChecklistItems(id),
            createdAt = row.created_at,
            updatedAt = row.updated_at
        )
    }

    private fun getChecklistItems(objectiveId: Int): List<ChecklistItem> {
        return objectivesQueries.getChecklistItems(objectiveId.toLong()).executeAsList().map { row ->
            ChecklistItem(
                id = row.id.toInt(),
                objectiveId = row.objective_id.toInt(),
                text = row.text,
                completed = row.completed == 1L,
                position = row.position.toInt(),
                createdAt = row.created_at
            )
        }
    }
}
