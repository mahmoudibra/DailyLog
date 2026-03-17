package com.booking.worktracker.data.models

import com.booking.worktracker.core.generated.resources.*
import org.jetbrains.compose.resources.StringResource

enum class Rank(val xpThreshold: Long, val nameRes: StringResource) {
    APPRENTICE(0, Res.string.achievements_rank_apprentice),
    JOURNEYMAN(500, Res.string.achievements_rank_journeyman),
    CRAFTSMAN(2000, Res.string.achievements_rank_craftsman),
    MASTER(5000, Res.string.achievements_rank_master),
    GRANDMASTER(10000, Res.string.achievements_rank_grandmaster);

    companion object {
        fun fromXp(totalXp: Long): Rank {
            return entries.lastOrNull { totalXp >= it.xpThreshold } ?: APPRENTICE
        }

        fun xpForNextRank(totalXp: Long): Long {
            val nextRank = entries.firstOrNull { it.xpThreshold > totalXp }
            return nextRank?.xpThreshold ?: entries.last().xpThreshold
        }

        fun fromName(name: String): Rank? = entries.find { it.name == name.uppercase() }
    }
}
