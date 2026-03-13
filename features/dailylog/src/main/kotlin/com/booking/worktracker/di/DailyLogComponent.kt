package com.booking.worktracker.di

import com.booking.worktracker.presentation.viewmodels.DailyLogViewModel
import com.booking.worktracker.presentation.viewmodels.LogListViewModel
import me.tatarka.inject.annotations.Component

@Component
abstract class DailyLogComponent(@Component val parent: AppComponent) {

    abstract val dailyLogViewModel: DailyLogViewModel

    abstract val logListViewModel: LogListViewModel

    companion object {
        val instance: DailyLogComponent by lazy {
            DailyLogComponent::class.create(AppComponent.instance)
        }
    }
}
