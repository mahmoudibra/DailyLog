package com.booking.worktracker.di

import com.booking.worktracker.presentation.viewmodels.TimeBudgetsViewModel
import me.tatarka.inject.annotations.Component

@Component
abstract class TimeBudgetsComponent(@Component val parent: AppComponent) {
    abstract val timeBudgetsViewModel: TimeBudgetsViewModel

    companion object {
        val instance: TimeBudgetsComponent by lazy {
            TimeBudgetsComponent::class.create(AppComponent.instance)
        }
    }
}
