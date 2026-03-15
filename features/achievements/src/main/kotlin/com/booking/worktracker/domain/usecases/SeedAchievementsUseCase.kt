package com.booking.worktracker.domain.usecases

import com.booking.worktracker.data.repository.AchievementRepository
import me.tatarka.inject.annotations.Inject

@Inject
class SeedAchievementsUseCase(private val repository: AchievementRepository) {

    operator fun invoke(): Result<Unit> = try {
        repository.seedDefaultAchievements()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
