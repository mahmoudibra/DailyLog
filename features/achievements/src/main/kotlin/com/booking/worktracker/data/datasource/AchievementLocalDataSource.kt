package com.booking.worktracker.data.datasource

import com.booking.worktracker.data.DailyTrackerDatabase
import com.booking.worktracker.data.models.Achievement
import com.booking.worktracker.data.models.AchievementCategory
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

        return queries.getAllAchievements().executeAsList().map { row ->
            val id = row.id.toInt()
            Achievement(
                id = id,
                name = row.name,
                description = row.description,
                category = AchievementCategory.valueOf(row.category),
                icon = row.icon,
                xpReward = row.xp_reward.toInt(),
                requirementType = row.requirement_type,
                requirementValue = row.requirement_value.toInt(),
                isUnlocked = unlockedIds.containsKey(id),
                unlockedAt = unlockedIds[id]
            )
        }
    }

    fun getUnlockedAchievements(): List<Achievement> {
        return queries.getUnlockedAchievements().executeAsList().map { row ->
            Achievement(
                id = row.id.toInt(),
                name = row.name,
                description = row.description,
                category = AchievementCategory.valueOf(row.category),
                icon = row.icon,
                xpReward = row.xp_reward.toInt(),
                requirementType = row.requirement_type,
                requirementValue = row.requirement_value.toInt(),
                isUnlocked = true,
                unlockedAt = row.unlocked_at
            )
        }
    }

    fun getLockedAchievements(): List<Achievement> {
        return queries.getLockedAchievements().executeAsList().map { row ->
            Achievement(
                id = row.id.toInt(),
                name = row.name,
                description = row.description,
                category = AchievementCategory.valueOf(row.category),
                icon = row.icon,
                xpReward = row.xp_reward.toInt(),
                requirementType = row.requirement_type,
                requirementValue = row.requirement_value.toInt(),
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

        val defaults = listOf(
            AchievementSeed("First Log", "Create your first daily log entry", "CONSISTENCY", "pencil", 25, "LOG_COUNT", 1),
            AchievementSeed("Week Warrior", "Log entries for 7 consecutive days", "CONSISTENCY", "fire", 100, "STREAK_DAYS", 7),
            AchievementSeed("Month Master", "Log entries for 30 consecutive days", "CONSISTENCY", "trophy", 250, "STREAK_DAYS", 30),
            AchievementSeed("Time Tracker", "Complete your first time tracking session", "MASTERY", "clock", 25, "TIME_SESSION_COUNT", 1),
            AchievementSeed("Goal Setter", "Create your first objective", "EXPLORER", "target", 25, "OBJECTIVE_COUNT", 1),
            AchievementSeed("Checklist Champion", "Complete 10 checklist items", "MASTERY", "check", 100, "CHECKLIST_COUNT", 10),
            AchievementSeed("Review Rookie", "Write your first daily review", "EXPLORER", "book", 25, "REVIEW_COUNT", 1),
            AchievementSeed("Budget Boss", "Hit a time budget target", "CHALLENGE", "chart", 50, "BUDGET_HIT_COUNT", 1),
            AchievementSeed("Century Club", "Earn 100 total XP", "CHALLENGE", "star", 50, "TOTAL_XP", 100),
            AchievementSeed("XP Veteran", "Earn 1000 total XP", "CHALLENGE", "medal", 150, "TOTAL_XP", 1000),
        )

        defaults.forEach { seed ->
            queries.insertAchievement(
                seed.name,
                seed.description,
                seed.category,
                seed.icon,
                seed.xpReward.toLong(),
                seed.requirementType,
                seed.requirementValue.toLong()
            )
        }
    }

    private data class AchievementSeed(
        val name: String,
        val description: String,
        val category: String,
        val icon: String,
        val xpReward: Int,
        val requirementType: String,
        val requirementValue: Int
    )
}
