package com.booking.worktracker.di

import com.booking.worktracker.presentation.viewmodels.ObjectivesViewModel
import me.tatarka.inject.annotations.Component

@Component
abstract class ObjectivesComponent(@Component val parent: DatabaseComponent) {

    abstract val objectivesViewModel: ObjectivesViewModel

    companion object {
        val instance: ObjectivesComponent by lazy {
            ObjectivesComponent::class.create(DatabaseComponent.instance)
        }
    }
}
