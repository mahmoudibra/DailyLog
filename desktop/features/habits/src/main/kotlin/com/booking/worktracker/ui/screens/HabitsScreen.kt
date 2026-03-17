package com.booking.worktracker.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.booking.worktracker.core.generated.resources.*
import com.booking.worktracker.data.models.Habit
import com.booking.worktracker.data.models.HabitStreak
import com.booking.worktracker.di.HabitsComponent
import com.booking.worktracker.ui.designsystem.DSTheme
import com.booking.worktracker.ui.designsystem.components.*
import kotlinx.datetime.*
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

private val presetColors = listOf(
    "#4CAF50", "#2196F3", "#FF9800", "#E91E63",
    "#9C27B0", "#00BCD4", "#FF5722", "#607D8B"
)

@Composable
fun HabitsScreen() {
    val viewModel = viewModel { HabitsComponent.instance.habitsViewModel }
    val habitStreaks by viewModel.habitStreaks.collectAsState()
    val completedToday by viewModel.completedToday.collectAsState()
    val weeklyScore by viewModel.weeklyScore.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingHabit by remember { mutableStateOf<Habit?>(null) }

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
                DSScreenTitle(stringResource(Res.string.habits_title))
                DSButton(
                    text = stringResource(Res.string.habits_add),
                    icon = Icons.Default.Add,
                    onClick = { showAddDialog = true }
                )
            }
        }

        // Weekly Score Card
        item {
            weeklyScore?.let { score ->
                WeeklyScoreCard(score)
            }
        }

        // Today's Habits Section
        item {
            DSSectionHeader(title = stringResource(Res.string.habits_today))
        }

        if (habitStreaks.isEmpty() && !isLoading) {
            item {
                DSEmptyState(
                    message = stringResource(Res.string.habits_empty)
                )
            }
        } else {
            items(habitStreaks, key = { it.habit.id }) { streak ->
                TodayHabitCard(
                    streak = streak,
                    isCompleted = completedToday.contains(streak.habit.id),
                    onToggle = { viewModel.toggleHabitCompletion(streak.habit.id) },
                    onEdit = { editingHabit = streak.habit },
                    onArchive = { viewModel.archiveHabit(streak.habit.id) }
                )
            }
        }

        // Streak Board Section
        if (habitStreaks.isNotEmpty()) {
            item {
                DSSectionHeader(title = stringResource(Res.string.habits_streak_board))
            }

            items(habitStreaks, key = { "streak-${it.habit.id}" }) { streak ->
                StreakBoardCard(streak = streak)
            }
        }
    }

    if (showAddDialog) {
        HabitDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, icon, color ->
                viewModel.createHabit(name, icon, color, null)
                showAddDialog = false
            }
        )
    }

    editingHabit?.let { habit ->
        HabitDialog(
            existingHabit = habit,
            onDismiss = { editingHabit = null },
            onSave = { name, icon, color ->
                viewModel.updateHabit(habit.id, name, icon, color, habit.objectiveId)
                editingHabit = null
            }
        )
    }
}

@Composable
private fun WeeklyScoreCard(score: com.booking.worktracker.data.models.HabitWeeklyScore) {
    DSCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.small)) {
            DSSectionHeader(title = stringResource(Res.string.habits_this_week))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(Res.string.habits_completion),
                        style = DSTheme.font.bodySmall,
                        color = DSTheme.colors.onSurfaceVariant
                    )
                    Text(
                        text = "${(score.completionRate * 100).toInt()}%",
                        style = DSTheme.font.titleMedium
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(Res.string.habits_perfect_days),
                        style = DSTheme.font.bodySmall,
                        color = DSTheme.colors.onSurfaceVariant
                    )
                    Text(
                        text = "${score.perfectDays}",
                        style = DSTheme.font.titleMedium
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(Res.string.habits_completions),
                        style = DSTheme.font.bodySmall,
                        color = DSTheme.colors.onSurfaceVariant
                    )
                    Text(
                        text = "${score.totalHabits} / ${score.totalPossible}",
                        style = DSTheme.font.titleMedium
                    )
                }
            }

            LinearProgressIndicator(
                progress = { score.completionRate.toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = DSTheme.colors.primary,
                trackColor = DSTheme.colors.surfaceVariant
            )
        }
    }
}

@Composable
private fun TodayHabitCard(
    streak: HabitStreak,
    isCompleted: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onArchive: () -> Unit
) {
    val habit = streak.habit
    val checkColor by animateColorAsState(
        targetValue = if (isCompleted) DSTheme.colors.primary else DSTheme.colors.surfaceVariant
    )

    DSCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: icon and name
            Row(
                horizontalArrangement = Arrangement.spacedBy(DSTheme.spacing.small),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = habit.icon ?: "\u2b50",
                    style = DSTheme.font.titleMedium
                )
                Column {
                    Text(
                        text = habit.name,
                        style = DSTheme.font.titleSmall
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(DSTheme.spacing.small)
                    ) {
                        if (streak.currentStreak > 0) {
                            val dayText = pluralStringResource(Res.plurals.habits_day_streak, streak.currentStreak, streak.currentStreak)
                            Text(
                                text = stringResource(Res.string.habits_streak_fire, dayText),
                                style = DSTheme.font.bodySmall,
                                color = DSTheme.colors.primary
                            )
                        }
                        Text(
                            text = stringResource(Res.string.habits_best, streak.longestStreak),
                            style = DSTheme.font.bodySmall,
                            color = DSTheme.colors.onSurfaceVariant
                        )
                    }
                }
            }

            // Actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(DSTheme.spacing.extraSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DSIconButton(
                    icon = Icons.Default.Edit,
                    contentDescription = stringResource(Res.string.action_edit),
                    onClick = onEdit
                )
                DSIconButton(
                    icon = Icons.Default.Archive,
                    contentDescription = stringResource(Res.string.habits_archive),
                    onClick = onArchive
                )

                // Completion toggle
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(checkColor)
                        .border(2.dp, DSTheme.colors.outline, CircleShape)
                        .clickable { onToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = stringResource(Res.string.completed),
                            tint = DSTheme.colors.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StreakBoardCard(streak: HabitStreak) {
    val habit = streak.habit
    val habitColor = habit.color?.let { parseHexColor(it) } ?: DSTheme.colors.primary

    DSCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.small)) {
            // Habit name and icon
            Row(
                horizontalArrangement = Arrangement.spacedBy(DSTheme.spacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = habit.icon ?: "\u2b50",
                    style = DSTheme.font.titleMedium
                )
                Text(
                    text = habit.name,
                    style = DSTheme.font.titleSmall
                )
            }

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(Res.string.habits_current),
                        style = DSTheme.font.bodySmall,
                        color = DSTheme.colors.onSurfaceVariant
                    )
                    Text(
                        text = pluralStringResource(Res.plurals.habits_day_streak, streak.currentStreak, streak.currentStreak),
                        style = DSTheme.font.titleSmall
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(Res.string.habits_longest),
                        style = DSTheme.font.bodySmall,
                        color = DSTheme.colors.onSurfaceVariant
                    )
                    Text(
                        text = pluralStringResource(Res.plurals.habits_day_streak, streak.longestStreak, streak.longestStreak),
                        style = DSTheme.font.titleSmall
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(Res.string.total),
                        style = DSTheme.font.bodySmall,
                        color = DSTheme.colors.onSurfaceVariant
                    )
                    Text(
                        text = "${streak.totalCompletions}",
                        style = DSTheme.font.titleSmall
                    )
                }
            }

            // Mini heatmap: 4 rows x 7 columns
            MiniHeatmap(
                currentStreak = streak.currentStreak,
                color = habitColor
            )
        }
    }
}

@Composable
private fun MiniHeatmap(
    currentStreak: Int,
    color: Color
) {
    val totalDays = 28
    val completedColor = color
    val emptyColor = DSTheme.colors.surfaceVariant

    val dayLabels = listOf(
        stringResource(Res.string.day_mon).first().toString(),
        stringResource(Res.string.day_tue).first().toString(),
        stringResource(Res.string.day_wed).first().toString(),
        stringResource(Res.string.day_thu).first().toString(),
        stringResource(Res.string.day_fri).first().toString(),
        stringResource(Res.string.day_sat).first().toString(),
        stringResource(Res.string.day_sun).first().toString(),
    )

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        // Day of week labels
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            dayLabels.forEach { label ->
                Text(
                    text = label,
                    style = DSTheme.font.bodySmall,
                    color = DSTheme.colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.size(8.dp)
                )
            }
        }

        // 4 rows x 7 columns, most recent day at bottom-right
        for (row in 0 until 4) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                for (col in 0 until 7) {
                    val dayIndex = row * 7 + col
                    val daysAgo = totalDays - 1 - dayIndex
                    val isCompleted = daysAgo < currentStreak

                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(if (isCompleted) completedColor else emptyColor)
                    )
                }
            }
        }
    }
}

@Composable
private fun HabitDialog(
    existingHabit: Habit? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, icon: String?, color: String?) -> Unit
) {
    var name by remember { mutableStateOf(existingHabit?.name ?: "") }
    var icon by remember { mutableStateOf(existingHabit?.icon ?: "") }
    var selectedColor by remember { mutableStateOf(existingHabit?.color) }

    val title = if (existingHabit != null) stringResource(Res.string.habits_edit) else stringResource(Res.string.habits_add)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.medium)) {
                DSOutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = stringResource(Res.string.habits_name_label),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                DSOutlinedTextField(
                    value = icon,
                    onValueChange = { icon = it },
                    label = stringResource(Res.string.habits_icon_label),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Color picker
                Text(
                    text = stringResource(Res.string.habits_color_label),
                    style = DSTheme.font.bodyMedium,
                    color = DSTheme.colors.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(DSTheme.spacing.small),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    presetColors.forEach { colorHex ->
                        val colorVal = parseHexColor(colorHex)
                        val isSelected = selectedColor == colorHex
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(colorVal)
                                .then(
                                    if (isSelected) Modifier.border(
                                        2.dp,
                                        DSTheme.colors.onSurface,
                                        CircleShape
                                    ) else Modifier
                                )
                                .clickable {
                                    selectedColor = if (isSelected) null else colorHex
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = stringResource(Res.string.habits_selected),
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            DSButton(
                text = stringResource(Res.string.action_save),
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            name.trim(),
                            icon.trim().ifEmpty { null },
                            selectedColor
                        )
                    }
                },
                enabled = name.isNotBlank()
            )
        },
        dismissButton = {
            DSTextButton(text = stringResource(Res.string.action_cancel), onClick = onDismiss)
        }
    )
}

private fun parseHexColor(hex: String): Color {
    val cleanHex = hex.removePrefix("#")
    return try {
        Color(("FF$cleanHex").toLong(16))
    } catch (_: Exception) {
        Color.Gray
    }
}
