package com.booking.worktracker.di

import com.booking.worktracker.data.DailyTrackerDatabase
import com.booking.worktracker.data.DatabaseProvider
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@Component
@Singleton
abstract class DatabaseComponent {

    @Provides
    @Singleton
    fun provideDatabase(): DailyTrackerDatabase = DatabaseProvider.getDatabase()

    companion object {
        val instance: DatabaseComponent by lazy { DatabaseComponent::class.create() }
    }
}
