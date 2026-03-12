package com.booking.worktracker.domain.usecases.reviews

import com.booking.worktracker.data.models.AutoSummary
import com.booking.worktracker.data.models.DailyReview
import com.booking.worktracker.data.models.WeeklySummary
import com.booking.worktracker.data.repository.LogRepository
import com.booking.worktracker.data.repository.ObjectiveRepository
import com.booking.worktracker.data.repository.ReviewRepository
import com.booking.worktracker.data.repository.TimeEntryRepository
import kotlinx.datetime.*

class GetReviewForDateUseCase(private val reviewRepository: ReviewRepository = ReviewRepository()) {
    operator fun invoke(date: String): DailyReview? = reviewRepository.getReviewForDate(date)
}

class SaveDailyReviewUseCase(private val reviewRepository: ReviewRepository = ReviewRepository()) {
    operator fun invoke(date: String, wentWell: String?, couldImprove: String?, tomorrowPriority: String?): Result<DailyReview> {
        return try {
            if (wentWell == null && couldImprove == null && tomorrowPriority == null) {
                return Result.failure(IllegalArgumentException("Please fill in at least one field"))
            }
            val review = reviewRepository.insertOrUpdateReview(date, wentWell, couldImprove, tomorrowPriority)
            Result.success(review)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class LoadWeeklySummaryUseCase(private val reviewRepository: ReviewRepository = ReviewRepository()) {
    operator fun invoke(weekStartDate: String): WeeklySummary? = reviewRepository.getWeeklySummary(weekStartDate)
}

class SaveWeeklySummaryUseCase(private val reviewRepository: ReviewRepository = ReviewRepository()) {
    operator fun invoke(
        weekStartDate: String,
        weekEndDate: String,
        summaryText: String?,
        autoSummaryJson: String?
    ): Result<WeeklySummary> {
        return try {
            val summary = reviewRepository.insertOrUpdateWeeklySummary(weekStartDate, weekEndDate, summaryText, autoSummaryJson)
            Result.success(summary)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class GenerateAutoSummaryUseCase(
    private val logRepository: LogRepository = LogRepository(),
    private val timeEntryRepository: TimeEntryRepository = TimeEntryRepository(),
    private val objectiveRepository: ObjectiveRepository = ObjectiveRepository(),
    private val reviewRepository: ReviewRepository = ReviewRepository()
) {
    operator fun invoke(weekStart: LocalDate, weekEnd: LocalDate): Result<AutoSummary> {
        return try {
            val logs = logRepository.getLogForDateRange(weekStart, weekEnd)
            val entryCount = logs?.sumOf { it.entries.size } ?: 0

            var totalMinutes = 0
            var currentDate = weekStart
            while (currentDate <= weekEnd) {
                totalMinutes += timeEntryRepository.getTotalMinutesForDate(currentDate)
                currentDate = currentDate.plus(DatePeriod(days = 1))
            }

            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val year = today.year
            val quarter = (today.monthNumber - 1) / 3 + 1
            val yearlyObjectives = objectiveRepository.getYearlyObjectives(year)
            val quarterlyObjectives = objectiveRepository.getQuarterlyObjectives(year, quarter)
            val allObjectives = yearlyObjectives + quarterlyObjectives
            val objectivesProgressed = allObjectives.count { it.completionPercentage() > 0 }

            var streakDays = 0
            var checkDate = today
            while (logRepository.hasLogForDate(checkDate)) {
                streakDays++
                checkDate = checkDate.minus(DatePeriod(days = 1))
            }

            val topTags = logs?.flatMap { it.tags }
                ?.groupBy { it.name }
                ?.entries
                ?.sortedByDescending { it.value.size }
                ?.take(5)
                ?.map { it.key } ?: emptyList()

            val dailyReviewCount = reviewRepository.getReviewCountForDateRange(
                weekStart.toString(), weekEnd.toString()
            )

            Result.success(AutoSummary(
                entryCount = entryCount,
                timeTrackedMinutes = totalMinutes,
                objectivesProgressed = objectivesProgressed,
                streakDays = streakDays,
                topTags = topTags,
                dailyReviewCount = dailyReviewCount
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
