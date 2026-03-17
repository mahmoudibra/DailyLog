package com.booking.worktracker.di

import com.booking.worktracker.presentation.viewmodels.FocusZonesViewModel
import me.tatarka.inject.annotations.Component

@Component
abstract class FocusZonesComponent(@Component val parent: DatabaseComponent) {

    abstract val focusZonesViewModel: FocusZonesViewModel

    companion object {
        val instance: FocusZonesComponent by lazy {
            FocusZonesComponent::class.create(DatabaseComponent.instance)
        }
    }
}
