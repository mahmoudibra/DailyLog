package com.booking.worktracker.data.repository

import com.booking.worktracker.data.datasource.TimeBudgetLocalDataSource
import com.booking.worktracker.data.models.PeriodType
import com.booking.worktracker.data.models.TimeBudget
import kotlinx.datetime.LocalDate

class TimeBudgetRepository(
    private val localDataSource: TimeBudgetLocalDataSource = TimeBudgetLocalDataSource()
) {
    fun getAll(): List<TimeBudget> = localDataSource.getAll()
    fun getById(id: Int): TimeBudget? = localDataSource.getById(id)
    fun create(category: String, targetMinutes: Int, periodType: PeriodType, objectiveId: Int?): TimeBudget =
        localDataSource.create(category, targetMinutes, periodType, objectiveId)
    fun update(id: Int, category: String, targetMinutes: Int, periodType: PeriodType, objectiveId: Int?): TimeBudget =
        localDataSource.update(id, category, targetMinutes, periodType, objectiveId)
    fun delete(id: Int) = localDataSource.delete(id)
    fun getActualMinutesForCategory(category: String, startDate: LocalDate, endDate: LocalDate): Int =
        localDataSource.getActualMinutesForCategory(category, startDate, endDate)
    fun getObjectiveTitle(objectiveId: Int): String? = localDataSource.getObjectiveTitle(objectiveId)
    fun getActiveObjectives(): List<Pair<Int, String>> = localDataSource.getActiveObjectives()
}
