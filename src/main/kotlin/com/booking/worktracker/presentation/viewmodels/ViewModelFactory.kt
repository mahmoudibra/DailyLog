package com.booking.worktracker.presentation.viewmodels

class ViewModelFactory {
    fun createDailyLogViewModel(): DailyLogViewModel {
        return DailyLogViewModel()
    }

    fun createObjectivesViewModel(): ObjectivesViewModel {
        return ObjectivesViewModel()
    }
}
