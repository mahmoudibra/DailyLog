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
import com.booking.worktracker.ui.designsystem.components.*
import com.booking.worktracker.ui.designsystem.tokens.DSColors
import com.booking.worktracker.ui.designsystem.tokens.SpacingTokens
import com.booking.worktracker.core.generated.resources.*
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.stringResource

@Composable
fun ReviewsScreen(viewModel: ReviewsViewModel) {
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
            .padding(SpacingTokens.screenPadding)
    ) {
        DSScreenTitle(stringResource(Res.string.review_reflection))

        Spacer(Modifier.height(SpacingTokens.large))

        // Tab toggle between Daily and Weekly
        TabRow(
            selectedTabIndex = if (isWeeklyView) 1 else 0,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = DSColors.Primary
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

        Spacer(Modifier.height(SpacingTokens.large))

        // Save message banner
        saveMessage?.let { message ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = DSColors.TertiaryContainer,
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier.padding(SpacingTokens.medium),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.small)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = DSColors.Tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DSColors.OnTertiaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    DSIconButton(
                        icon = Icons.Default.Close,
                        contentDescription = stringResource(Res.string.action_close),
                        onClick = { viewModel.clearSaveMessage() }
                    )
                }
            }
            Spacer(Modifier.height(SpacingTokens.medium))
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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            DSIconButton(
                icon = Icons.Default.ChevronRight,
                contentDescription = stringResource(Res.string.next_month),
                onClick = { onDateChange(selectedDate.plus(DatePeriod(days = 1))) }
            )
        }

        if (hasExistingReview) {
            Spacer(Modifier.height(SpacingTokens.small))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = DSColors.CompletedGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(Res.string.review_saved),
                    style = MaterialTheme.typography.bodySmall,
                    color = DSColors.CompletedGreen
                )
            }
        }

        Spacer(Modifier.height(SpacingTokens.extraLarge))

        // Prompt 1: What went well?
        PromptCard(
            title = stringResource(Res.string.went_well_prompt),
            icon = Icons.Default.ThumbUp,
            iconColor = DSColors.CardGreen,
            text = wentWellText,
            placeholder = stringResource(Res.string.went_well_placeholder),
            onTextChange = onWentWellChange
        )

        Spacer(Modifier.height(SpacingTokens.large))

        // Prompt 2: What could improve?
        PromptCard(
            title = stringResource(Res.string.could_improve_prompt),
            icon = Icons.Default.TrendingUp,
            iconColor = DSColors.CardOrange,
            text = couldImproveText,
            placeholder = stringResource(Res.string.could_improve_placeholder),
            onTextChange = onCouldImproveChange
        )

        Spacer(Modifier.height(SpacingTokens.large))

        // Prompt 3: Tomorrow priority
        PromptCard(
            title = stringResource(Res.string.tomorrow_priority_prompt),
            icon = Icons.Default.Star,
            iconColor = DSColors.CardBlue,
            text = tomorrowPriorityText,
            placeholder = stringResource(Res.string.tomorrow_priority_placeholder),
            onTextChange = onTomorrowPriorityChange
        )

        Spacer(Modifier.height(SpacingTokens.extraLarge))

        DSButton(
            text = stringResource(Res.string.save_review),
            onClick = onSave,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(SpacingTokens.extraLarge))
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
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.small)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(SpacingTokens.medium))
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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            DSIconButton(
                icon = Icons.Default.ChevronRight,
                contentDescription = stringResource(Res.string.next_week),
                onClick = onNextWeek
            )
        }

        Spacer(Modifier.height(SpacingTokens.extraLarge))

        DSSectionHeader(title = stringResource(Res.string.weekly_stats))

        Spacer(Modifier.height(SpacingTokens.medium))

        if (autoSummary != null) {
            Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.medium)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.medium)
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
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.medium)
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
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.medium)
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
                                    tint = DSColors.Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(SpacingTokens.small))
                                Text(
                                    stringResource(Res.string.top_tags_stat),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.height(SpacingTokens.small))
                            Text(
                                autoSummary.topTags.joinToString(", "),
                                style = MaterialTheme.typography.bodyMedium,
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

        Spacer(Modifier.height(SpacingTokens.extraLarge))

        DSSectionHeader(title = stringResource(Res.string.weekly_reflection_prompt))

        Spacer(Modifier.height(SpacingTokens.medium))

        DSOutlinedTextField(
            value = weeklySummaryText,
            onValueChange = onWeeklySummaryTextChange,
            placeholder = stringResource(Res.string.weekly_reflection_placeholder),
            modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp),
            singleLine = false
        )

        Spacer(Modifier.height(SpacingTokens.large))

        DSButton(
            text = stringResource(Res.string.save_weekly_summary),
            onClick = onSave,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(SpacingTokens.extraLarge))
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
                tint = DSColors.Primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(SpacingTokens.small))
            Text(
                title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(SpacingTokens.small))
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatMinutes(totalMinutes: Int): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}
