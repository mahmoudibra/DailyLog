package com.booking.worktracker.domain.usecases

import com.booking.worktracker.data.models.Achievement
import com.booking.worktracker.data.models.AchievementCategory
import com.booking.worktracker.data.repository.AchievementRepository
import me.tatarka.inject.annotations.Inject

@Inject
class GetAchievementsUseCase(private val repository: AchievementRepository) {

    operator fun invoke(category: AchievementCategory?): Result<List<Achievement>> = try {
        val achievements = repository.getAllAchievements()
        val filtered = if (category != null) {
            achievements.filter { it.category == category }
        } else {
            achievements
        }
        Result.success(filtered)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
