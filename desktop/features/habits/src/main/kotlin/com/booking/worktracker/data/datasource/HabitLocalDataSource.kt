package com.booking.worktracker.data.datasource

import com.booking.worktracker.data.DailyTrackerDatabase
import com.booking.worktracker.data.models.Habit
import com.booking.worktracker.data.models.HabitCompletion
import com.booking.worktracker.di.Singleton
import me.tatarka.inject.annotations.Inject

@Inject
@Singleton
class HabitLocalDataSource(db: DailyTrackerDatabase) {

    private val queries = db.habitsQueries

    fun getAllActiveHabits(): List<Habit> {
        return queries.getAllActiveHabits().executeAsList().map { row ->
            Habit(
                id = row.id.toInt(),
                name = row.name,
                icon = row.icon,
                color = row.color,
                objectiveId = row.objective_id?.toInt(),
                isArchived = row.is_archived != 0L,
                createdAt = row.created_at,
                updatedAt = row.updated_at
            )
        }
    }

    fun getHabitById(id: Int): Habit? {
        val row = queries.getHabitById(id.toLong()).executeAsOneOrNull() ?: return null
        return Habit(
            id = row.id.toInt(),
            name = row.name,
            icon = row.icon,
            color = row.color,
            objectiveId = row.objective_id?.toInt(),
            isArchived = row.is_archived != 0L,
            createdAt = row.created_at,
            updatedAt = row.updated_at
        )
    }

    fun createHabit(name: String, icon: String?, color: String?, objectiveId: Int?): Habit {
        return queries.transactionWithResult {
            queries.insertHabit(name, icon, color, objectiveId?.toLong())
            val id = queries.lastInsertRowId().executeAsOne()
            val row = queries.getHabitById(id).executeAsOne()
            Habit(
                id = row.id.toInt(),
                name = row.name,
                icon = row.icon,
                color = row.color,
                objectiveId = row.objective_id?.toInt(),
                isArchived = row.is_archived != 0L,
                createdAt = row.created_at,
                updatedAt = row.updated_at
            )
        }
    }

    fun updateHabit(id: Int, name: String, icon: String?, color: String?, objectiveId: Int?) {
        queries.updateHabit(name, icon, color, objectiveId?.toLong(), id.toLong())
    }

    fun archiveHabit(id: Int) {
        queries.archiveHabit(id.toLong())
    }

    fun getCompletionsForDate(date: String): List<HabitCompletion> {
        return queries.getCompletionsForDate(date).executeAsList().map { row ->
            HabitCompletion(
                id = row.id.toInt(),
                habitId = row.habit_id.toInt(),
                date = row.date,
                createdAt = row.created_at
            )
        }
    }

    fun toggleCompletion(habitId: Int, date: String): Boolean {
        val existing = queries.getCompletionForHabitAndDate(habitId.toLong(), date).executeAsOneOrNull()
        return if (existing != null) {
            queries.deleteCompletion(habitId.toLong(), date)
            false
        } else {
            queries.insertCompletion(habitId.toLong(), date)
            true
        }
    }

    fun getCompletionDatesForHabit(habitId: Int): List<String> {
        return queries.getCompletionDatesForHabit(habitId.toLong()).executeAsList()
    }

    fun getAllCompletionsInRange(startDate: String, endDate: String): List<HabitCompletion> {
        return queries.getAllCompletionsInRange(startDate, endDate).executeAsList().map { row ->
            HabitCompletion(
                id = row.id.toInt(),
                habitId = row.habit_id.toInt(),
                date = row.date,
                createdAt = row.created_at
            )
        }
    }

    fun getHabitsByObjectiveId(objectiveId: Int): List<Habit> {
        return queries.getHabitsByObjectiveId(objectiveId.toLong()).executeAsList().map { row ->
            Habit(
                id = row.id.toInt(),
                name = row.name,
                icon = row.icon,
                color = row.color,
                objectiveId = row.objective_id?.toInt(),
                isArchived = row.is_archived != 0L,
                createdAt = row.created_at,
                updatedAt = row.updated_at
            )
        }
    }
}
