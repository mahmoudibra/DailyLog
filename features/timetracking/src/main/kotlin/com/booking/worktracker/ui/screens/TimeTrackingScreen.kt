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
import com.booking.worktracker.core.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import com.booking.worktracker.data.models.TimeEntry
import com.booking.worktracker.data.repository.TimeEntryRepository
import com.booking.worktracker.presentation.viewmodels.TimeTrackingViewModel
import com.booking.worktracker.ui.designsystem.components.*
import com.booking.worktracker.ui.designsystem.tokens.SpacingTokens
import kotlinx.coroutines.launch
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeTrackingScreen(
    timeEntryRepository: TimeEntryRepository
) {
    val viewModel = remember { TimeTrackingViewModel(timeEntryRepository) }
    val entries by viewModel.entries.collectAsState()
    val runningEntry by viewModel.runningEntry.collectAsState()
    val totalMinutes by viewModel.totalMinutes.collectAsState()
    val categoryBreakdown by viewModel.categoryBreakdown.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val message by viewModel.message.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showManualDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Show snackbar for messages
    LaunchedEffect(message) {
        if (message != null) {
            kotlinx.coroutines.delay(2000)
            viewModel.clearMessage()
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
            DSScreenTitle(stringResource(Res.string.time_tracking))

            Row(horizontalArrangement = Arrangement.spacedBy(SpacingTokens.small)) {
                if (runningEntry == null) {
                    DSButton(
                        text = stringResource(Res.string.start_timer),
                        icon = Icons.Default.PlayArrow,
                        onClick = { showAddDialog = true }
                    )
                } else {
                    DSButton(
                        text = stringResource(Res.string.stop_timer),
                        icon = Icons.Default.Stop,
                        onClick = { viewModel.stopTimer() }
                    )
                }
                DSOutlinedButton(
                    text = stringResource(Res.string.add_manual),
                    icon = Icons.Default.Add,
                    onClick = { showManualDialog = true }
                )
            }
        }

        // Message banner
        message?.let { msg ->
            DSInfoBanner(
                title = stringResource(Res.string.status),
                message = msg,
                icon = { Icon(Icons.Default.Info, contentDescription = null) }
            )
        }

        // Running timer card
        runningEntry?.let { running ->
            DSCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(Res.string.timer_running),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = running.description,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = stringResource(Res.string.started_at, running.startTime, running.category),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DSButton(
                        text = stringResource(Res.string.action_stop),
                        icon = Icons.Default.Stop,
                        onClick = { viewModel.stopTimer() }
                    )
                }
            }
        }

        // Summary card
        DSCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.small)) {
                DSSectionHeader(title = stringResource(Res.string.todays_summary))
                Text(
                    text = stringResource(Res.string.total_time, viewModel.formatTotalTime(totalMinutes)),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (categoryBreakdown.isNotEmpty()) {
                    DSDivider()
                    categoryBreakdown.forEach { (category, minutes) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = category, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = viewModel.formatTotalTime(minutes),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Entries list
        DSSectionHeader(title = stringResource(Res.string.time_entries))

        if (entries.isEmpty()) {
            DSEmptyState(
                message = stringResource(Res.string.no_time_entries),
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.small),
                modifier = Modifier.weight(1f)
            ) {
                items(entries) { entry ->
                    TimeEntryCard(
                        entry = entry,
                        onDelete = { viewModel.deleteEntry(entry.id) }
                    )
                }
            }
        }
    }

    // Start timer dialog
    if (showAddDialog) {
        StartTimerDialog(
            categories = categories,
            onDismiss = { showAddDialog = false },
            onStart = { description, category ->
                viewModel.startTimer(description, category)
                showAddDialog = false
            }
        )
    }

    // Manual entry dialog
    if (showManualDialog) {
        ManualEntryDialog(
            categories = categories,
            onDismiss = { showManualDialog = false },
            onAdd = { description, category, startTime, endTime ->
                viewModel.addManualEntry(description, category, startTime, endTime)
                showManualDialog = false
            }
        )
    }
}

@Composable
fun TimeEntryCard(
    entry: TimeEntry,
    onDelete: () -> Unit
) {
    DSCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.description,
                    style = MaterialTheme.typography.bodyLarge
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.medium)
                ) {
                    Text(
                        text = if (entry.endTime != null) stringResource(Res.string.time_range_ended, entry.startTime, entry.endTime) else stringResource(Res.string.time_range_running, entry.startTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = entry.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (!entry.isRunning) {
                        Text(
                            text = entry.formattedDuration(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
            if (!entry.isRunning) {
                DSIconButton(
                    icon = Icons.Default.Delete,
                    contentDescription = stringResource(Res.string.action_delete),
                    onClick = onDelete
                )
            }
        }
    }
}

@Composable
fun StartTimerDialog(
    categories: List<String>,
    onDismiss: () -> Unit,
    onStart: (String, String) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: "General") }
    var categoryExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.start_timer)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.medium)) {
                DSOutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = stringResource(Res.string.what_are_you_working_on),
                    placeholder = stringResource(Res.string.working_on_placeholder),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(Res.string.category)) },
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
            }
        },
        confirmButton = {
            DSButton(
                text = stringResource(Res.string.action_start),
                onClick = { if (description.isNotBlank()) onStart(description, selectedCategory) },
                enabled = description.isNotBlank()
            )
        },
        dismissButton = {
            DSTextButton(text = stringResource(Res.string.action_cancel), onClick = onDismiss)
        }
    )
}

@Composable
fun ManualEntryDialog(
    categories: List<String>,
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: "General") }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("10:00") }
    var categoryExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.add_manual_entry)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.medium)) {
                DSOutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = stringResource(Res.string.description),
                    placeholder = stringResource(Res.string.what_did_you_work_on),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(Res.string.category)) },
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

                Row(horizontalArrangement = Arrangement.spacedBy(SpacingTokens.medium)) {
                    DSOutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = stringResource(Res.string.start_time),
                        placeholder = stringResource(Res.string.time_format_placeholder),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    DSOutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = stringResource(Res.string.end_time),
                        placeholder = stringResource(Res.string.time_format_placeholder),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            DSButton(
                text = stringResource(Res.string.action_add),
                onClick = { if (description.isNotBlank()) onAdd(description, selectedCategory, startTime, endTime) },
                enabled = description.isNotBlank()
            )
        },
        dismissButton = {
            DSTextButton(text = stringResource(Res.string.action_cancel), onClick = onDismiss)
        }
    )
}
