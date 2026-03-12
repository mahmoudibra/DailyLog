package com.booking.worktracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.booking.worktracker.core.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.pluralStringResource
import com.booking.worktracker.data.models.*
import com.booking.worktracker.presentation.viewmodels.AnalyticsViewModel
import com.booking.worktracker.ui.designsystem.DSTheme
import com.booking.worktracker.ui.designsystem.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen() {
    val viewModel = remember { AnalyticsViewModel() }
    val summary by viewModel.summary.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(DSTheme.spacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.sectionSpacing)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DSScreenTitle(stringResource(Res.string.analytics_and_insights))
            DSIconButton(
                icon = Icons.Default.Refresh,
                contentDescription = stringResource(Res.string.action_refresh),
                onClick = { viewModel.loadData() }
            )
        }

        if (isLoading) {
            DSLoadingIndicator(modifier = Modifier.weight(1f))
        } else if (summary == null) {
            DSEmptyState(
                message = stringResource(Res.string.no_data_available),
                modifier = Modifier.weight(1f)
            )
        } else {
            val data = summary!!

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.medium),
                modifier = Modifier.weight(1f)
            ) {
                // Streak card
                item {
                    StreakCard(data.streakInfo)
                }

                // Overview card
                item {
                    OverviewCard(
                        totalEntries = data.totalEntries,
                        averagePerDay = data.averageEntriesPerDay,
                        mostActiveDay = data.mostActiveDay,
                        totalDaysLogged = data.streakInfo.totalDaysLogged
                    )
                }

                // Objective stats
                item {
                    ObjectiveStatsCard(data.objectiveStats)
                }

                // Tag usage
                if (data.tagStats.isNotEmpty()) {
                    item {
                        DSSectionHeader(title = stringResource(Res.string.tag_usage))
                    }
                    items(data.tagStats) { tag ->
                        TagStatRow(tag, data.tagStats.maxOfOrNull { it.usageCount } ?: 1)
                    }
                }

                // Weekly activity
                if (data.weeklyStats.isNotEmpty()) {
                    item {
                        DSSectionHeader(title = stringResource(Res.string.weekly_activity))
                    }
                    items(data.weeklyStats) { week ->
                        WeeklyStatRow(week)
                    }
                }

                // Recent daily activity
                if (data.recentDailyStats.isNotEmpty()) {
                    item {
                        DSSectionHeader(title = stringResource(Res.string.recent_daily_activity))
                    }
                    items(data.recentDailyStats.take(14)) { day ->
                        DailyStatRow(day)
                    }
                }
            }
        }
    }
}

@Composable
fun StreakCard(streakInfo: StreakInfo) {
    DSCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.small)) {
            DSSectionHeader(title = stringResource(Res.string.streaks))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = streakInfo.currentStreak.toString(),
                    label = stringResource(Res.string.current_streak),
                    icon = Icons.Default.LocalFireDepartment
                )
                StatItem(
                    value = streakInfo.longestStreak.toString(),
                    label = stringResource(Res.string.best_streak),
                    icon = Icons.Default.EmojiEvents
                )
                StatItem(
                    value = streakInfo.totalDaysLogged.toString(),
                    label = stringResource(Res.string.days_logged),
                    icon = Icons.Default.CalendarMonth
                )
            }
        }
    }
}

@Composable
fun OverviewCard(
    totalEntries: Int,
    averagePerDay: Double,
    mostActiveDay: String?,
    totalDaysLogged: Int
) {
    DSCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.small)) {
            DSSectionHeader(title = stringResource(Res.string.overview))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = totalEntries.toString(),
                    label = stringResource(Res.string.total_entries),
                    icon = Icons.Default.Article
                )
                StatItem(
                    value = String.format("%.1f", averagePerDay),
                    label = stringResource(Res.string.avg_per_day),
                    icon = Icons.Default.TrendingUp
                )
            }
            if (mostActiveDay != null) {
                DSDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(Res.string.most_active_day),
                        style = DSTheme.font.bodyMedium,
                        color = DSTheme.colors.onSurfaceVariant
                    )
                    Text(
                        text = mostActiveDay,
                        style = DSTheme.font.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ObjectiveStatsCard(stats: ObjectiveStats) {
    DSCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.small)) {
            DSSectionHeader(title = stringResource(Res.string.objectives))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(value = stats.totalObjectives.toString(), label = stringResource(Res.string.total), icon = Icons.Default.Flag)
                StatItem(value = stats.completedObjectives.toString(), label = stringResource(Res.string.completed), icon = Icons.Default.CheckCircle)
                StatItem(value = stats.inProgressObjectives.toString(), label = stringResource(Res.string.in_progress), icon = Icons.Default.Schedule)
            }
            if (stats.totalObjectives > 0) {
                DSDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(Res.string.checklist_completion),
                        style = DSTheme.font.bodyMedium,
                        color = DSTheme.colors.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(Res.string.percent_value, stats.averageChecklistCompletion),
                        style = DSTheme.font.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = DSTheme.colors.primary
                    )
                }
                LinearProgressIndicator(
                    progress = stats.averageChecklistCompletion / 100f,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun StatItem(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            contentDescription = null,
            tint = DSTheme.colors.primary
        )
        Text(
            text = value,
            style = DSTheme.font.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = DSTheme.font.bodySmall,
            color = DSTheme.colors.onSurfaceVariant
        )
    }
}

@Composable
fun TagStatRow(tag: TagStats, maxUsage: Int) {
    DSCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = tag.tagName, style = DSTheme.font.bodyLarge)
                Text(
                    text = pluralStringResource(Res.plurals.times_used, tag.usageCount, tag.usageCount),
                    style = DSTheme.font.bodySmall,
                    color = DSTheme.colors.onSurfaceVariant
                )
            }
            LinearProgressIndicator(
                progress = if (maxUsage > 0) tag.usageCount.toFloat() / maxUsage else 0f,
                modifier = Modifier.width(DSTheme.spacing.space16 * 6)
            )
        }
    }
}

@Composable
fun WeeklyStatRow(week: WeeklyStats) {
    DSCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${week.weekStart} - ${week.weekEnd}",
                    style = DSTheme.font.bodyMedium
                )
                Text(
                    text = stringResource(Res.string.active_days, week.activeDays),
                    style = DSTheme.font.bodySmall,
                    color = DSTheme.colors.onSurfaceVariant
                )
            }
            Text(
                text = pluralStringResource(Res.plurals.entries_count_label, week.totalEntries, week.totalEntries),
                style = DSTheme.font.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = DSTheme.colors.primary
            )
        }
    }
}

@Composable
fun DailyStatRow(day: DailyStats) {
    DSCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = day.date, style = DSTheme.font.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(DSTheme.spacing.medium)) {
                Text(
                    text = pluralStringResource(Res.plurals.entries_count_label, day.entryCount, day.entryCount),
                    style = DSTheme.font.bodyMedium,
                    color = DSTheme.colors.primary
                )
                if (day.tagCount > 0) {
                    Text(
                        text = pluralStringResource(Res.plurals.tags_count, day.tagCount, day.tagCount),
                        style = DSTheme.font.bodySmall,
                        color = DSTheme.colors.onSurfaceVariant
                    )
                }
            }
        }
    }
}
