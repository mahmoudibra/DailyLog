package com.booking.worktracker.data.models

enum class Rank(val xpThreshold: Long) {
    APPRENTICE(0),
    JOURNEYMAN(500),
    CRAFTSMAN(2000),
    MASTER(5000),
    GRANDMASTER(10000);

    companion object {
        fun fromXp(totalXp: Long): Rank {
            return entries.lastOrNull { totalXp >= it.xpThreshold } ?: APPRENTICE
        }

        fun xpForNextRank(totalXp: Long): Long {
            val nextRank = entries.firstOrNull { it.xpThreshold > totalXp }
            return nextRank?.xpThreshold ?: entries.last().xpThreshold
        }
    }
}
