package com.booking.worktracker.data.repository

import com.booking.worktracker.data.datasource.AchievementLocalDataSource
import com.booking.worktracker.data.models.Achievement
import com.booking.worktracker.data.models.Rank
import com.booking.worktracker.data.models.UserLevel
import com.booking.worktracker.data.models.XpActionType
import com.booking.worktracker.data.models.XpEvent
import com.booking.worktracker.di.Singleton
import me.tatarka.inject.annotations.Inject

@Inject
@Singleton
class AchievementRepository(private val localDataSource: AchievementLocalDataSource) {

    fun getAllXpEvents(): List<XpEvent> = localDataSource.getAllXpEvents()

    fun getXpEventsByDateRange(startDate: String, endDate: String): List<XpEvent> =
        localDataSource.getXpEventsByDateRange(startDate, endDate)

    fun addXpEvent(actionType: XpActionType, xpAmount: Int, description: String?) {
        localDataSource.addXpEvent(actionType, xpAmount, description)
        refreshUserLevel(xpAmount)
    }

    fun getTotalXpForDateRange(startDate: String, endDate: String): Long =
        localDataSource.getTotalXpForDateRange(startDate, endDate)

    fun getXpByActionType(): Map<String, Long> = localDataSource.getXpByActionType()

    fun getDailyXpTotals(): List<Pair<String, Long>> = localDataSource.getDailyXpTotals()

    fun getUserLevel(): UserLevel {
        localDataSource.initUserLevel()
        return localDataSource.getUserLevel()!!
    }

    fun getAllAchievements(): List<Achievement> = localDataSource.getAllAchievements()

    fun getUnlockedAchievements(): List<Achievement> = localDataSource.getUnlockedAchievements()

    fun getLockedAchievements(): List<Achievement> = localDataSource.getLockedAchievements()

    fun unlockAchievement(achievementId: Int) = localDataSource.unlockAchievement(achievementId)

    fun seedDefaultAchievements() = localDataSource.seedDefaultAchievements()

    private fun refreshUserLevel(addedXp: Int) {
        localDataSource.initUserLevel()
        val current = localDataSource.getUserLevel() ?: return
        val newTotalXp = current.totalXp + addedXp
        val newRank = Rank.fromXp(newTotalXp)
        val newLevel = newRank.ordinal + 1
        localDataSource.updateUserLevel(newTotalXp, newLevel, newRank.name)
    }
}
