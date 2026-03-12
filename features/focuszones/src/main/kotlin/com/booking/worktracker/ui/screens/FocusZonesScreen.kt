package com.booking.worktracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.booking.worktracker.data.models.CategoryRecommendation
import com.booking.worktracker.data.models.FocusPattern
import com.booking.worktracker.data.models.HourlyFocusData
import com.booking.worktracker.presentation.viewmodels.FocusZonesViewModel
import com.booking.worktracker.core.generated.resources.*
import com.booking.worktracker.ui.designsystem.DSTheme
import com.booking.worktracker.ui.designsystem.components.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun FocusZonesScreen(
    viewModel: FocusZonesViewModel
) {
    val summary by viewModel.focusSummary.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val weeksBack by viewModel.weeksBack.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(DSTheme.spacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.sectionSpacing)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DSScreenTitle(stringResource(Res.string.focus_zones_title))

                // Weeks selector
                Row(horizontalArrangement = Arrangement.spacedBy(DSTheme.spacing.extraSmall)) {
                    listOf(2, 4, 8, 12).forEach { weeks ->
                        FilterChip(
                            selected = weeksBack == weeks,
                            onClick = { viewModel.setWeeksBack(weeks) },
                            label = { Text(stringResource(Res.string.focus_weeks_label, weeks)) }
                        )
                    }
                }
            }
        }

        if (isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else if (summary == null || summary!!.totalRatedEntries == 0) {
            item {
                DSEmptyState(
                    message = stringResource(Res.string.focus_empty_state),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            val data = summary!!

            // Summary stats
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DSTheme.spacing.medium)
                ) {
                    DSCard(modifier = Modifier.weight(1f)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${data.totalRatedEntries}",
                                style = DSTheme.font.headlineMedium,
                                color = DSTheme.colors.primary
                            )
                            Text(
                                text = stringResource(Res.string.focus_rated_entries),
                                style = DSTheme.font.bodySmall,
                                color = DSTheme.colors.onSurfaceVariant
                            )
                        }
                    }
                    DSCard(modifier = Modifier.weight(1f)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format("%.1f", data.averageFocusRating),
                                style = DSTheme.font.headlineMedium,
                                color = DSTheme.colors.primary
                            )
                            Text(
                                text = stringResource(Res.string.focus_avg_focus),
                                style = DSTheme.font.bodySmall,
                                color = DSTheme.colors.onSurfaceVariant
                            )
                            // Star rating visual
                            Row {
                                repeat(5) { i ->
                                    Icon(
                                        if (i < data.averageFocusRating.toInt()) Icons.Default.Star
                                        else Icons.Default.StarBorder,
                                        contentDescription = null,
                                        tint = if (i < data.averageFocusRating.toInt()) DSTheme.colors.primary
                                        else DSTheme.colors.outlineVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Peak Patterns
            if (data.patterns.isNotEmpty()) {
                item {
                    DSSectionHeader(title = stringResource(Res.string.focus_peak_windows))
                }
                items(data.patterns.take(3)) { pattern ->
                    DSCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(DSTheme.spacing.medium)
                        ) {
                            Icon(
                                Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = DSTheme.colors.tertiary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = pattern.description,
                                    style = DSTheme.font.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = stringResource(Res.string.focus_avg_rating, String.format("%.1f", pattern.averagePeakRating)),
                                    style = DSTheme.font.bodySmall,
                                    color = DSTheme.colors.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Recommendations
            if (data.recommendations.isNotEmpty()) {
                item {
                    DSSectionHeader(title = stringResource(Res.string.focus_best_time_for))
                }
                items(data.recommendations.take(5)) { rec ->
                    DSCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(DSTheme.spacing.medium)
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = DSTheme.colors.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = rec.description,
                                    style = DSTheme.font.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    repeat(rec.averageRating.toInt().coerceIn(0, 5)) {
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = null,
                                            tint = DSTheme.colors.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Heatmap
            item {
                DSSectionHeader(title = stringResource(Res.string.focus_weekly_heatmap))
            }
            item {
                FocusHeatmap(data.heatmapData)
            }
        }
    }
}

@Composable
fun FocusHeatmap(data: List<HourlyFocusData>) {
    val dayLabels = listOf(
        stringResource(Res.string.day_mon),
        stringResource(Res.string.day_tue),
        stringResource(Res.string.day_wed),
        stringResource(Res.string.day_thu),
        stringResource(Res.string.day_fri),
        stringResource(Res.string.day_sat),
        stringResource(Res.string.day_sun)
    )
    val hourRange = 6..21 // 6am to 9pm

    // Build lookup map
    val dataMap = data.associateBy { Pair(it.dayOfWeek, it.hour) }

    DSCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            // Hour labels row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Empty cell for day labels
                Box(modifier = Modifier.width(36.dp))
                hourRange.forEach { hour ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (hour % 3 == 0) {
                            Text(
                                text = FocusZonesViewModel.formatHour(hour),
                                style = DSTheme.font.labelSmall,
                                fontSize = 9.sp,
                                color = DSTheme.colors.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Day rows
            dayLabels.forEachIndexed { index, dayLabel ->
                val dayOfWeek = index + 1 // 1=Monday
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Day label
                    Text(
                        text = dayLabel,
                        style = DSTheme.font.labelSmall,
                        modifier = Modifier.width(36.dp),
                        color = DSTheme.colors.onSurfaceVariant
                    )
                    // Hour cells
                    hourRange.forEach { hour ->
                        val cellData = dataMap[Pair(dayOfWeek, hour)]
                        val color = if (cellData != null) {
                            focusRatingColor(cellData.averageRating)
                        } else {
                            DSTheme.colors.surfaceVariant.copy(alpha = 0.3f)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(24.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(color)
                                .border(0.5.dp, DSTheme.colors.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(3.dp))
                        )
                    }
                }
            }

            Spacer(Modifier.height(DSTheme.spacing.small))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.focus_legend_low),
                    style = DSTheme.font.labelSmall,
                    color = DSTheme.colors.onSurfaceVariant
                )
                Spacer(Modifier.width(4.dp))
                listOf(1.0, 2.0, 3.0, 4.0, 5.0).forEach { rating ->
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(focusRatingColor(rating))
                    )
                    Spacer(Modifier.width(2.dp))
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(Res.string.focus_legend_high),
                    style = DSTheme.font.labelSmall,
                    color = DSTheme.colors.onSurfaceVariant
                )
            }
        }
    }
}

private fun focusRatingColor(rating: Double): Color {
    // Interpolate from cool blue (low) to warm orange/red (high focus)
    return when {
        rating <= 1.0 -> Color(0xFFBBDEFB) // Light blue
        rating <= 2.0 -> Color(0xFF81D4FA) // Sky blue
        rating <= 3.0 -> Color(0xFFFFCC80) // Warm amber
        rating <= 4.0 -> Color(0xFFFF8A65) // Orange
        else -> Color(0xFFEF5350)           // Red/hot
    }
}
