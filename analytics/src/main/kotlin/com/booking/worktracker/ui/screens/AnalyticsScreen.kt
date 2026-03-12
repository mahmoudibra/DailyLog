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
import com.booking.worktracker.data.models.*
import com.booking.worktracker.data.repository.AnalyticsRepository
import com.booking.worktracker.presentation.viewmodels.AnalyticsViewModel
import com.booking.worktracker.ui.designsystem.components.*
import com.booking.worktracker.ui.designsystem.tokens.SpacingTokens
import com.booking.worktracker.ui.localization.LocalStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    analyticsRepository: AnalyticsRepository
) {
    val strings = LocalStrings.current
    val viewModel = remember { AnalyticsViewModel(analyticsRepository) }
    val summary by viewModel.summary.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SpacingTokens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.sectionSpacing)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DSScreenTitle(strings.analyticsAndInsights)
            DSIconButton(
                icon = Icons.Default.Refresh,
                contentDescription = strings.refresh,
                onClick = { viewModel.loadData() }
            )
        }

        if (isLoading) {
            DSLoadingIndicator(modifier = Modifier.weight(1f))
        } else if (summary == null) {
            DSEmptyState(
                message = strings.noDataAvailable,
                modifier = Modifier.weight(1f)
            )
        } else {
            val data = summary!!

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.medium),
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
                        DSSectionHeader(title = strings.tagUsage)
                    }
                    items(data.tagStats) { tag ->
                        TagStatRow(tag, data.tagStats.maxOfOrNull { it.usageCount } ?: 1)
                    }
                }

                // Weekly activity
                if (data.weeklyStats.isNotEmpty()) {
                    item {
                        DSSectionHeader(title = strings.weeklyActivity)
                    }
                    items(data.weeklyStats) { week ->
                        WeeklyStatRow(week)
                    }
                }

                // Recent daily activity
                if (data.recentDailyStats.isNotEmpty()) {
                    item {
                        DSSectionHeader(title = strings.recentDailyActivity)
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
    val strings = LocalStrings.current
    DSCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.small)) {
            DSSectionHeader(title = strings.streaks)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = streakInfo.currentStreak.toString(),
                    label = strings.currentStreak,
                    icon = Icons.Default.LocalFireDepartment
                )
                StatItem(
                    value = streakInfo.longestStreak.toString(),
                    label = strings.bestStreak,
                    icon = Icons.Default.EmojiEvents
                )
                StatItem(
                    value = streakInfo.totalDaysLogged.toString(),
                    label = strings.daysLogged,
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
    val strings = LocalStrings.current
    DSCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.small)) {
            DSSectionHeader(title = strings.overview)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = totalEntries.toString(),
                    label = strings.totalEntries,
                    icon = Icons.Default.Article
                )
                StatItem(
                    value = String.format("%.1f", averagePerDay),
                    label = strings.avgPerDay,
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
                        text = strings.mostActiveDay,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = mostActiveDay,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ObjectiveStatsCard(stats: ObjectiveStats) {
    val strings = LocalStrings.current
    DSCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.small)) {
            DSSectionHeader(title = strings.objectives)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(value = stats.totalObjectives.toString(), label = strings.total, icon = Icons.Default.Flag)
                StatItem(value = stats.completedObjectives.toString(), label = strings.completed, icon = Icons.Default.CheckCircle)
                StatItem(value = stats.inProgressObjectives.toString(), label = strings.inProgress, icon = Icons.Default.Schedule)
            }
            if (stats.totalObjectives > 0) {
                DSDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = strings.checklistCompletion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = strings.percentValue(stats.averageChecklistCompletion),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
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
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun TagStatRow(tag: TagStats, maxUsage: Int) {
    val strings = LocalStrings.current
    DSCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = tag.tagName, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = strings.timesUsed(tag.usageCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            LinearProgressIndicator(
                progress = if (maxUsage > 0) tag.usageCount.toFloat() / maxUsage else 0f,
                modifier = Modifier.width(SpacingTokens.space16 * 6)
            )
        }
    }
}

@Composable
fun WeeklyStatRow(week: WeeklyStats) {
    val strings = LocalStrings.current
    DSCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${week.weekStart} - ${week.weekEnd}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = strings.activeDays(week.activeDays),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = strings.entriesCountLabel(week.totalEntries),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun DailyStatRow(day: DailyStats) {
    val strings = LocalStrings.current
    DSCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = day.date, style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(SpacingTokens.medium)) {
                Text(
                    text = strings.entriesCountLabel(day.entryCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (day.tagCount > 0) {
                    Text(
                        text = strings.tagsCount(day.tagCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
