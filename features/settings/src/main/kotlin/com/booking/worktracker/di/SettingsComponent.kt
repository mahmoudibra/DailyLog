package com.booking.worktracker.di

import com.booking.worktracker.presentation.viewmodels.SettingsViewModel
import me.tatarka.inject.annotations.Component

@Component
abstract class SettingsComponent(@Component val parent: DatabaseComponent) {

    abstract val settingsViewModel: SettingsViewModel

    companion object {
        val instance: SettingsComponent by lazy {
            SettingsComponent::class.create(DatabaseComponent.instance)
        }
    }
}
