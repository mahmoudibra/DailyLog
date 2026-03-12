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
import com.booking.worktracker.data.repository.LogRepository
import com.booking.worktracker.data.repository.ObjectiveRepository
import com.booking.worktracker.data.repository.SettingsRepository
import com.booking.worktracker.data.repository.TagRepository
import com.booking.worktracker.data.repository.TimeEntryRepository
import com.booking.worktracker.data.repository.AnalyticsRepository
import com.booking.worktracker.data.repository.ExportRepository
import com.booking.worktracker.ui.designsystem.WorkTrackerTheme
import com.booking.worktracker.ui.designsystem.tokens.ColorTokens
import com.booking.worktracker.ui.designsystem.tokens.ShapeTokens
import com.booking.worktracker.ui.localization.AppLocale
import com.booking.worktracker.core.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import com.booking.worktracker.ui.localization.ProvideLocalization
import com.booking.worktracker.ui.screens.*

enum class Screen {
    DAILY_LOG, LOG_LIST, OBJECTIVES, TIME_TRACKING, ANALYTICS, EXPORT, SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    logRepository: LogRepository,
    tagRepository: TagRepository,
    objectiveRepository: ObjectiveRepository,
    settingsRepository: SettingsRepository,
    timeEntryRepository: TimeEntryRepository,
    analyticsRepository: AnalyticsRepository,
    exportRepository: ExportRepository
) {
    var currentScreen by remember { mutableStateOf(Screen.DAILY_LOG) }
    var currentLocale by remember { mutableStateOf(AppLocale.ENGLISH) }

    LaunchedEffect(Unit) {
        currentLocale = AppLocale.fromCode(settingsRepository.getLanguage())
    }

    ProvideLocalization(locale = currentLocale) {
        WorkTrackerTheme {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Permanent side rail
                Surface(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(220.dp),
                    color = MaterialTheme.colorScheme.surface,
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
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
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
                            color = MaterialTheme.colorScheme.outlineVariant
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

                        Spacer(Modifier.weight(1f))

                        Divider(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
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
                            logRepository = logRepository,
                            tagRepository = tagRepository,
                            onNavigateToObjectives = { currentScreen = Screen.OBJECTIVES },
                            onNavigateToTimer = { currentScreen = Screen.TIME_TRACKING }
                        )
                        Screen.LOG_LIST -> LogListScreen(logRepository)
                        Screen.OBJECTIVES -> ObjectivesScreen(objectiveRepository)
                        Screen.TIME_TRACKING -> TimeTrackingScreen(timeEntryRepository)
                        Screen.ANALYTICS -> AnalyticsScreen(analyticsRepository)
                        Screen.EXPORT -> ExportScreen(exportRepository)
                        Screen.SETTINGS -> SettingsScreen(
                            settingsRepository = settingsRepository,
                            currentLocale = currentLocale,
                            onLanguageChanged = { locale ->
                                settingsRepository.setLanguage(locale.code)
                                currentLocale = locale
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
    val bgColor = if (selected) ColorTokens.Primary.copy(alpha = 0.12f)
        else Color.Transparent
    val contentColor = if (selected) ColorTokens.Primary
        else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = ShapeTokens.medium,
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
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor
            )
        }
    }
}
