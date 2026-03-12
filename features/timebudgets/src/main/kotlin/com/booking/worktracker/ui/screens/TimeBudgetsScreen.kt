package com.booking.worktracker.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import com.booking.worktracker.data.models.BudgetProgress
import com.booking.worktracker.data.models.BudgetStatus
import com.booking.worktracker.data.models.PeriodType
import com.booking.worktracker.presentation.viewmodels.TimeBudgetsViewModel
import com.booking.worktracker.ui.designsystem.DSTheme
import com.booking.worktracker.ui.designsystem.components.*

@Composable
fun TimeBudgetsScreen() {
    val viewModel = remember { TimeBudgetsViewModel() }
    val budgetProgress by viewModel.budgetProgress.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val activeObjectives by viewModel.activeObjectives.collectAsState()
    val periodElapsed by viewModel.periodElapsedFraction.collectAsState()
    val message by viewModel.message.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingBudget by remember { mutableStateOf<BudgetProgress?>(null) }

    LaunchedEffect(message) {
        if (message != null) {
            kotlinx.coroutines.delay(2000)
            viewModel.clearMessage()
        }
    }

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
            DSScreenTitle("Time Budgets")
            DSButton(
                text = "Add Budget",
                icon = Icons.Default.Add,
                onClick = { showAddDialog = true }
            )
        }

        // Message banner
        message?.let { msg ->
            DSInfoBanner(
                title = "Status",
                message = msg,
                icon = { Icon(Icons.Default.Info, contentDescription = null) }
            )
        }

        // Pace indicator card
        if (budgetProgress.isNotEmpty()) {
            PaceIndicatorCard(budgetProgress, periodElapsed, viewModel)
        }

        // Budget list
        if (budgetProgress.isEmpty()) {
            DSEmptyState(
                message = "No time budgets set. Add a budget to start tracking your time allocation goals.",
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.small),
                modifier = Modifier.weight(1f)
            ) {
                items(budgetProgress) { progress ->
                    BudgetProgressCard(
                        progress = progress,
                        periodElapsed = periodElapsed,
                        viewModel = viewModel,
                        onEdit = { editingBudget = progress },
                        onDelete = { viewModel.removeBudget(progress.budget.id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        BudgetDialog(
            categories = categories,
            activeObjectives = activeObjectives,
            onDismiss = { showAddDialog = false },
            onSave = { category, targetMinutes, periodType, objectiveId ->
                viewModel.addBudget(category, targetMinutes, periodType, objectiveId)
                showAddDialog = false
            }
        )
    }

    editingBudget?.let { progress ->
        BudgetDialog(
            categories = categories,
            activeObjectives = activeObjectives,
            existingBudget = progress,
            onDismiss = { editingBudget = null },
            onSave = { category, targetMinutes, periodType, objectiveId ->
                viewModel.editBudget(progress.budget.id, category, targetMinutes, periodType, objectiveId)
                editingBudget = null
            }
        )
    }
}

@Composable
private fun PaceIndicatorCard(
    budgetProgress: List<BudgetProgress>,
    periodElapsed: Float,
    viewModel: TimeBudgetsViewModel
) {
    val totalTarget = budgetProgress.sumOf { it.budget.targetMinutes }
    val totalActual = budgetProgress.sumOf { it.actualMinutes }
    val expectedByNow = (totalTarget * periodElapsed).toInt()
    val paceDirection = when {
        totalActual >= expectedByNow -> "ahead"
        totalActual >= expectedByNow * 0.8f -> "on pace"
        else -> "behind"
    }
    val paceColor = when (paceDirection) {
        "ahead" -> DSTheme.colors.tertiary
        "on pace" -> DSTheme.colors.secondary
        else -> DSTheme.colors.error
    }
    val paceIcon = when (paceDirection) {
        "ahead" -> Icons.Default.TrendingUp
        "on pace" -> Icons.Default.TrendingFlat
        else -> Icons.Default.TrendingDown
    }

    DSCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.small)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DSSectionHeader(title = "Weekly Pace")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(DSTheme.spacing.extraSmall),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(paceIcon, contentDescription = null, tint = paceColor, modifier = Modifier.size(20.dp))
                    Text(
                        text = "You're $paceDirection",
                        style = DSTheme.font.titleSmall,
                        color = paceColor
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Logged", style = DSTheme.font.bodySmall, color = DSTheme.colors.onSurfaceVariant)
                    Text(viewModel.formatMinutes(totalActual), style = DSTheme.font.titleMedium)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Expected by now", style = DSTheme.font.bodySmall, color = DSTheme.colors.onSurfaceVariant)
                    Text(viewModel.formatMinutes(expectedByNow), style = DSTheme.font.titleMedium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total target", style = DSTheme.font.bodySmall, color = DSTheme.colors.onSurfaceVariant)
                    Text(viewModel.formatMinutes(totalTarget), style = DSTheme.font.titleMedium)
                }
            }

            // Period progress bar
            val periodPercent = (periodElapsed * 100).toInt()
            Text(
                text = "Period: $periodPercent% elapsed",
                style = DSTheme.font.bodySmall,
                color = DSTheme.colors.onSurfaceVariant
            )
            LinearProgressIndicator(
                progress = { periodElapsed.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = DSTheme.colors.outlineVariant,
                trackColor = DSTheme.colors.surfaceVariant
            )
        }
    }
}

@Composable
private fun BudgetProgressCard(
    progress: BudgetProgress,
    periodElapsed: Float,
    viewModel: TimeBudgetsViewModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (progress.status) {
        BudgetStatus.ON_TRACK -> DSTheme.colors.tertiary
        BudgetStatus.AT_RISK -> DSTheme.colors.secondary
        BudgetStatus.BEHIND -> DSTheme.colors.error
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress.percentage.coerceIn(0f, 1f)
    )

    val expectedMinutes = (progress.budget.targetMinutes * periodElapsed).toInt()
    val paceText = when {
        progress.actualMinutes >= expectedMinutes -> "Ahead of pace"
        progress.actualMinutes >= expectedMinutes * 0.8f -> "On pace"
        else -> "Behind pace"
    }

    DSCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.small)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = progress.budget.category,
                        style = DSTheme.font.titleMedium
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(DSTheme.spacing.small)) {
                        Text(
                            text = progress.budget.periodType.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = DSTheme.font.bodySmall,
                            color = DSTheme.colors.onSurfaceVariant
                        )
                        progress.objectiveTitle?.let { title ->
                            Text(
                                text = "| $title",
                                style = DSTheme.font.bodySmall,
                                color = DSTheme.colors.primary
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(DSTheme.spacing.extraSmall)) {
                    DSIconButton(
                        icon = Icons.Default.Edit,
                        contentDescription = "Edit",
                        onClick = onEdit
                    )
                    DSIconButton(
                        icon = Icons.Default.Delete,
                        contentDescription = "Delete",
                        onClick = onDelete
                    )
                }
            }

            // Progress bar
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${progress.formattedActual()} / ${progress.budget.formattedTarget()}",
                        style = DSTheme.font.bodyMedium
                    )
                    Text(
                        text = "${(progress.percentage * 100).toInt()}%",
                        style = DSTheme.font.bodyMedium,
                        color = statusColor
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(DSTheme.colors.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedProgress)
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusColor)
                    )
                    // Pace marker line
                    if (periodElapsed in 0.01f..0.99f) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .offset(x = (periodElapsed * 300).dp.coerceAtMost(296.dp))
                                .width(2.dp)
                                .background(DSTheme.colors.onSurface.copy(alpha = 0.5f))
                        )
                    }
                }

                Text(
                    text = paceText,
                    style = DSTheme.font.bodySmall,
                    color = statusColor
                )
            }
        }
    }
}

@Composable
private fun BudgetDialog(
    categories: List<String>,
    activeObjectives: List<Pair<Int, String>>,
    existingBudget: BudgetProgress? = null,
    onDismiss: () -> Unit,
    onSave: (String, Int, PeriodType, Int?) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(existingBudget?.budget?.category ?: categories.firstOrNull() ?: "General") }
    var targetHours by remember { mutableStateOf(existingBudget?.let { (it.budget.targetMinutes / 60).toString() } ?: "10") }
    var targetMinutesRemainder by remember { mutableStateOf(existingBudget?.let { (it.budget.targetMinutes % 60).toString() } ?: "0") }
    var selectedPeriod by remember { mutableStateOf(existingBudget?.budget?.periodType ?: PeriodType.WEEKLY) }
    var selectedObjectiveId by remember { mutableStateOf(existingBudget?.budget?.objectiveId) }

    var categoryExpanded by remember { mutableStateOf(false) }
    var periodExpanded by remember { mutableStateOf(false) }
    var objectiveExpanded by remember { mutableStateOf(false) }

    val title = if (existingBudget != null) "Edit Budget" else "Add Budget"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.medium)) {
                // Category dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = { selectedCategory = it },
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Target hours and minutes
                Row(horizontalArrangement = Arrangement.spacedBy(DSTheme.spacing.small)) {
                    DSOutlinedTextField(
                        value = targetHours,
                        onValueChange = { targetHours = it.filter { c -> c.isDigit() } },
                        label = "Hours",
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    DSOutlinedTextField(
                        value = targetMinutesRemainder,
                        onValueChange = { targetMinutesRemainder = it.filter { c -> c.isDigit() } },
                        label = "Minutes",
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Period type dropdown
                ExposedDropdownMenuBox(
                    expanded = periodExpanded,
                    onExpandedChange = { periodExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedPeriod.name.lowercase().replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Period") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = periodExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = periodExpanded,
                        onDismissRequest = { periodExpanded = false }
                    ) {
                        PeriodType.entries.forEach { period ->
                            DropdownMenuItem(
                                text = { Text(period.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    selectedPeriod = period
                                    periodExpanded = false
                                }
                            )
                        }
                    }
                }

                // Optional objective link
                ExposedDropdownMenuBox(
                    expanded = objectiveExpanded,
                    onExpandedChange = { objectiveExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedObjectiveId?.let { id ->
                            activeObjectives.find { it.first == id }?.second ?: "Unknown"
                        } ?: "None",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Link to Objective (optional)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = objectiveExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = objectiveExpanded,
                        onDismissRequest = { objectiveExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("None") },
                            onClick = {
                                selectedObjectiveId = null
                                objectiveExpanded = false
                            }
                        )
                        activeObjectives.forEach { (id, title) ->
                            DropdownMenuItem(
                                text = { Text(title) },
                                onClick = {
                                    selectedObjectiveId = id
                                    objectiveExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            DSButton(
                text = "Save",
                onClick = {
                    val hours = targetHours.toIntOrNull() ?: 0
                    val mins = targetMinutesRemainder.toIntOrNull() ?: 0
                    val totalMinutes = hours * 60 + mins
                    if (totalMinutes > 0 && selectedCategory.isNotBlank()) {
                        onSave(selectedCategory, totalMinutes, selectedPeriod, selectedObjectiveId)
                    }
                },
                enabled = selectedCategory.isNotBlank() && ((targetHours.toIntOrNull() ?: 0) > 0 || (targetMinutesRemainder.toIntOrNull() ?: 0) > 0)
            )
        },
        dismissButton = {
            DSTextButton(text = "Cancel", onClick = onDismiss)
        }
    )
}
