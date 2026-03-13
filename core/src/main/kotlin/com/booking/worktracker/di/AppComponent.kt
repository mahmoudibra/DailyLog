package com.booking.worktracker.di

import com.booking.worktracker.data.DailyWorkTrackerDatabase
import com.booking.worktracker.data.DatabaseProvider
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@Component
@Singleton
abstract class AppComponent {

    @Provides
    @Singleton
    fun provideDatabase(): DailyWorkTrackerDatabase = DatabaseProvider.getDatabase()

    companion object {
        val instance: AppComponent by lazy { AppComponent::class.create() }
    }
}
