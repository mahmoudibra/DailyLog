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
import com.booking.worktracker.data.models.Tag
import com.booking.worktracker.data.models.WorkEntry
import com.booking.worktracker.data.repository.LogRepository
import com.booking.worktracker.data.repository.TagRepository
import com.booking.worktracker.ui.designsystem.components.*
import com.booking.worktracker.ui.designsystem.tokens.ColorTokens
import com.booking.worktracker.ui.designsystem.tokens.SizeTokens
import com.booking.worktracker.ui.designsystem.tokens.SpacingTokens
import com.booking.worktracker.ui.localization.LocalStrings
import kotlinx.coroutines.launch
import kotlinx.datetime.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DailyLogScreen(
    logRepository: LogRepository,
    tagRepository: TagRepository,
    onNavigateToObjectives: () -> Unit = {},
    onNavigateToTimer: () -> Unit = {}
) {
    val strings = LocalStrings.current
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    var selectedDate by remember { mutableStateOf(today) }
    var workEntries by remember { mutableStateOf<List<WorkEntry>>(emptyList()) }
    var selectedTags by remember { mutableStateOf<Set<Tag>>(emptySet()) }
    var availableTags by remember { mutableStateOf<List<Tag>>(emptyList()) }
    var showNewTagDialog by remember { mutableStateOf(false) }
    var showAddEntryDialog by remember { mutableStateOf(false) }
    var saveMessage by remember { mutableStateOf<String?>(null) }
    var entryCountByDate by remember { mutableStateOf<Map<LocalDate, Int>>(emptyMap()) }
    var streakCount by remember { mutableStateOf(0) }

    val scope = rememberCoroutineScope()

    // Load data
    LaunchedEffect(selectedDate) {
        availableTags = tagRepository.getAllTags()

        logRepository.getLogForDate(selectedDate)?.let { log ->
            workEntries = log.entries
            selectedTags = log.tags.toSet()
        } ?: run {
            workEntries = emptyList()
            selectedTags = emptySet()
        }

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
            .padding(SpacingTokens.screenPadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.sectionSpacing)
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
                title = strings.startYourDay,
                action = {
                    Surface(
                        shape = com.booking.worktracker.ui.designsystem.tokens.ShapeTokens.pill,
                        color = ColorTokens.Primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = strings.entriesCount(workEntries.size),
                            style = MaterialTheme.typography.labelMedium,
                            color = ColorTokens.Primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            )
        } else {
            DSSectionHeader(title = strings.viewingDate(selectedDate.toString()))
        }

        // Action cards
        ActionCard(
            title = strings.dailyLog,
            subtitle = strings.recordWorkEntries,
            backgroundColor = ColorTokens.CardBlue,
            icon = Icons.Default.Edit,
            badge = if (workEntries.isNotEmpty()) strings.loggedCount(workEntries.size) else null,
            onClick = { showAddEntryDialog = true }
        )

        ActionCard(
            title = strings.objectives,
            subtitle = strings.checkYourGoals,
            backgroundColor = ColorTokens.CardGreen,
            icon = Icons.Default.Flag,
            onClick = onNavigateToObjectives
        )

        ActionCard(
            title = strings.quickTimer,
            subtitle = strings.startTrackingTime,
            backgroundColor = ColorTokens.CardOrange,
            icon = Icons.Default.Timer,
            onClick = onNavigateToTimer
        )

        // Work entries section
        Column(
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.small)
        ) {
            DSSectionHeader(
                title = strings.workEntriesCount(workEntries.size),
                action = {
                    DSButton(
                        text = strings.addEntry,
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
                        text = strings.noEntriesYet,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                workEntries.forEach { entry ->
                    WorkEntryCard(
                        entry = entry,
                        onDelete = {
                            scope.launch {
                                logRepository.deleteWorkEntry(entry.id)
                                workEntries = workEntries.filter { it.id != entry.id }
                                saveMessage = strings.entryDeleted
                                kotlinx.coroutines.delay(2000)
                                saveMessage = null
                            }
                        }
                    )
                }
            }
        }

        // Tags section
        Column(
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.small)
        ) {
            DSSectionHeader(
                title = strings.tags,
                action = {
                    IconButton(onClick = { showNewTagDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = strings.addNewTag)
                    }
                }
            )

            if (availableTags.isEmpty()) {
                Text(
                    text = strings.noTagsYet,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.small),
                    verticalArrangement = Arrangement.spacedBy(SpacingTokens.small)
                ) {
                    availableTags.forEach { tag ->
                        DSTagChip(
                            tagName = tag.name,
                            tagColor = tag.color,
                            selected = tag in selectedTags,
                            onClick = {
                                selectedTags = if (tag in selectedTags) {
                                    selectedTags - tag
                                } else {
                                    selectedTags + tag
                                }
                                scope.launch {
                                    logRepository.updateLogTags(selectedDate, selectedTags.toList())
                                }
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
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
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
                        val newEntry = logRepository.addWorkEntry(selectedDate, content)
                        workEntries = workEntries + newEntry
                        saveMessage = strings.entryAdded
                        showAddEntryDialog = false
                        kotlinx.coroutines.delay(2000)
                        saveMessage = null
                    } catch (e: Exception) {
                        saveMessage = strings.errorMessage(e.message ?: "")
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
                    tagRepository.createTag(name, color)
                    availableTags = tagRepository.getAllTags()
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
    val strings = LocalStrings.current
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
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(SizeTokens.iconMedium)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = strings.deleteEntry,
                    tint = MaterialTheme.colorScheme.error
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
    val strings = LocalStrings.current
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.addWorkEntry) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.medium)
            ) {
                Text(
                    text = strings.whatDidYouAccomplish,
                    style = MaterialTheme.typography.bodyMedium
                )

                DSOutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = strings.entryPlaceholder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SpacingTokens.space16 * 2),
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            DSButton(
                text = strings.add,
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
                text = strings.cancel,
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
    val strings = LocalStrings.current
    var tagName by remember { mutableStateOf("") }
    var selectedColorIndex by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.createNewTag) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.medium)
            ) {
                DSOutlinedTextField(
                    value = tagName,
                    onValueChange = { tagName = it },
                    label = strings.tagName,
                    placeholder = strings.tagNamePlaceholder,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = strings.colorLabel,
                    style = MaterialTheme.typography.bodyMedium
                )

                DSColorPicker(
                    selectedColorIndex = selectedColorIndex,
                    onColorSelected = { selectedColorIndex = it }
                )
            }
        },
        confirmButton = {
            DSButton(
                text = strings.create,
                onClick = {
                    if (tagName.isNotBlank()) {
                        val colorHex = String.format(
                            "#%06X",
                            (0xFFFFFF and ColorTokens.TagColors[selectedColorIndex].value.toInt())
                        )
                        onConfirm(tagName, colorHex)
                    }
                },
                enabled = tagName.isNotBlank()
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
