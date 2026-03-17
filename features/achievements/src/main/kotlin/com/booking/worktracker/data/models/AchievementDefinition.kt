package com.booking.worktracker.data.models

import com.booking.worktracker.core.generated.resources.*
import org.jetbrains.compose.resources.StringResource

enum class AchievementDefinition(
    val titleRes: StringResource,
    val subtitleRes: StringResource,
    val descriptionRes: StringResource,
    val category: AchievementCategory,
    val icon: String,
    val xpReward: Int,
    val requirementType: String,
    val requirementValue: Int
) {
    FIRST_LOG(
        titleRes = Res.string.achievement_first_log,
        subtitleRes = Res.string.achievement_icon_pencil,
        descriptionRes = Res.string.achievement_first_log_desc,
        category = AchievementCategory.CONSISTENCY,
        icon = "pencil",
        xpReward = 25,
        requirementType = "LOG_COUNT",
        requirementValue = 1
    ),
    WEEK_WARRIOR(
        titleRes = Res.string.achievement_week_warrior,
        subtitleRes = Res.string.achievement_icon_fire,
        descriptionRes = Res.string.achievement_week_warrior_desc,
        category = AchievementCategory.CONSISTENCY,
        icon = "fire",
        xpReward = 100,
        requirementType = "STREAK_DAYS",
        requirementValue = 7
    ),
    MONTH_MASTER(
        titleRes = Res.string.achievement_month_master,
        subtitleRes = Res.string.achievement_icon_trophy,
        descriptionRes = Res.string.achievement_month_master_desc,
        category = AchievementCategory.CONSISTENCY,
        icon = "trophy",
        xpReward = 250,
        requirementType = "STREAK_DAYS",
        requirementValue = 30
    ),
    TIME_TRACKER(
        titleRes = Res.string.achievement_time_tracker,
        subtitleRes = Res.string.achievement_icon_clock,
        descriptionRes = Res.string.achievement_time_tracker_desc,
        category = AchievementCategory.MASTERY,
        icon = "clock",
        xpReward = 25,
        requirementType = "TIME_SESSION_COUNT",
        requirementValue = 1
    ),
    GOAL_SETTER(
        titleRes = Res.string.achievement_goal_setter,
        subtitleRes = Res.string.achievement_icon_target,
        descriptionRes = Res.string.achievement_goal_setter_desc,
        category = AchievementCategory.EXPLORER,
        icon = "target",
        xpReward = 25,
        requirementType = "OBJECTIVE_COUNT",
        requirementValue = 1
    ),
    CHECKLIST_CHAMPION(
        titleRes = Res.string.achievement_checklist_champion,
        subtitleRes = Res.string.achievement_icon_check,
        descriptionRes = Res.string.achievement_checklist_champion_desc,
        category = AchievementCategory.MASTERY,
        icon = "check",
        xpReward = 100,
        requirementType = "CHECKLIST_COUNT",
        requirementValue = 10
    ),
    REVIEW_ROOKIE(
        titleRes = Res.string.achievement_review_rookie,
        subtitleRes = Res.string.achievement_icon_book,
        descriptionRes = Res.string.achievement_review_rookie_desc,
        category = AchievementCategory.EXPLORER,
        icon = "book",
        xpReward = 25,
        requirementType = "REVIEW_COUNT",
        requirementValue = 1
    ),
    BUDGET_BOSS(
        titleRes = Res.string.achievement_budget_boss,
        subtitleRes = Res.string.achievement_icon_chart,
        descriptionRes = Res.string.achievement_budget_boss_desc,
        category = AchievementCategory.CHALLENGE,
        icon = "chart",
        xpReward = 50,
        requirementType = "BUDGET_HIT_COUNT",
        requirementValue = 1
    ),
    CENTURY_CLUB(
        titleRes = Res.string.achievement_century_club,
        subtitleRes = Res.string.achievement_icon_star,
        descriptionRes = Res.string.achievement_century_club_desc,
        category = AchievementCategory.CHALLENGE,
        icon = "star",
        xpReward = 50,
        requirementType = "TOTAL_XP",
        requirementValue = 100
    ),
    XP_VETERAN(
        titleRes = Res.string.achievement_xp_veteran,
        subtitleRes = Res.string.achievement_icon_medal,
        descriptionRes = Res.string.achievement_xp_veteran_desc,
        category = AchievementCategory.CHALLENGE,
        icon = "medal",
        xpReward = 150,
        requirementType = "TOTAL_XP",
        requirementValue = 1000
    );

    companion object {
        fun fromKey(key: String): AchievementDefinition? = entries.find { it.name == key }
    }
}
