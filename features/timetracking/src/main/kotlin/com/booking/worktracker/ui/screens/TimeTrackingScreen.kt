package com.booking.worktracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.booking.worktracker.core.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import com.booking.worktracker.data.models.TimeEntry
import com.booking.worktracker.presentation.viewmodels.TimeTrackingViewModel
import com.booking.worktracker.ui.designsystem.components.*
import com.booking.worktracker.ui.designsystem.tokens.SpacingTokens
import kotlinx.coroutines.launch
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeTrackingScreen(
    viewModel: TimeTrackingViewModel = remember { TimeTrackingViewModel() }
) {
    val entries by viewModel.entries.collectAsState()
    val runningEntry by viewModel.runningEntry.collectAsState()
    val totalMinutes by viewModel.totalMinutes.collectAsState()
    val categoryBreakdown by viewModel.categoryBreakdown.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val message by viewModel.message.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showManualDialog by remember { mutableStateOf(false) }
    var showFocusRatingDialog by remember { mutableStateOf(false) }

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
                        onClick = { showFocusRatingDialog = true }
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
                        onClick = { showFocusRatingDialog = true }
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
            onAdd = { description, category, startTime, endTime, focusRating ->
                viewModel.addManualEntry(description, category, startTime, endTime, focusRating)
                showManualDialog = false
            }
        )
    }

    // Focus rating dialog
    if (showFocusRatingDialog) {
        FocusRatingDialog(
            onDismiss = { showFocusRatingDialog = false },
            onRate = { rating ->
                viewModel.stopTimer(rating)
                showFocusRatingDialog = false
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
                    entry.focusRating?.let { rating ->
                        Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                            repeat(rating) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
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
    onAdd: (String, String, String, String, Int?) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: "General") }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("10:00") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var focusRating by remember { mutableIntStateOf(0) }

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

                // Focus rating
                Text(
                    text = stringResource(Res.string.focus_rating_optional),
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.small),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(Modifier.weight(1f))
                    (1..5).forEach { rating ->
                        IconButton(onClick = { focusRating = rating }) {
                            Icon(
                                if (rating <= focusRating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = stringResource(Res.string.focus_stars_desc, rating),
                                tint = if (rating <= focusRating)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            DSButton(
                text = stringResource(Res.string.action_add),
                onClick = { if (description.isNotBlank()) onAdd(description, selectedCategory, startTime, endTime, if (focusRating > 0) focusRating else null) },
                enabled = description.isNotBlank()
            )
        },
        dismissButton = {
            DSTextButton(text = stringResource(Res.string.action_cancel), onClick = onDismiss)
        }
    )
}

@Composable
fun FocusRatingDialog(
    onDismiss: () -> Unit,
    onRate: (Int?) -> Unit
) {
    var selectedRating by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.focus_rate_title)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.medium),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(Res.string.focus_rate_prompt),
                    style = MaterialTheme.typography.bodyMedium
                )
                // Star rating row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.small),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(Modifier.weight(1f))
                    (1..5).forEach { rating ->
                        IconButton(onClick = { selectedRating = rating }) {
                            Icon(
                                if (rating <= selectedRating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = stringResource(Res.string.focus_stars_desc, rating),
                                tint = if (rating <= selectedRating)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                }
                // Rating label
                Text(
                    text = when (selectedRating) {
                        1 -> stringResource(Res.string.focus_rating_1)
                        2 -> stringResource(Res.string.focus_rating_2)
                        3 -> stringResource(Res.string.focus_rating_3)
                        4 -> stringResource(Res.string.focus_rating_4)
                        5 -> stringResource(Res.string.focus_rating_5)
                        else -> stringResource(Res.string.focus_tap_to_rate)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            DSButton(
                text = stringResource(Res.string.focus_save_rating),
                onClick = { onRate(if (selectedRating > 0) selectedRating else null) }
            )
        },
        dismissButton = {
            DSTextButton(
                text = stringResource(Res.string.focus_skip),
                onClick = { onRate(null) }
            )
        }
    )
}
