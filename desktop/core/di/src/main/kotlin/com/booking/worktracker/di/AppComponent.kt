package com.booking.worktracker.di

import kotlinx.coroutines.CoroutineDispatcher
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@Component
abstract class AppComponent {

    @Provides
    fun provideIoDispatcher(): CoroutineDispatcher = DispatcherProvider.io

    companion object {
        val instance: AppComponent by lazy { AppComponent::class.create() }
    }
}
