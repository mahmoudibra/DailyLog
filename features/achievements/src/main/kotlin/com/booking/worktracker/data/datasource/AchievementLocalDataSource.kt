package com.booking.worktracker.data.datasource

import com.booking.worktracker.data.DailyTrackerDatabase
import com.booking.worktracker.data.models.Achievement
import com.booking.worktracker.data.models.AchievementDefinition
import com.booking.worktracker.data.models.UserLevel
import com.booking.worktracker.data.models.Rank
import com.booking.worktracker.data.models.XpActionType
import com.booking.worktracker.data.models.XpEvent
import com.booking.worktracker.di.Singleton
import me.tatarka.inject.annotations.Inject

@Inject
@Singleton
class AchievementLocalDataSource(db: DailyTrackerDatabase) {

    private val queries = db.achievementsQueries

    fun getAllXpEvents(): List<XpEvent> {
        return queries.getAllXpEvents().executeAsList().map { row ->
            XpEvent(
                id = row.id.toInt(),
                actionType = XpActionType.valueOf(row.action_type),
                xpAmount = row.xp_amount.toInt(),
                description = row.description,
                createdAt = row.created_at
            )
        }
    }

    fun getXpEventsByDateRange(startDate: String, endDate: String): List<XpEvent> {
        return queries.getXpEventsByDateRange(startDate, endDate).executeAsList().map { row ->
            XpEvent(
                id = row.id.toInt(),
                actionType = XpActionType.valueOf(row.action_type),
                xpAmount = row.xp_amount.toInt(),
                description = row.description,
                createdAt = row.created_at
            )
        }
    }

    fun addXpEvent(actionType: XpActionType, xpAmount: Int, description: String?) {
        queries.insertXpEvent(actionType.name, xpAmount.toLong(), description)
    }

    fun getTotalXpForDateRange(startDate: String, endDate: String): Long {
        return queries.getTotalXpForDateRange(startDate, endDate).executeAsOne()
    }

    fun getXpByActionType(): Map<String, Long> {
        return queries.getXpByActionType().executeAsList().associate { row ->
            row.action_type to row.total_xp!!
        }
    }

    fun getDailyXpTotals(): List<Pair<String, Long>> {
        return queries.getDailyXpTotals().executeAsList().map { row ->
            row.date!! to row.total_xp!!
        }
    }

    fun getUserLevel(): UserLevel? {
        val row = queries.getUserLevel().executeAsOneOrNull() ?: return null
        val totalXp = row.total_xp
        val rank = Rank.fromXp(totalXp)
        val nextRankXp = Rank.xpForNextRank(totalXp)
        val progressPercent = if (nextRankXp > rank.xpThreshold) {
            ((totalXp - rank.xpThreshold).toFloat() / (nextRankXp - rank.xpThreshold).toFloat())
        } else {
            1.0f
        }
        return UserLevel(
            totalXp = totalXp,
            currentLevel = row.current_level.toInt(),
            rankTitle = row.rank_title,
            xpForNextLevel = nextRankXp,
            progressPercent = progressPercent.coerceIn(0f, 1f)
        )
    }

    fun initUserLevel() {
        queries.initUserLevel()
    }

    fun updateUserLevel(totalXp: Long, currentLevel: Int, rankTitle: String) {
        queries.updateUserLevel(totalXp, currentLevel.toLong(), rankTitle)
    }

    fun getAllAchievements(): List<Achievement> {
        val unlockedIds = queries.getUnlockedAchievements().executeAsList()
            .associate { it.id.toInt() to it.unlocked_at }

        return queries.getAllAchievements().executeAsList().mapNotNull { row ->
            val id = row.id.toInt()
            val definition = AchievementDefinition.fromKey(row.achievement_key) ?: return@mapNotNull null
            Achievement(
                id = id,
                definition = definition,
                xpNeeded = row.xp_needed.toInt(),
                isUnlocked = unlockedIds.containsKey(id),
                unlockedAt = unlockedIds[id]
            )
        }
    }

    fun getUnlockedAchievements(): List<Achievement> {
        return queries.getUnlockedAchievements().executeAsList().mapNotNull { row ->
            val definition = AchievementDefinition.fromKey(row.achievement_key) ?: return@mapNotNull null
            Achievement(
                id = row.id.toInt(),
                definition = definition,
                xpNeeded = row.xp_needed.toInt(),
                isUnlocked = true,
                unlockedAt = row.unlocked_at
            )
        }
    }

    fun getLockedAchievements(): List<Achievement> {
        return queries.getLockedAchievements().executeAsList().mapNotNull { row ->
            val definition = AchievementDefinition.fromKey(row.achievement_key) ?: return@mapNotNull null
            Achievement(
                id = row.id.toInt(),
                definition = definition,
                xpNeeded = row.xp_needed.toInt(),
                isUnlocked = false,
                unlockedAt = null
            )
        }
    }

    fun unlockAchievement(achievementId: Int) {
        queries.unlockAchievement(achievementId.toLong())
    }

    fun seedDefaultAchievements() {
        val existing = queries.getAllAchievements().executeAsList()
        if (existing.isNotEmpty()) return

        AchievementDefinition.entries.forEach { def ->
            queries.insertAchievement(def.name, def.requirementValue.toLong())
        }
    }
}
