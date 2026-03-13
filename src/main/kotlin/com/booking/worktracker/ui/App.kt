package com.booking.worktracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.booking.worktracker.data.DatabaseProvider
import com.booking.worktracker.data.datasource.SettingsLocalDataSource
import com.booking.worktracker.data.repository.SettingsRepository
import com.booking.worktracker.ui.designsystem.DSTheme
import com.booking.worktracker.ui.designsystem.WorkTrackerTheme
import com.booking.worktracker.ui.localization.AppLocale
import com.booking.worktracker.core.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import com.booking.worktracker.ui.localization.ProvideLocalization
import com.booking.worktracker.ui.screens.*

enum class Screen {
    DAILY_LOG, LOG_LIST, OBJECTIVES, TIME_TRACKING, ANALYTICS, EXPORT, REVIEWS, FOCUS_ZONES, TIME_BUDGETS, HABITS, SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val settingsRepository = remember { SettingsRepository(SettingsLocalDataSource(DatabaseProvider.getDatabase())) }
    var currentScreen by remember { mutableStateOf(Screen.DAILY_LOG) }
    var currentLocale by remember { mutableStateOf(AppLocale.ENGLISH) }
    var isDarkMode by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        currentLocale = AppLocale.fromCode(settingsRepository.getLanguage())
        isDarkMode = settingsRepository.isDarkMode()
    }

    ProvideLocalization(locale = currentLocale) {
        WorkTrackerTheme(darkTheme = isDarkMode) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DSTheme.colors.background)
            ) {
                // Permanent side rail
                Surface(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(220.dp),
                    color = DSTheme.colors.surface,
                    tonalElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 20.dp, horizontal = 8.dp)
                    ) {
                        // App title
                        Text(
                            text = stringResource(Res.string.daily_tracker),
                            style = DSTheme.font.titleLarge,
                            color = DSTheme.colors.primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
                        )

                        Spacer(Modifier.height(8.dp))

                        // Main nav items
                        SideNavItem(
                            icon = Icons.Default.Home,
                            label = stringResource(Res.string.nav_home),
                            selected = currentScreen == Screen.DAILY_LOG,
                            onClick = { currentScreen = Screen.DAILY_LOG }
                        )
                        SideNavItem(
                            icon = Icons.Default.History,
                            label = stringResource(Res.string.nav_history),
                            selected = currentScreen == Screen.LOG_LIST,
                            onClick = { currentScreen = Screen.LOG_LIST }
                        )

                        Spacer(Modifier.height(12.dp))
                        Divider(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            color = DSTheme.colors.outlineVariant
                        )
                        Spacer(Modifier.height(12.dp))

                        SideNavItem(
                            icon = Icons.Default.Flag,
                            label = stringResource(Res.string.nav_objectives),
                            selected = currentScreen == Screen.OBJECTIVES,
                            onClick = { currentScreen = Screen.OBJECTIVES }
                        )
                        SideNavItem(
                            icon = Icons.Default.Timer,
                            label = stringResource(Res.string.nav_time_tracking),
                            selected = currentScreen == Screen.TIME_TRACKING,
                            onClick = { currentScreen = Screen.TIME_TRACKING }
                        )
                        SideNavItem(
                            icon = Icons.Default.BarChart,
                            label = stringResource(Res.string.nav_analytics),
                            selected = currentScreen == Screen.ANALYTICS,
                            onClick = { currentScreen = Screen.ANALYTICS }
                        )
                        SideNavItem(
                            icon = Icons.Default.FileDownload,
                            label = stringResource(Res.string.nav_export),
                            selected = currentScreen == Screen.EXPORT,
                            onClick = { currentScreen = Screen.EXPORT }
                        )
                        SideNavItem(
                            icon = Icons.Default.RateReview,
                            label = stringResource(Res.string.nav_reviews),
                            selected = currentScreen == Screen.REVIEWS,
                            onClick = { currentScreen = Screen.REVIEWS }
                        )
                        SideNavItem(
                            icon = Icons.Default.Psychology,
                            label = stringResource(Res.string.nav_focus_zones),
                            selected = currentScreen == Screen.FOCUS_ZONES,
                            onClick = { currentScreen = Screen.FOCUS_ZONES }
                        )
                        SideNavItem(
                            icon = Icons.Default.PieChart,
                            label = "Time Budgets",
                            selected = currentScreen == Screen.TIME_BUDGETS,
                            onClick = { currentScreen = Screen.TIME_BUDGETS }
                        )
                        SideNavItem(
                            icon = Icons.Default.LocalFireDepartment,
                            label = "Habits",
                            selected = currentScreen == Screen.HABITS,
                            onClick = { currentScreen = Screen.HABITS }
                        )

                        Spacer(Modifier.weight(1f))

                        Divider(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            color = DSTheme.colors.outlineVariant
                        )
                        Spacer(Modifier.height(8.dp))

                        SideNavItem(
                            icon = Icons.Default.Settings,
                            label = stringResource(Res.string.nav_settings),
                            selected = currentScreen == Screen.SETTINGS,
                            onClick = { currentScreen = Screen.SETTINGS }
                        )
                    }
                }

                // Main content area
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

@Composable
private fun SideNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (selected) DSTheme.colors.primary.copy(alpha = 0.12f)
        else Color.Transparent
    val contentColor = if (selected) DSTheme.colors.primary
        else DSTheme.colors.onSurfaceVariant

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = DSTheme.shapes.medium,
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                style = DSTheme.font.bodyMedium,
                color = contentColor
            )
        }
    }
}
