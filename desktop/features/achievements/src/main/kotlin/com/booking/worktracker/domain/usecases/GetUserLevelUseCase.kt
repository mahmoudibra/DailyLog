package com.booking.worktracker.domain.usecases

import com.booking.worktracker.data.models.UserLevel
import com.booking.worktracker.data.repository.AchievementRepository
import me.tatarka.inject.annotations.Inject

@Inject
class GetUserLevelUseCase(private val repository: AchievementRepository) {

    operator fun invoke(): Result<UserLevel> = try {
        Result.success(repository.getUserLevel())
    } catch (e: Exception) {
        Result.failure(e)
    }
}
