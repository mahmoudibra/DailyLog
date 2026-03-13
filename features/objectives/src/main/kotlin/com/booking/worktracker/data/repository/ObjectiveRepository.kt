package com.booking.worktracker.data.repository

import com.booking.worktracker.data.datasource.ObjectiveLocalDataSource
import com.booking.worktracker.data.models.ChecklistItem
import com.booking.worktracker.data.models.Objective
import com.booking.worktracker.data.models.ObjectiveStatus
import com.booking.worktracker.data.models.ObjectiveType
import com.booking.worktracker.di.Singleton
import me.tatarka.inject.annotations.Inject

@Inject
@Singleton
class ObjectiveRepository(private val localDataSource: ObjectiveLocalDataSource) {

    fun getYearlyObjectives(year: Int): List<Objective> {
        return localDataSource.getYearlyObjectives(year)
    }

    fun getQuarterlyObjectives(year: Int, quarter: Int): List<Objective> {
        return localDataSource.getQuarterlyObjectives(year, quarter)
    }

    fun createObjective(
        title: String,
        description: String,
        type: ObjectiveType,
        year: Int,
        quarter: Int? = null
    ): Objective {
        return localDataSource.createObjective(title, description, type, year, quarter)
    }

    fun updateObjective(
        id: Int,
        title: String,
        description: String,
        status: ObjectiveStatus
    ): Objective {
        return localDataSource.updateObjective(id, title, description, status)
    }

    fun deleteObjective(id: Int) {
        localDataSource.deleteObjective(id)
    }

    fun addChecklistItem(objectiveId: Int, text: String): ChecklistItem {
        return localDataSource.addChecklistItem(objectiveId, text)
    }

    fun toggleChecklistItem(itemId: Int): ChecklistItem {
        return localDataSource.toggleChecklistItem(itemId)
    }

    fun deleteChecklistItem(itemId: Int) {
        localDataSource.deleteChecklistItem(itemId)
    }
}
