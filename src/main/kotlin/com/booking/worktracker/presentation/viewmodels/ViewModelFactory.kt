package com.booking.worktracker.presentation.viewmodels

import com.booking.worktracker.data.repository.LogRepository
import com.booking.worktracker.data.repository.ObjectiveRepository
import com.booking.worktracker.data.repository.TagRepository

class ViewModelFactory(
    private val logRepository: LogRepository,
    private val tagRepository: TagRepository,
    private val objectiveRepository: ObjectiveRepository
) {
    fun createDailyLogViewModel(): DailyLogViewModel {
        return DailyLogViewModel(logRepository, tagRepository)
    }

    fun createObjectivesViewModel(): ObjectivesViewModel {
        return ObjectivesViewModel(objectiveRepository)
    }
}
