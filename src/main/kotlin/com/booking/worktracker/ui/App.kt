package com.booking.worktracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.booking.worktracker.data.DatabaseProvider
import com.booking.worktracker.data.datasource.SettingsLocalDataSource
import com.booking.worktracker.data.repository.SettingsRepository
import com.booking.worktracker.ui.designsystem.DSTheme
import com.booking.worktracker.ui.designsystem.WorkTrackerTheme
import com.booking.worktracker.ui.localization.AppLocale
import com.booking.worktracker.core.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import com.booking.worktracker.ui.localization.ProvideLocalization
import com.booking.worktracker.ui.navigation.NavItem
import com.booking.worktracker.ui.navigation.SideNavBar
import com.booking.worktracker.ui.screens.*

enum class Screen {
    DAILY_LOG, LOG_LIST, OBJECTIVES, TIME_TRACKING, ANALYTICS, EXPORT, REVIEWS, FOCUS_ZONES, TIME_BUDGETS, HABITS, SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val settingsRepository = remember { SettingsRepository(SettingsLocalDataSource(DatabaseProvider.getDatabase())) }
    var showSplash by remember { mutableStateOf(true) }
    var currentScreen by remember { mutableStateOf(Screen.DAILY_LOG) }
    var currentLocale by remember { mutableStateOf(AppLocale.ENGLISH) }
    var isDarkMode by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        currentLocale = AppLocale.fromCode(settingsRepository.getLanguage())
        isDarkMode = settingsRepository.isDarkMode()
    }

    ProvideLocalization(locale = currentLocale) {
        val navItems = listOf(
            NavItem(Screen.DAILY_LOG, Icons.Default.Home, stringResource(Res.string.nav_home)),
            NavItem(Screen.LOG_LIST, Icons.Default.History, stringResource(Res.string.nav_history)),
            NavItem(Screen.OBJECTIVES, Icons.Default.Flag, stringResource(Res.string.nav_objectives)),
            NavItem(Screen.TIME_TRACKING, Icons.Default.Timer, stringResource(Res.string.nav_time_tracking)),
            NavItem(Screen.ANALYTICS, Icons.Default.BarChart, stringResource(Res.string.nav_analytics)),
            NavItem(Screen.EXPORT, Icons.Default.FileDownload, stringResource(Res.string.nav_export)),
            NavItem(Screen.REVIEWS, Icons.Default.RateReview, stringResource(Res.string.nav_reviews)),
            NavItem(Screen.FOCUS_ZONES, Icons.Default.Psychology, stringResource(Res.string.nav_focus_zones)),
            NavItem(Screen.TIME_BUDGETS, Icons.Default.PieChart, stringResource(Res.string.nav_time_budgets)),
            NavItem(Screen.HABITS, Icons.Default.LocalFireDepartment, stringResource(Res.string.nav_habits)),
        )

        WorkTrackerTheme(darkTheme = isDarkMode) {
            if (showSplash) {
                SplashScreen(onSplashFinished = { showSplash = false })
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DSTheme.colors.background)
                ) {
                    SideNavBar(
                        currentScreen = currentScreen,
                        onScreenSelected = { currentScreen = it },
                        navItems = navItems,
                        dividerAfterIndex = 1,
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        when (currentScreen) {
                            Screen.DAILY_LOG -> DailyLogScreen(
                                onNavigateToObjectives = { currentScreen = Screen.OBJECTIVES },
                                onNavigateToTimer = { currentScreen = Screen.TIME_TRACKING }
                            )
                            Screen.LOG_LIST -> LogListScreen()
                            Screen.OBJECTIVES -> ObjectivesScreen()
                            Screen.TIME_TRACKING -> TimeTrackingScreen()
                            Screen.ANALYTICS -> AnalyticsScreen()
                            Screen.EXPORT -> ExportScreen()
                            Screen.REVIEWS -> ReviewsScreen()
                            Screen.FOCUS_ZONES -> FocusZonesScreen()
                            Screen.TIME_BUDGETS -> TimeBudgetsScreen()
                            Screen.HABITS -> HabitsScreen()
                            Screen.SETTINGS -> SettingsScreen(
                                currentLocale = currentLocale,
                                isDarkMode = isDarkMode,
                                onLanguageChanged = { locale ->
                                    settingsRepository.setLanguage(locale.code)
                                    currentLocale = locale
                                },
                                onDarkModeChanged = { enabled ->
                                    settingsRepository.setDarkMode(enabled)
                                    isDarkMode = enabled
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
