package com.booking.worktracker

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.booking.worktracker.data.DatabaseProvider
import com.booking.worktracker.data.datasource.*
import com.booking.worktracker.data.repository.*
import com.booking.worktracker.domain.usecases.analytics.GetAnalyticsSummaryUseCase
import com.booking.worktracker.domain.usecases.export.GenerateExportUseCase
import com.booking.worktracker.domain.usecases.export.SaveExportToFileUseCase
import com.booking.worktracker.domain.usecases.focuszones.GetFocusSummaryUseCase
import com.booking.worktracker.domain.usecases.logs.*
import com.booking.worktracker.domain.usecases.objectives.*
import com.booking.worktracker.domain.usecases.reviews.*
import com.booking.worktracker.domain.usecases.timebudgets.*
import com.booking.worktracker.domain.usecases.timetracking.*
import com.booking.worktracker.notifications.ReminderScheduler
import com.booking.worktracker.presentation.viewmodels.*
import com.booking.worktracker.ui.App
import com.booking.worktracker.ui.localization.AppLocale
import com.booking.worktracker.core.generated.resources.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString

fun main() = application {
    DatabaseProvider.init()
    val db = DatabaseProvider.getDatabase()

    // DataSources
    val settingsDs = SettingsLocalDataSource(db.settingsQueries)
    val logDs = LogLocalDataSource(db.dailyLogsQueries, db.tagsQueries)
    val tagDs = TagLocalDataSource(db.tagsQueries, db.dailyLogsQueries)
    val objectiveDs = ObjectiveLocalDataSource(db.objectivesQueries, db.dailyLogsQueries)
    val timeEntryDs = TimeEntryLocalDataSource(db.timeEntriesQueries, db.dailyLogsQueries)
    val analyticsDs = AnalyticsLocalDataSource(db.analyticsQueries)
    val exportDs = ExportLocalDataSource(db.exportQueries)
    val reviewDs = ReviewLocalDataSource(db.reviewsQueries)
    val focusZonesDs = FocusZonesLocalDataSource(db.focusZonesQueries)
    val timeBudgetDs = TimeBudgetLocalDataSource(db.timeBudgetsQueries, db.objectivesQueries, db.dailyLogsQueries)

    // Repositories
    val settingsRepo = SettingsRepository(settingsDs)
    val logRepo = LogRepository(logDs)
    val tagRepo = TagRepository(tagDs)
    val objectiveRepo = ObjectiveRepository(objectiveDs)
    val timeEntryRepo = TimeEntryRepository(timeEntryDs)
    val analyticsRepo = AnalyticsRepository(analyticsDs)
    val exportRepo = ExportRepository(exportDs)
    val reviewRepo = ReviewRepository(reviewDs)
    val focusZonesRepo = FocusZonesRepository(focusZonesDs)
    val timeBudgetRepo = TimeBudgetRepository(timeBudgetDs)

    // Use Cases
    val getLogForDate = GetLogForDateUseCase(logRepo)
    val addWorkEntry = AddWorkEntryUseCase(logRepo)
    val deleteWorkEntry = DeleteWorkEntryUseCase(logRepo)
    val updateLogTags = UpdateLogTagsUseCase(logRepo)
    val getAllTags = GetAllTagsUseCase(tagRepo)
    val createTag = CreateTagUseCase(tagRepo)

    val getObjectives = GetObjectivesUseCase(objectiveRepo)
    val createObjective = CreateObjectiveUseCase(objectiveRepo)
    val updateObjective = UpdateObjectiveUseCase(objectiveRepo)
    val deleteObjective = DeleteObjectiveUseCase(objectiveRepo)
    val manageChecklist = ManageChecklistUseCase(objectiveRepo)

    val getTimeTrackingData = GetTimeTrackingDataUseCase(timeEntryRepo)
    val startTimer = StartTimerUseCase(timeEntryRepo)
    val stopTimer = StopTimerUseCase(timeEntryRepo)
    val addManualEntry = AddManualEntryUseCase(timeEntryRepo)
    val deleteTimeEntry = DeleteTimeEntryUseCase(timeEntryRepo)

    val getAnalyticsSummary = GetAnalyticsSummaryUseCase(analyticsRepo)
    val generateExport = GenerateExportUseCase(exportRepo)

    val getReviewForDate = GetReviewForDateUseCase(reviewRepo)
    val saveDailyReview = SaveDailyReviewUseCase(reviewRepo)
    val loadWeeklySummary = LoadWeeklySummaryUseCase(reviewRepo)
    val saveWeeklySummary = SaveWeeklySummaryUseCase(reviewRepo)
    val generateAutoSummary = GenerateAutoSummaryUseCase(logRepo, timeEntryRepo, objectiveRepo, reviewRepo)

    val getFocusSummary = GetFocusSummaryUseCase(focusZonesRepo)

    val getDashboard = GetTimeBudgetDashboardUseCase(timeBudgetRepo)
    val createBudget = CreateTimeBudgetUseCase(timeBudgetRepo)
    val updateBudget = UpdateTimeBudgetUseCase(timeBudgetRepo)
    val deleteBudget = DeleteTimeBudgetUseCase(timeBudgetRepo)

    // ViewModels
    val dailyLogVm = DailyLogViewModel(getLogForDate, addWorkEntry, deleteWorkEntry, updateLogTags, getAllTags, createTag)
    val objectivesVm = ObjectivesViewModel(getObjectives, createObjective, updateObjective, deleteObjective, manageChecklist)
    val timeTrackingVm = TimeTrackingViewModel(getTimeTrackingData, startTimer, stopTimer, addManualEntry, deleteTimeEntry)
    val analyticsVm = AnalyticsViewModel(getAnalyticsSummary)
    val exportVm = ExportViewModel(generateExport, SaveExportToFileUseCase())
    val reviewsVm = ReviewsViewModel(getReviewForDate, saveDailyReview, loadWeeklySummary, saveWeeklySummary, generateAutoSummary)
    val focusZonesVm = FocusZonesViewModel(getFocusSummary)
    val timeBudgetsVm = TimeBudgetsViewModel(getDashboard, createBudget, updateBudget, deleteBudget)

    // Set system locale for Compose Resources
    val locale = AppLocale.fromCode(settingsRepo.getLanguage())
    java.util.Locale.setDefault(java.util.Locale(locale.code))

    val windowTitle = runBlocking { getString(Res.string.app_title) }

    val reminderScheduler = ReminderScheduler(logRepo, settingsRepo)
    reminderScheduler.start()

    Window(
        onCloseRequest = {
            reminderScheduler.stop()
            DatabaseProvider.close()
            exitApplication()
        },
        title = windowTitle,
        state = rememberWindowState(width = 900.dp, height = 700.dp)
    ) {
        App(
            settingsRepository = settingsRepo,
            logRepository = logRepo,
            tagRepository = tagRepo,
            objectiveRepository = objectiveRepo,
            dailyLogViewModel = dailyLogVm,
            objectivesViewModel = objectivesVm,
            timeTrackingViewModel = timeTrackingVm,
            analyticsViewModel = analyticsVm,
            exportViewModel = exportVm,
            reviewsViewModel = reviewsVm,
            focusZonesViewModel = focusZonesVm,
            timeBudgetsViewModel = timeBudgetsVm
        )
    }
}
