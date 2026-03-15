package com.booking.worktracker.domain.usecases

import com.booking.worktracker.data.models.Achievement
import com.booking.worktracker.data.repository.AchievementRepository
import me.tatarka.inject.annotations.Inject

@Inject
class CheckAndUnlockAchievementsUseCase(private val repository: AchievementRepository) {

    operator fun invoke(): Result<List<Achievement>> = try {
        val locked = repository.getLockedAchievements()
        val userLevel = repository.getUserLevel()
        val xpByAction = repository.getXpByActionType()
        val newlyUnlocked = mutableListOf<Achievement>()

        for (achievement in locked) {
            val met = isRequirementMet(
                requirementType = achievement.requirementType,
                requirementValue = achievement.requirementValue,
                totalXp = userLevel.totalXp,
                xpByAction = xpByAction
            )
            if (met) {
                repository.unlockAchievement(achievement.id)
                newlyUnlocked.add(achievement.copy(isUnlocked = true))
            }
        }

        Result.success(newlyUnlocked)
    } catch (e: Exception) {
        Result.failure(e)
    }

    private fun isRequirementMet(
        requirementType: String,
        requirementValue: Int,
        totalXp: Long,
        xpByAction: Map<String, Long>
    ): Boolean {
        return when (requirementType) {
            "TOTAL_XP" -> totalXp >= requirementValue
            "LOG_COUNT" -> countFromXp(xpByAction, "LOG_ENTRY", 10) >= requirementValue
            "TIME_SESSION_COUNT" -> countFromXp(xpByAction, "TIME_SESSION", 15) >= requirementValue
            "CHECKLIST_COUNT" -> countFromXp(xpByAction, "CHECKLIST_COMPLETE", 20) >= requirementValue
            "REVIEW_COUNT" -> countFromXp(xpByAction, "REVIEW_WRITTEN", 30) >= requirementValue
            "BUDGET_HIT_COUNT" -> countFromXp(xpByAction, "BUDGET_HIT", 35) >= requirementValue
            "OBJECTIVE_COUNT" -> countFromXp(xpByAction, "CHECKLIST_COMPLETE", 20) >= requirementValue
            "STREAK_DAYS" -> countFromXp(xpByAction, "STREAK_MAINTAINED", 25) >= requirementValue
            else -> false
        }
    }

    private fun countFromXp(xpByAction: Map<String, Long>, actionType: String, xpPerAction: Int): Long {
        val totalXpForAction = xpByAction[actionType] ?: 0L
        return totalXpForAction / xpPerAction
    }
}
