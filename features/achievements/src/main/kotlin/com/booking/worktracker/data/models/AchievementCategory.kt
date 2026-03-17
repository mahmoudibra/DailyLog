package com.booking.worktracker.data.models

import com.booking.worktracker.core.generated.resources.*
import org.jetbrains.compose.resources.StringResource

enum class AchievementCategory(val nameRes: StringResource) {
    CONSISTENCY(Res.string.achievements_category_consistency),
    MASTERY(Res.string.achievements_category_mastery),
    EXPLORER(Res.string.achievements_category_explorer),
    CHALLENGE(Res.string.achievements_category_challenge)
}
