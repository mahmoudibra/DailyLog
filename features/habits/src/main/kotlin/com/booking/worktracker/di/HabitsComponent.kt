package com.booking.worktracker.di

import com.booking.worktracker.presentation.viewmodels.HabitsViewModel
import me.tatarka.inject.annotations.Component

@Component
abstract class HabitsComponent(@Component val parent: DatabaseComponent) {
    abstract val habitsViewModel: HabitsViewModel

    companion object {
        val instance: HabitsComponent by lazy {
            HabitsComponent::class.create(DatabaseComponent.instance)
        }
    }
}
