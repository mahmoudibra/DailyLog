package com.booking.worktracker.domain.usecases

import com.booking.worktracker.data.models.UserLevel
import com.booking.worktracker.data.models.XpActionType
import com.booking.worktracker.data.repository.AchievementRepository
import me.tatarka.inject.annotations.Inject

@Inject
class AwardXpUseCase(private val repository: AchievementRepository) {

    operator fun invoke(actionType: XpActionType, description: String?): Result<UserLevel> = try {
        val xpAmount = xpForAction(actionType)
        repository.addXpEvent(actionType, xpAmount, description)
        Result.success(repository.getUserLevel())
    } catch (e: Exception) {
        Result.failure(e)
    }

    companion object {
        fun xpForAction(actionType: XpActionType): Int = when (actionType) {
            XpActionType.LOG_ENTRY -> 10
            XpActionType.TIME_SESSION -> 15
            XpActionType.CHECKLIST_COMPLETE -> 20
            XpActionType.STREAK_MAINTAINED -> 25
            XpActionType.REVIEW_WRITTEN -> 30
            XpActionType.BUDGET_HIT -> 35
        }
    }
}
