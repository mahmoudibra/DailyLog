package com.booking.worktracker.di

import com.booking.worktracker.presentation.viewmodels.AnalyticsViewModel
import me.tatarka.inject.annotations.Component

@Component
abstract class AnalyticsComponent(@Component val parent: AppComponent) {
    abstract val analyticsViewModel: AnalyticsViewModel

    companion object {
        val instance: AnalyticsComponent by lazy {
            AnalyticsComponent::class.create(AppComponent.instance)
        }
    }
}
