package com.booking.worktracker.di

import com.booking.worktracker.presentation.viewmodels.ExportViewModel
import me.tatarka.inject.annotations.Component

@Component
abstract class ExportComponent(@Component val parent: DatabaseComponent) {
    abstract val exportViewModel: ExportViewModel

    companion object {
        val instance: ExportComponent by lazy {
            ExportComponent::class.create(DatabaseComponent.instance)
        }
    }
}
