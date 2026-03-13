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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.booking.worktracker.data.models.Objective
import com.booking.worktracker.data.models.ObjectiveStatus
import com.booking.worktracker.data.models.ObjectiveType
import com.booking.worktracker.presentation.viewmodels.ObjectivesViewModel
import com.booking.worktracker.ui.designsystem.DSTheme
import com.booking.worktracker.ui.designsystem.components.*
import com.booking.worktracker.core.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObjectivesScreen() {
    val viewModel = viewModel { ObjectivesViewModel() }

    val objectives by viewModel.objectives.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val selectedQuarter by viewModel.selectedQuarter.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingObjective by remember { mutableStateOf<Objective?>(null) }

    val scope = rememberCoroutineScope()

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
            DSScreenTitle(stringResource(Res.string.work_objectives))

            DSButton(
                text = stringResource(Res.string.add_objective),
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
                onClick = { viewModel.setTab(0) },
                text = { Text(stringResource(Res.string.yearly)) },
                icon = { Icon(Icons.Default.CalendarToday, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { viewModel.setTab(1) },
                text = { Text(stringResource(Res.string.quarterly)) },
                icon = { Icon(Icons.Default.DateRange, contentDescription = null) }
            )
        }

        // Period selector
        DSCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DSTheme.spacing.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedTab == 0) stringResource(Res.string.year_label) else stringResource(Res.string.quarter_label),
                    style = DSTheme.font.titleMedium
                )

                if (selectedTab == 0) {
                    // Year selector
                    Row(horizontalArrangement = Arrangement.spacedBy(DSTheme.spacing.small)) {
                        IconButton(onClick = { viewModel.setYear(selectedYear - 1) }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = stringResource(Res.string.previous_year))
                        }
                        Text(
                            text = selectedYear.toString(),
                            style = DSTheme.font.titleLarge,
                            modifier = Modifier.padding(horizontal = DSTheme.spacing.medium)
                        )
                        IconButton(onClick = { viewModel.setYear(selectedYear + 1) }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = stringResource(Res.string.next_year))
                        }
                    }
                } else {
                    // Quarter selector
                    Row(horizontalArrangement = Arrangement.spacedBy(DSTheme.spacing.small)) {
                        IconButton(onClick = {
                            if (selectedQuarter == 1) {
                                viewModel.setQuarter(4)
                                viewModel.setYear(selectedYear - 1)
                            } else {
                                viewModel.setQuarter(selectedQuarter - 1)
                            }
                        }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = stringResource(Res.string.previous_quarter))
                        }
                        Text(
                            text = stringResource(Res.string.quarter_year, selectedQuarter, selectedYear),
                            style = DSTheme.font.titleLarge,
                            modifier = Modifier.padding(horizontal = DSTheme.spacing.medium)
                        )
                        IconButton(onClick = {
                            if (selectedQuarter == 4) {
                                viewModel.setQuarter(1)
                                viewModel.setYear(selectedYear + 1)
                            } else {
                                viewModel.setQuarter(selectedQuarter + 1)
                            }
                        }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = stringResource(Res.string.next_quarter))
                        }
                    }
                }
            }
        }

        // Objectives list
        if (objectives.isEmpty()) {
            DSEmptyState(
                message = stringResource(Res.string.no_objectives_yet),
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.medium),
                modifier = Modifier.weight(1f)
            ) {
                items(objectives) { objective ->
                    ObjectiveCard(
                        objective = objective,
                        viewModel = viewModel,
                        onEdit = {
                            editingObjective = objective
                            showAddDialog = true
                        },
                        onDelete = {
                            scope.launch {
                                viewModel.deleteObjective(objective.id)
                            }
                        },
                        onStatusChange = { newStatus ->
                            scope.launch {
                                viewModel.updateObjective(
                                    id = objective.id,
                                    title = objective.title,
                                    description = objective.description,
                                    status = newStatus
                                )
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
                        viewModel.updateObjective(
                            id = editingObjective!!.id,
                            title = title,
                            description = description,
                            status = editingObjective!!.status
                        )
                    } else {
                        viewModel.createObjective(
                            title = title,
                            description = description,
                            type = if (selectedTab == 0) ObjectiveType.YEARLY else ObjectiveType.QUARTERLY,
                            year = selectedYear,
                            quarter = if (selectedTab == 1) selectedQuarter else null
                        )
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
    viewModel: ObjectivesViewModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStatusChange: (ObjectiveStatus) -> Unit
) {

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
                        horizontalArrangement = Arrangement.spacedBy(DSTheme.spacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Status icon
                        when (objective.status) {
                            ObjectiveStatus.IN_PROGRESS -> Icon(
                                Icons.Default.Schedule,
                                contentDescription = stringResource(Res.string.in_progress),
                                tint = DSTheme.colors.primary
                            )
                            ObjectiveStatus.COMPLETED -> Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = stringResource(Res.string.completed),
                                tint = DSTheme.colors.tertiary
                            )
                            ObjectiveStatus.CANCELLED -> Icon(
                                Icons.Default.Cancel,
                                contentDescription = stringResource(Res.string.cancelled),
                                tint = DSTheme.colors.error
                            )
                        }

                        Text(
                            text = objective.title,
                            style = DSTheme.font.titleMedium
                        )
                    }

                    if (objective.description.isNotBlank()) {
                        Spacer(Modifier.height(DSTheme.spacing.small))
                        Text(
                            text = objective.description,
                            style = DSTheme.font.bodyMedium,
                            color = DSTheme.colors.onSurfaceVariant
                        )
                    }

                    Spacer(Modifier.height(DSTheme.spacing.small))

                    // Progress info and status chip
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(DSTheme.spacing.small),
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
                                        contentDescription = if (expanded) stringResource(Res.string.collapse) else stringResource(Res.string.expand)
                                    )
                                }
                            )
                        }
                    }
                }

                // Action buttons
                Row {
                    IconButton(onClick = { showChecklistDialog = true }) {
                        Icon(Icons.Default.PlaylistAdd, contentDescription = stringResource(Res.string.add_checklist_item))
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(Res.string.action_edit))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(Res.string.action_delete), tint = DSTheme.colors.error)
                    }
                }
            }

            // Checklist items (shown when expanded)
            if (expanded && objective.checklistItems.isNotEmpty()) {
                Spacer(Modifier.height(DSTheme.spacing.medium))
                Divider()
                Spacer(Modifier.height(DSTheme.spacing.small))

                objective.checklistItems.forEach { item ->
                    ChecklistItemRow(
                        item = item,
                        onToggle = {
                            scope.launch {
                                viewModel.toggleChecklistItem(item.id)
                            }
                        },
                        onDelete = {
                            scope.launch {
                                viewModel.deleteChecklistItem(item.id)
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
                    viewModel.addChecklistItem(objective.id, text)
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

    var title by remember { mutableStateOf(objective?.title ?: "") }
    var description by remember { mutableStateOf(objective?.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (objective != null) stringResource(Res.string.edit_objective) else stringResource(Res.string.add_objective))
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.medium)
            ) {
                Text(
                    text = if (type == ObjectiveType.YEARLY) stringResource(Res.string.year_display, year) else stringResource(Res.string.quarter_display, quarter ?: 1, year),
                    style = DSTheme.font.bodyMedium,
                    color = DSTheme.colors.primary
                )

                DSOutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = stringResource(Res.string.objective_title),
                    placeholder = stringResource(Res.string.objective_title_placeholder),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                DSOutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = stringResource(Res.string.description_optional),
                    placeholder = stringResource(Res.string.description_placeholder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(DSTheme.spacing.space16 * 2),
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            DSButton(
                text = if (objective != null) stringResource(Res.string.action_update) else stringResource(Res.string.action_add),
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
                text = stringResource(Res.string.action_cancel),
                onClick = onDismiss
            )
        }
    )
}
