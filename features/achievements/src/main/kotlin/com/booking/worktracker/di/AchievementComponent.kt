package com.booking.worktracker.di

import com.booking.worktracker.presentation.viewmodels.AchievementViewModel
import me.tatarka.inject.annotations.Component

@Component
abstract class AchievementComponent(@Component val parent: DatabaseComponent) {

    abstract val achievementViewModel: AchievementViewModel

    companion object {
        val instance: AchievementComponent by lazy {
            AchievementComponent::class.create(DatabaseComponent.instance)
        }
    }
}
