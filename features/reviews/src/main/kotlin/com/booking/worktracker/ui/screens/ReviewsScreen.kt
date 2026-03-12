package com.booking.worktracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.booking.worktracker.data.models.AutoSummary
import com.booking.worktracker.presentation.viewmodels.ReviewsViewModel
import com.booking.worktracker.ui.designsystem.DSTheme
import com.booking.worktracker.ui.designsystem.components.*
import com.booking.worktracker.core.generated.resources.*
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.stringResource

@Composable
fun ReviewsScreen() {
    val viewModel = remember { ReviewsViewModel() }
    val selectedDate by viewModel.selectedDate.collectAsState()
    val isWeeklyView by viewModel.isWeeklyView.collectAsState()
    val wentWellText by viewModel.wentWellText.collectAsState()
    val couldImproveText by viewModel.couldImproveText.collectAsState()
    val tomorrowPriorityText by viewModel.tomorrowPriorityText.collectAsState()
    val weeklySummaryText by viewModel.weeklySummaryText.collectAsState()
    val autoSummary by viewModel.autoSummary.collectAsState()
    val currentReview by viewModel.currentReview.collectAsState()
    val saveMessage by viewModel.saveMessage.collectAsState()
    val weekStartDate by viewModel.weekStartDate.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(DSTheme.spacing.screenPadding)
    ) {
        DSScreenTitle(stringResource(Res.string.review_reflection))

        Spacer(Modifier.height(DSTheme.spacing.large))

        // Tab toggle between Daily and Weekly
        TabRow(
            selectedTabIndex = if (isWeeklyView) 1 else 0,
            containerColor = DSTheme.colors.surface,
            contentColor = DSTheme.colors.primary
        ) {
            Tab(
                selected = !isWeeklyView,
                onClick = { viewModel.setWeeklyView(false) },
                text = { Text(stringResource(Res.string.daily_review)) }
            )
            Tab(
                selected = isWeeklyView,
                onClick = { viewModel.setWeeklyView(true) },
                text = { Text(stringResource(Res.string.weekly_summary)) }
            )
        }

        Spacer(Modifier.height(DSTheme.spacing.large))

        // Save message banner
        saveMessage?.let { message ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = DSTheme.colors.tertiaryContainer,
                shape = DSTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier.padding(DSTheme.spacing.medium),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DSTheme.spacing.small)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = DSTheme.colors.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = message,
                        style = DSTheme.font.bodyMedium,
                        color = DSTheme.colors.onTertiaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    DSIconButton(
                        icon = Icons.Default.Close,
                        contentDescription = stringResource(Res.string.action_close),
                        onClick = { viewModel.clearSaveMessage() }
                    )
                }
            }
            Spacer(Modifier.height(DSTheme.spacing.medium))
        }

        if (!isWeeklyView) {
            DailyReviewContent(
                selectedDate = selectedDate,
                wentWellText = wentWellText,
                couldImproveText = couldImproveText,
                tomorrowPriorityText = tomorrowPriorityText,
                hasExistingReview = currentReview != null,
                onWentWellChange = viewModel::updateWentWell,
                onCouldImproveChange = viewModel::updateCouldImprove,
                onTomorrowPriorityChange = viewModel::updateTomorrowPriority,
                onSave = viewModel::saveDailyReview,
                onDateChange = viewModel::setDate
            )
        } else {
            WeeklySummaryContent(
                weekStartDate = weekStartDate,
                autoSummary = autoSummary,
                weeklySummaryText = weeklySummaryText,
                onWeeklySummaryTextChange = viewModel::updateWeeklySummaryText,
                onSave = viewModel::saveWeeklySummary,
                onPreviousWeek = viewModel::navigateToPreviousWeek,
                onNextWeek = viewModel::navigateToNextWeek
            )
        }
    }
}

@Composable
private fun DailyReviewContent(
    selectedDate: LocalDate,
    wentWellText: String,
    couldImproveText: String,
    tomorrowPriorityText: String,
    hasExistingReview: Boolean,
    onWentWellChange: (String) -> Unit,
    onCouldImproveChange: (String) -> Unit,
    onTomorrowPriorityChange: (String) -> Unit,
    onSave: () -> Unit,
    onDateChange: (LocalDate) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Date navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DSIconButton(
                icon = Icons.Default.ChevronLeft,
                contentDescription = stringResource(Res.string.previous_month),
                onClick = { onDateChange(selectedDate.minus(DatePeriod(days = 1))) }
            )
            Text(
                text = selectedDate.toString(),
                style = DSTheme.font.titleMedium,
                fontWeight = FontWeight.Bold
            )
            DSIconButton(
                icon = Icons.Default.ChevronRight,
                contentDescription = stringResource(Res.string.next_month),
                onClick = { onDateChange(selectedDate.plus(DatePeriod(days = 1))) }
            )
        }

        if (hasExistingReview) {
            Spacer(Modifier.height(DSTheme.spacing.small))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = DSTheme.completedGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(Res.string.review_saved),
                    style = DSTheme.font.bodySmall,
                    color = DSTheme.completedGreen
                )
            }
        }

        Spacer(Modifier.height(DSTheme.spacing.extraLarge))

        // Prompt 1: What went well?
        PromptCard(
            title = stringResource(Res.string.went_well_prompt),
            icon = Icons.Default.ThumbUp,
            iconColor = DSTheme.cardGreen,
            text = wentWellText,
            placeholder = stringResource(Res.string.went_well_placeholder),
            onTextChange = onWentWellChange
        )

        Spacer(Modifier.height(DSTheme.spacing.large))

        // Prompt 2: What could improve?
        PromptCard(
            title = stringResource(Res.string.could_improve_prompt),
            icon = Icons.Default.TrendingUp,
            iconColor = DSTheme.cardOrange,
            text = couldImproveText,
            placeholder = stringResource(Res.string.could_improve_placeholder),
            onTextChange = onCouldImproveChange
        )

        Spacer(Modifier.height(DSTheme.spacing.large))

        // Prompt 3: Tomorrow priority
        PromptCard(
            title = stringResource(Res.string.tomorrow_priority_prompt),
            icon = Icons.Default.Star,
            iconColor = DSTheme.cardBlue,
            text = tomorrowPriorityText,
            placeholder = stringResource(Res.string.tomorrow_priority_placeholder),
            onTextChange = onTomorrowPriorityChange
        )

        Spacer(Modifier.height(DSTheme.spacing.extraLarge))

        DSButton(
            text = stringResource(Res.string.save_review),
            onClick = onSave,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(DSTheme.spacing.extraLarge))
    }
}

@Composable
private fun PromptCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: androidx.compose.ui.graphics.Color,
    text: String,
    placeholder: String,
    onTextChange: (String) -> Unit
) {
    DSElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DSTheme.spacing.small)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                style = DSTheme.font.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(DSTheme.spacing.medium))
        DSOutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = placeholder,
            modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
            singleLine = false
        )
    }
}

@Composable
private fun WeeklySummaryContent(
    weekStartDate: LocalDate,
    autoSummary: AutoSummary?,
    weeklySummaryText: String,
    onWeeklySummaryTextChange: (String) -> Unit,
    onSave: () -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Week navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DSIconButton(
                icon = Icons.Default.ChevronLeft,
                contentDescription = stringResource(Res.string.previous_week),
                onClick = onPreviousWeek
            )
            Text(
                text = stringResource(Res.string.week_of, weekStartDate.toString()),
                style = DSTheme.font.titleMedium,
                fontWeight = FontWeight.Bold
            )
            DSIconButton(
                icon = Icons.Default.ChevronRight,
                contentDescription = stringResource(Res.string.next_week),
                onClick = onNextWeek
            )
        }

        Spacer(Modifier.height(DSTheme.spacing.extraLarge))

        DSSectionHeader(title = stringResource(Res.string.weekly_stats))

        Spacer(Modifier.height(DSTheme.spacing.medium))

        if (autoSummary != null) {
            Column(verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.medium)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DSTheme.spacing.medium)
                ) {
                    StatCard(
                        title = stringResource(Res.string.entries_logged_stat),
                        value = autoSummary.entryCount.toString(),
                        icon = Icons.Default.Edit,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = stringResource(Res.string.time_tracked_stat),
                        value = formatMinutes(autoSummary.timeTrackedMinutes),
                        icon = Icons.Default.Timer,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DSTheme.spacing.medium)
                ) {
                    StatCard(
                        title = stringResource(Res.string.objectives_progressed_stat),
                        value = autoSummary.objectivesProgressed.toString(),
                        icon = Icons.Default.Flag,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = stringResource(Res.string.streak_days_stat),
                        value = autoSummary.streakDays.toString(),
                        icon = Icons.Default.LocalFireDepartment,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DSTheme.spacing.medium)
                ) {
                    StatCard(
                        title = stringResource(Res.string.reviews_completed_stat),
                        value = "${autoSummary.dailyReviewCount}/7",
                        icon = Icons.Default.RateReview,
                        modifier = Modifier.weight(1f)
                    )
                    if (autoSummary.topTags.isNotEmpty()) {
                        DSCard(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Label,
                                    contentDescription = null,
                                    tint = DSTheme.colors.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(DSTheme.spacing.small))
                                Text(
                                    stringResource(Res.string.top_tags_stat),
                                    style = DSTheme.font.bodySmall,
                                    color = DSTheme.colors.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.height(DSTheme.spacing.small))
                            Text(
                                autoSummary.topTags.joinToString(", "),
                                style = DSTheme.font.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        } else {
            DSEmptyState(message = stringResource(Res.string.no_data_available))
        }

        Spacer(Modifier.height(DSTheme.spacing.extraLarge))

        DSSectionHeader(title = stringResource(Res.string.weekly_reflection_prompt))

        Spacer(Modifier.height(DSTheme.spacing.medium))

        DSOutlinedTextField(
            value = weeklySummaryText,
            onValueChange = onWeeklySummaryTextChange,
            placeholder = stringResource(Res.string.weekly_reflection_placeholder),
            modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp),
            singleLine = false
        )

        Spacer(Modifier.height(DSTheme.spacing.large))

        DSButton(
            text = stringResource(Res.string.save_weekly_summary),
            onClick = onSave,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(DSTheme.spacing.extraLarge))
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    DSCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = DSTheme.colors.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(DSTheme.spacing.small))
            Text(
                title,
                style = DSTheme.font.bodySmall,
                color = DSTheme.colors.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(DSTheme.spacing.small))
        Text(
            value,
            style = DSTheme.font.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = DSTheme.colors.onSurface
        )
    }
}

private fun formatMinutes(totalMinutes: Int): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}
