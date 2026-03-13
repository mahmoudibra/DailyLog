package com.booking.worktracker.di

import com.booking.worktracker.presentation.viewmodels.ExportViewModel
import me.tatarka.inject.annotations.Component

@Component
abstract class ExportComponent(@Component val parent: AppComponent) {
    abstract val exportViewModel: ExportViewModel

    companion object {
        val instance: ExportComponent by lazy {
            ExportComponent::class.create(AppComponent.instance)
        }
    }
}
