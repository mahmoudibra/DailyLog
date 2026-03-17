package com.booking.worktracker.domain.usecases

import com.booking.worktracker.data.repository.AchievementRepository
import kotlinx.datetime.*
import me.tatarka.inject.annotations.Inject

@Inject
class GetWeeklyXpSummaryUseCase(private val repository: AchievementRepository) {

    operator fun invoke(): Result<List<Pair<String, Long>>> = try {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val startOfWeek = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)
        val fourWeeksAgo = startOfWeek.minus(28, DateTimeUnit.DAY)

        val dailyTotals = repository.getDailyXpTotals()
            .filter { (date, _) -> date >= fourWeeksAgo.toString() }

        Result.success(dailyTotals)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
