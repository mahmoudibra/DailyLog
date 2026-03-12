package com.booking.worktracker.data.datasource

import com.booking.worktracker.data.DailyLogsQueries
import com.booking.worktracker.data.ObjectivesQueries
import com.booking.worktracker.data.TimeBudgetsQueries
import com.booking.worktracker.data.models.PeriodType
import com.booking.worktracker.data.models.TimeBudget
import kotlinx.datetime.LocalDate

class TimeBudgetLocalDataSource(
    private val queries: TimeBudgetsQueries,
    private val objectivesQueries: ObjectivesQueries,
    private val dailyLogsQueries: DailyLogsQueries
) {

    fun getAll(): List<TimeBudget> {
        return queries.getAll().executeAsList().map { it.toTimeBudget() }
    }

    fun getById(id: Int): TimeBudget? {
        return queries.getById(id.toLong()).executeAsOneOrNull()?.toTimeBudget()
    }

    fun create(category: String, targetMinutes: Int, periodType: PeriodType, objectiveId: Int?): TimeBudget {
        val id = queries.transactionWithResult {
            queries.insertBudget(category, targetMinutes.toLong(), periodType.name, objectiveId?.toLong())
            dailyLogsQueries.lastInsertRowId().executeAsOne()
        }
        return queries.getById(id).executeAsOne().toTimeBudget()
    }

    fun update(id: Int, category: String, targetMinutes: Int, periodType: PeriodType, objectiveId: Int?): TimeBudget {
        queries.updateBudget(category, targetMinutes.toLong(), periodType.name, objectiveId?.toLong(), id.toLong())
        return queries.getById(id.toLong()).executeAsOne().toTimeBudget()
    }

    fun delete(id: Int) {
        queries.deleteBudget(id.toLong())
    }

    fun getActualMinutesForCategory(category: String, startDate: LocalDate, endDate: LocalDate): Int {
        return queries.getActualMinutesForCategory(category, startDate.toString(), endDate.toString())
            .executeAsOne().toInt()
    }

    fun getObjectiveTitle(objectiveId: Int): String? {
        return queries.getObjectiveTitle(objectiveId.toLong()).executeAsOneOrNull()
    }

    fun getActiveObjectives(): List<Pair<Int, String>> {
        return objectivesQueries.getActiveObjectives().executeAsList().map {
            Pair(it.id.toInt(), it.title)
        }
    }

    private fun com.booking.worktracker.data.Time_budgets.toTimeBudget() = TimeBudget(
        id = id.toInt(),
        category = category,
        targetMinutes = target_minutes.toInt(),
        periodType = PeriodType.valueOf(period_type),
        objectiveId = objective_id?.toInt(),
        createdAt = created_at,
        updatedAt = updated_at
    )
}
