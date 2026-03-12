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
import com.booking.worktracker.data.models.TimeEntry
import com.booking.worktracker.data.repository.TimeEntryRepository
import com.booking.worktracker.presentation.viewmodels.TimeTrackingViewModel
import com.booking.worktracker.ui.designsystem.components.*
import com.booking.worktracker.ui.designsystem.tokens.SpacingTokens
import com.booking.worktracker.ui.localization.LocalStrings
import kotlinx.coroutines.launch
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeTrackingScreen(
    timeEntryRepository: TimeEntryRepository
) {
    val strings = LocalStrings.current
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
            DSScreenTitle(strings.timeTracking)

            Row(horizontalArrangement = Arrangement.spacedBy(SpacingTokens.small)) {
                if (runningEntry == null) {
                    DSButton(
                        text = strings.startTimer,
                        icon = Icons.Default.PlayArrow,
                        onClick = { showAddDialog = true }
                    )
                } else {
                    DSButton(
                        text = strings.stopTimer,
                        icon = Icons.Default.Stop,
                        onClick = { viewModel.stopTimer() }
                    )
                }
                DSOutlinedButton(
                    text = strings.addManual,
                    icon = Icons.Default.Add,
                    onClick = { showManualDialog = true }
                )
            }
        }

        // Message banner
        message?.let { msg ->
            DSInfoBanner(
                title = strings.status,
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
                            text = strings.timerRunning,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = running.description,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = strings.startedAt(running.startTime, running.category),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DSButton(
                        text = strings.stop,
                        icon = Icons.Default.Stop,
                        onClick = { viewModel.stopTimer() }
                    )
                }
            }
        }

        // Summary card
        DSCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.small)) {
                DSSectionHeader(title = strings.todaysSummary)
                Text(
                    text = strings.totalTime(viewModel.formatTotalTime(totalMinutes)),
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
        DSSectionHeader(title = strings.timeEntries)

        if (entries.isEmpty()) {
            DSEmptyState(
                message = strings.noTimeEntries,
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
    val strings = LocalStrings.current
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
                        text = strings.timeRange(entry.startTime, entry.endTime),
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
                    contentDescription = strings.delete,
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
    val strings = LocalStrings.current
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: "General") }
    var categoryExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.startTimer) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.medium)) {
                DSOutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = strings.whatAreYouWorkingOn,
                    placeholder = strings.workingOnPlaceholder,
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
                        label = { Text(strings.category) },
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
                text = strings.start,
                onClick = { if (description.isNotBlank()) onStart(description, selectedCategory) },
                enabled = description.isNotBlank()
            )
        },
        dismissButton = {
            DSTextButton(text = strings.cancel, onClick = onDismiss)
        }
    )
}

@Composable
fun ManualEntryDialog(
    categories: List<String>,
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String) -> Unit
) {
    val strings = LocalStrings.current
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: "General") }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("10:00") }
    var categoryExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.addManualEntry) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.medium)) {
                DSOutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = strings.description,
                    placeholder = strings.whatDidYouWorkOn,
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
                        label = { Text(strings.category) },
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
                        label = strings.startTime,
                        placeholder = strings.timeFormatPlaceholder,
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    DSOutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = strings.endTime,
                        placeholder = strings.timeFormatPlaceholder,
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            DSButton(
                text = strings.add,
                onClick = { if (description.isNotBlank()) onAdd(description, selectedCategory, startTime, endTime) },
                enabled = description.isNotBlank()
            )
        },
        dismissButton = {
            DSTextButton(text = strings.cancel, onClick = onDismiss)
        }
    )
}
