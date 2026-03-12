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
import androidx.compose.ui.unit.dp
import com.booking.worktracker.core.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.pluralStringResource
import com.booking.worktracker.data.models.Tag
import com.booking.worktracker.data.models.WorkEntry
import com.booking.worktracker.data.repository.LogRepository
import com.booking.worktracker.presentation.viewmodels.DailyLogViewModel
import com.booking.worktracker.ui.designsystem.DSTheme
import com.booking.worktracker.ui.designsystem.components.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DailyLogScreen(
    onNavigateToObjectives: () -> Unit = {},
    onNavigateToTimer: () -> Unit = {}
) {
    val viewModel = remember { DailyLogViewModel() }
    val logRepository = remember { LogRepository() }
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    var selectedDate by remember { mutableStateOf(today) }
    val workEntries by viewModel.workEntries.collectAsState()
    val selectedTags by viewModel.selectedTags.collectAsState()
    val availableTags by viewModel.availableTags.collectAsState()
    val saveMessage by viewModel.saveMessage.collectAsState()
    var showNewTagDialog by remember { mutableStateOf(false) }
    var showAddEntryDialog by remember { mutableStateOf(false) }
    var entryCountByDate by remember { mutableStateOf<Map<LocalDate, Int>>(emptyMap()) }
    var streakCount by remember { mutableStateOf(0) }

    val scope = rememberCoroutineScope()

    // Pre-capture strings for use inside coroutines
    val entryDeletedMsg = stringResource(Res.string.entry_deleted)
    val entryAddedMsg = stringResource(Res.string.entry_added)

    // Auto-clear save message
    LaunchedEffect(saveMessage) {
        if (saveMessage != null) {
            kotlinx.coroutines.delay(2000)
            viewModel.clearSaveMessage()
        }
    }

    // Load data
    LaunchedEffect(selectedDate) {
        viewModel.setDate(selectedDate)

        // Build entry counts for the selected month
        val year = selectedDate.year
        val month = selectedDate.month
        val daysInMonth = when (month) {
            Month.JANUARY, Month.MARCH, Month.MAY, Month.JULY,
            Month.AUGUST, Month.OCTOBER, Month.DECEMBER -> 31
            Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30
            Month.FEBRUARY -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
        }
        val counts = mutableMapOf<LocalDate, Int>()
        for (day in 1..daysInMonth) {
            val date = LocalDate(year, month, day)
            val dayLog = logRepository.getLogForDate(date)
            val entryCount = dayLog?.entries?.size ?: 0
            if (entryCount > 0) {
                counts[date] = entryCount
            }
        }
        entryCountByDate = counts

        // Calculate streak
        var streak = 0
        var checkDate = today
        while (true) {
            val log = logRepository.getLogForDate(checkDate)
            if (log != null && log.entries.isNotEmpty()) {
                streak++
                checkDate = checkDate.minus(1, DateTimeUnit.DAY)
            } else {
                break
            }
        }
        streakCount = streak
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(DSTheme.spacing.screenPadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.sectionSpacing)
    ) {
        // Greeting header
        GreetingHeader(
            streakCount = streakCount
        )

        // Monthly calendar picker
        DSCard(modifier = Modifier.fillMaxWidth()) {
            MonthlyCalendarPicker(
                selectedDate = selectedDate,
                entryCountByDate = entryCountByDate,
                onDayClick = { date -> selectedDate = date }
            )
        }

        // Entry count indicator
        if (selectedDate == today) {
            DSSectionHeader(
                title = stringResource(Res.string.start_your_day),
                action = {
                    Surface(
                        shape = DSTheme.shapes.pill,
                        color = DSTheme.colors.primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = pluralStringResource(Res.plurals.entries_count, workEntries.size, workEntries.size),
                            style = DSTheme.font.labelMedium,
                            color = DSTheme.colors.primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            )
        } else {
            DSSectionHeader(title = stringResource(Res.string.viewing_date, selectedDate.toString()))
        }

        // Action cards
        ActionCard(
            title = stringResource(Res.string.daily_log),
            subtitle = stringResource(Res.string.record_work_entries),
            backgroundColor = DSTheme.cardBlue,
            icon = Icons.Default.Edit,
            badge = if (workEntries.isNotEmpty()) pluralStringResource(Res.plurals.logged_count, workEntries.size, workEntries.size) else null,
            onClick = { showAddEntryDialog = true }
        )

        ActionCard(
            title = stringResource(Res.string.objectives),
            subtitle = stringResource(Res.string.check_your_goals),
            backgroundColor = DSTheme.cardGreen,
            icon = Icons.Default.Flag,
            onClick = onNavigateToObjectives
        )

        ActionCard(
            title = stringResource(Res.string.quick_timer),
            subtitle = stringResource(Res.string.start_tracking_time),
            backgroundColor = DSTheme.cardOrange,
            icon = Icons.Default.Timer,
            onClick = onNavigateToTimer
        )

        // Work entries section
        Column(
            verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.small)
        ) {
            DSSectionHeader(
                title = stringResource(Res.string.work_entries_count, workEntries.size),
                action = {
                    DSButton(
                        text = stringResource(Res.string.add_entry),
                        icon = Icons.Default.Add,
                        onClick = { showAddEntryDialog = true }
                    )
                }
            )

            if (workEntries.isEmpty()) {
                DSCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(Res.string.no_entries_yet),
                        style = DSTheme.font.bodyMedium,
                        color = DSTheme.colors.onSurfaceVariant
                    )
                }
            } else {
                workEntries.forEach { entry ->
                    WorkEntryCard(
                        entry = entry,
                        onDelete = {
                            scope.launch {
                                viewModel.deleteWorkEntry(entry.id)
                            }
                        }
                    )
                }
            }
        }

        // Tags section
        Column(
            verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.small)
        ) {
            DSSectionHeader(
                title = stringResource(Res.string.tags),
                action = {
                    IconButton(onClick = { showNewTagDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(Res.string.add_new_tag))
                    }
                }
            )

            if (availableTags.isEmpty()) {
                Text(
                    text = stringResource(Res.string.no_tags_yet),
                    style = DSTheme.font.bodyMedium,
                    color = DSTheme.colors.onSurfaceVariant
                )
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DSTheme.spacing.small),
                    verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.small)
                ) {
                    availableTags.forEach { tag ->
                        DSTagChip(
                            tagName = tag.name,
                            tagColor = tag.color,
                            selected = tag in selectedTags,
                            onClick = {
                                viewModel.toggleTag(tag)
                            }
                        )
                    }
                }
            }
        }

        // Status message
        saveMessage?.let { message ->
            Text(
                text = message,
                style = DSTheme.font.bodyMedium,
                color = DSTheme.colors.primary
            )
        }
    }

    // Add entry dialog
    if (showAddEntryDialog) {
        AddEntryDialog(
            onDismiss = { showAddEntryDialog = false },
            onConfirm = { content ->
                scope.launch {
                    try {
                        viewModel.addWorkEntry(content)
                        showAddEntryDialog = false
                    } catch (e: Exception) {
                        // ViewModel handles error state
                    }
                }
            }
        )
    }

    // New tag dialog
    if (showNewTagDialog) {
        NewTagDialog(
            onDismiss = { showNewTagDialog = false },
            onConfirm = { name, color ->
                scope.launch {
                    viewModel.createTag(name, color)
                    showNewTagDialog = false
                }
            }
        )
    }
}

@Composable
fun WorkEntryCard(
    entry: WorkEntry,
    onDelete: () -> Unit
) {
    DSCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "\u2022 ${entry.content}",
                style = DSTheme.font.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(DSTheme.sizes.iconMedium)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(Res.string.delete_entry),
                    tint = DSTheme.colors.error
                )
            }
        }
    }
}

@Composable
fun AddEntryDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.add_work_entry)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.medium)
            ) {
                Text(
                    text = stringResource(Res.string.what_did_you_accomplish),
                    style = DSTheme.font.bodyMedium
                )

                DSOutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = stringResource(Res.string.entry_placeholder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(DSTheme.spacing.space16 * 2),
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            DSButton(
                text = stringResource(Res.string.action_add),
                onClick = {
                    if (content.isNotBlank()) {
                        onConfirm(content)
                    }
                },
                enabled = content.isNotBlank()
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

@Composable
fun NewTagDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit
) {
    var tagName by remember { mutableStateOf("") }
    var selectedColorIndex by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.create_new_tag)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.medium)
            ) {
                DSOutlinedTextField(
                    value = tagName,
                    onValueChange = { tagName = it },
                    label = stringResource(Res.string.tag_name),
                    placeholder = stringResource(Res.string.tag_name_placeholder),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = stringResource(Res.string.color_label),
                    style = DSTheme.font.bodyMedium
                )

                DSColorPicker(
                    selectedColorIndex = selectedColorIndex,
                    onColorSelected = { selectedColorIndex = it }
                )
            }
        },
        confirmButton = {
            DSButton(
                text = stringResource(Res.string.action_create),
                onClick = {
                    if (tagName.isNotBlank()) {
                        val colorHex = String.format(
                            "#%06X",
                            (0xFFFFFF and DSTheme.tagColors[selectedColorIndex].value.toInt())
                        )
                        onConfirm(tagName, colorHex)
                    }
                },
                enabled = tagName.isNotBlank()
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
