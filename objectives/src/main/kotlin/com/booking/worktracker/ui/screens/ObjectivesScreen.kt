package com.booking.worktracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.booking.worktracker.data.models.Objective
import com.booking.worktracker.data.models.ObjectiveStatus
import com.booking.worktracker.data.models.ObjectiveType
import com.booking.worktracker.data.repository.ObjectiveRepository
import com.booking.worktracker.ui.designsystem.components.*
import com.booking.worktracker.ui.designsystem.tokens.SpacingTokens
import com.booking.worktracker.ui.localization.LocalStrings
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObjectivesScreen(
    objectiveRepository: ObjectiveRepository
) {
    val strings = LocalStrings.current
    var selectedTab by remember { mutableStateOf(0) }
    var objectives by remember { mutableStateOf<List<Objective>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingObjective by remember { mutableStateOf<Objective?>(null) }
    var selectedYear by remember {
        mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year)
    }
    var selectedQuarter by remember {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        mutableStateOf((now.monthNumber - 1) / 3 + 1)
    }

    val scope = rememberCoroutineScope()

    // Load objectives based on selected tab
    LaunchedEffect(selectedTab, selectedYear, selectedQuarter) {
        objectives = when (selectedTab) {
            0 -> objectiveRepository.getYearlyObjectives(selectedYear)
            1 -> objectiveRepository.getQuarterlyObjectives(selectedYear, selectedQuarter)
            else -> emptyList()
        }
    }

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
            DSScreenTitle(strings.workObjectives)

            DSButton(
                text = strings.addObjective,
                icon = Icons.Default.Add,
                onClick = {
                    editingObjective = null
                    showAddDialog = true
                }
            )
        }

        // Tab selector
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(strings.yearly) },
                icon = { Icon(Icons.Default.CalendarToday, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(strings.quarterly) },
                icon = { Icon(Icons.Default.DateRange, contentDescription = null) }
            )
        }

        // Period selector
        DSCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedTab == 0) strings.yearLabel else strings.quarterLabel,
                    style = MaterialTheme.typography.titleMedium
                )

                if (selectedTab == 0) {
                    // Year selector
                    Row(horizontalArrangement = Arrangement.spacedBy(SpacingTokens.small)) {
                        IconButton(onClick = { selectedYear-- }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = strings.previousYear)
                        }
                        Text(
                            text = selectedYear.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(horizontal = SpacingTokens.medium)
                        )
                        IconButton(onClick = { selectedYear++ }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = strings.nextYear)
                        }
                    }
                } else {
                    // Quarter selector
                    Row(horizontalArrangement = Arrangement.spacedBy(SpacingTokens.small)) {
                        IconButton(onClick = {
                            if (selectedQuarter == 1) {
                                selectedQuarter = 4
                                selectedYear--
                            } else {
                                selectedQuarter--
                            }
                        }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = strings.previousQuarter)
                        }
                        Text(
                            text = strings.quarterYear(selectedQuarter, selectedYear),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(horizontal = SpacingTokens.medium)
                        )
                        IconButton(onClick = {
                            if (selectedQuarter == 4) {
                                selectedQuarter = 1
                                selectedYear++
                            } else {
                                selectedQuarter++
                            }
                        }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = strings.nextQuarter)
                        }
                    }
                }
            }
        }

        // Objectives list
        if (objectives.isEmpty()) {
            DSEmptyState(
                message = strings.noObjectivesYet,
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.medium),
                modifier = Modifier.weight(1f)
            ) {
                items(objectives) { objective ->
                    ObjectiveCard(
                        objective = objective,
                        objectiveRepository = objectiveRepository,
                        onEdit = {
                            editingObjective = objective
                            showAddDialog = true
                        },
                        onDelete = {
                            scope.launch {
                                objectiveRepository.deleteObjective(objective.id)
                                objectives = objectives.filter { it.id != objective.id }
                            }
                        },
                        onStatusChange = { newStatus ->
                            scope.launch {
                                objectiveRepository.updateObjective(
                                    id = objective.id,
                                    title = objective.title,
                                    description = objective.description,
                                    status = newStatus
                                )
                                // Reload objectives
                                objectives = when (selectedTab) {
                                    0 -> objectiveRepository.getYearlyObjectives(selectedYear)
                                    1 -> objectiveRepository.getQuarterlyObjectives(selectedYear, selectedQuarter)
                                    else -> emptyList()
                                }
                            }
                        },
                        onChecklistChanged = {
                            // Reload to show updated checklist
                            scope.launch {
                                objectives = when (selectedTab) {
                                    0 -> objectiveRepository.getYearlyObjectives(selectedYear)
                                    1 -> objectiveRepository.getQuarterlyObjectives(selectedYear, selectedQuarter)
                                    else -> emptyList()
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    // Add/Edit objective dialog
    if (showAddDialog) {
        ObjectiveDialog(
            objective = editingObjective,
            type = if (selectedTab == 0) ObjectiveType.YEARLY else ObjectiveType.QUARTERLY,
            year = selectedYear,
            quarter = if (selectedTab == 1) selectedQuarter else null,
            onDismiss = { showAddDialog = false },
            onSave = { title, description ->
                scope.launch {
                    if (editingObjective != null) {
                        // Update existing
                        objectiveRepository.updateObjective(
                            id = editingObjective!!.id,
                            title = title,
                            description = description,
                            status = editingObjective!!.status
                        )
                    } else {
                        // Create new
                        objectiveRepository.createObjective(
                            title = title,
                            description = description,
                            type = if (selectedTab == 0) ObjectiveType.YEARLY else ObjectiveType.QUARTERLY,
                            year = selectedYear,
                            quarter = if (selectedTab == 1) selectedQuarter else null
                        )
                    }

                    // Reload
                    objectives = when (selectedTab) {
                        0 -> objectiveRepository.getYearlyObjectives(selectedYear)
                        1 -> objectiveRepository.getQuarterlyObjectives(selectedYear, selectedQuarter)
                        else -> emptyList()
                    }
                    showAddDialog = false
                }
            }
        )
    }
}

@Composable
fun ObjectiveCard(
    objective: Objective,
    objectiveRepository: ObjectiveRepository,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStatusChange: (ObjectiveStatus) -> Unit,
    onChecklistChanged: () -> Unit
) {
    val strings = LocalStrings.current
    var showMenu by remember { mutableStateOf(false) }
    var showChecklistDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    DSCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Status icon
                        when (objective.status) {
                            ObjectiveStatus.IN_PROGRESS -> Icon(
                                Icons.Default.Schedule,
                                contentDescription = strings.inProgress,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            ObjectiveStatus.COMPLETED -> Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = strings.completed,
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            ObjectiveStatus.CANCELLED -> Icon(
                                Icons.Default.Cancel,
                                contentDescription = strings.cancelled,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }

                        Text(
                            text = objective.title,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    if (objective.description.isNotBlank()) {
                        Spacer(Modifier.height(SpacingTokens.small))
                        Text(
                            text = objective.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(Modifier.height(SpacingTokens.small))

                    // Progress info and status chip
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box {
                            FilterChip(
                                selected = false,
                                onClick = { showMenu = true },
                                label = { Text(objective.status.name.replace("_", " ")) }
                            )

                            // Status change menu
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                ObjectiveStatus.values().forEach { status ->
                                    DropdownMenuItem(
                                        text = { Text(status.name.replace("_", " ")) },
                                        onClick = {
                                            onStatusChange(status)
                                            showMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        if (objective.checklistItems.isNotEmpty()) {
                            FilterChip(
                                selected = false,
                                onClick = { expanded = !expanded },
                                label = { Text("${objective.checklistProgress()} \u2022 ${objective.completionPercentage()}%") },
                                leadingIcon = {
                                    Icon(
                                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = if (expanded) strings.collapse else strings.expand
                                    )
                                }
                            )
                        }
                    }
                }

                // Action buttons
                Row {
                    IconButton(onClick = { showChecklistDialog = true }) {
                        Icon(Icons.Default.PlaylistAdd, contentDescription = strings.addChecklistItem)
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = strings.edit)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = strings.delete, tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // Checklist items (shown when expanded)
            if (expanded && objective.checklistItems.isNotEmpty()) {
                Spacer(Modifier.height(SpacingTokens.medium))
                Divider()
                Spacer(Modifier.height(SpacingTokens.small))

                objective.checklistItems.forEach { item ->
                    ChecklistItemRow(
                        item = item,
                        onToggle = {
                            scope.launch {
                                objectiveRepository.toggleChecklistItem(item.id)
                                onChecklistChanged()
                            }
                        },
                        onDelete = {
                            scope.launch {
                                objectiveRepository.deleteChecklistItem(item.id)
                                onChecklistChanged()
                            }
                        }
                    )
                }
            }
        }

    }

    // Add checklist item dialog
    if (showChecklistDialog) {
        AddChecklistItemDialog(
            onDismiss = { showChecklistDialog = false },
            onAdd = { text ->
                scope.launch {
                    objectiveRepository.addChecklistItem(objective.id, text)
                    onChecklistChanged()
                    showChecklistDialog = false
                }
            }
        )
    }
}

@Composable
fun ObjectiveDialog(
    objective: Objective?,
    type: ObjectiveType,
    year: Int,
    quarter: Int?,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    val strings = LocalStrings.current
    var title by remember { mutableStateOf(objective?.title ?: "") }
    var description by remember { mutableStateOf(objective?.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (objective != null) strings.editObjective else strings.addObjective)
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.medium)
            ) {
                Text(
                    text = if (type == ObjectiveType.YEARLY) strings.yearDisplay(year) else strings.quarterDisplay(quarter ?: 1, year),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                DSOutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = strings.objectiveTitle,
                    placeholder = strings.objectiveTitlePlaceholder,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                DSOutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = strings.descriptionOptional,
                    placeholder = strings.descriptionPlaceholder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SpacingTokens.space16 * 2),
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            DSButton(
                text = if (objective != null) strings.update else strings.add,
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title, description)
                    }
                },
                enabled = title.isNotBlank()
            )
        },
        dismissButton = {
            DSTextButton(
                text = strings.cancel,
                onClick = onDismiss
            )
        }
    )
}
