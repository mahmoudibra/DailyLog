package com.booking.worktracker.domain.usecases.objectives

import com.booking.worktracker.data.models.ChecklistItem
import com.booking.worktracker.data.models.Objective
import com.booking.worktracker.data.models.ObjectiveStatus
import com.booking.worktracker.data.models.ObjectiveType
import com.booking.worktracker.data.repository.ObjectiveRepository
import me.tatarka.inject.annotations.Inject

@Inject
class CreateObjectiveUseCase(private val repository: ObjectiveRepository) {
    operator fun invoke(
        title: String,
        description: String,
        type: ObjectiveType,
        year: Int,
        quarter: Int? = null
    ): Result<Objective> {
        return try {
            require(title.isNotBlank()) { "Objective title cannot be blank" }
            val objective = repository.createObjective(title, description, type, year, quarter)
            Result.success(objective)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

@Inject
class GetObjectivesUseCase(private val repository: ObjectiveRepository) {
    fun getYearly(year: Int): List<Objective> = repository.getYearlyObjectives(year)
    fun getQuarterly(year: Int, quarter: Int): List<Objective> = repository.getQuarterlyObjectives(year, quarter)
}

@Inject
class UpdateObjectiveUseCase(private val repository: ObjectiveRepository) {
    operator fun invoke(
        id: Int,
        title: String,
        description: String,
        status: ObjectiveStatus
    ): Result<Objective> {
        return try {
            require(title.isNotBlank()) { "Objective title cannot be blank" }
            val objective = repository.updateObjective(id, title, description, status)
            Result.success(objective)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

@Inject
class DeleteObjectiveUseCase(private val repository: ObjectiveRepository) {
    operator fun invoke(id: Int): Result<Unit> {
        return try {
            repository.deleteObjective(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

@Inject
class ManageChecklistUseCase(private val repository: ObjectiveRepository) {
    fun addItem(objectiveId: Int, text: String): Result<ChecklistItem> {
        return try {
            require(text.isNotBlank()) { "Checklist item text cannot be blank" }
            val item = repository.addChecklistItem(objectiveId, text)
            Result.success(item)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun toggleItem(itemId: Int): Result<ChecklistItem> {
        return try {
            val item = repository.toggleChecklistItem(itemId)
            Result.success(item)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun deleteItem(itemId: Int): Result<Unit> {
        return try {
            repository.deleteChecklistItem(itemId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
