package com.booking.worktracker.di

import com.booking.worktracker.presentation.viewmodels.TimeTrackingViewModel
import me.tatarka.inject.annotations.Component

@Component
abstract class TimeTrackingComponent(@Component val parent: AppComponent) {

    abstract val timeTrackingViewModel: TimeTrackingViewModel

    companion object {
        val instance: TimeTrackingComponent by lazy {
            TimeTrackingComponent::class.create(AppComponent.instance)
        }
    }
}
