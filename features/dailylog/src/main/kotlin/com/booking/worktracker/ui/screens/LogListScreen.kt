package com.booking.worktracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.booking.worktracker.core.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.pluralStringResource
import com.booking.worktracker.data.models.DailyLog
import com.booking.worktracker.data.repository.LogRepository
import com.booking.worktracker.ui.designsystem.DSTheme
import com.booking.worktracker.ui.designsystem.components.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogListScreen(
    logRepository: LogRepository
) {
    var logs by remember { mutableStateOf<List<DailyLog>>(emptyList()) }
    var selectedLog by remember { mutableStateOf<DailyLog?>(null) }

    LaunchedEffect(Unit) {
        logs = logRepository.getAllLogs(limit = 50).filter { it.entries.isNotEmpty() }
    }

    if (selectedLog != null) {
        LogDetailView(
            log = selectedLog!!,
            onClose = { selectedLog = null }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(DSTheme.spacing.screenPadding),
            verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.sectionSpacing)
        ) {
            DSScreenTitle(stringResource(Res.string.history))

            if (logs.isEmpty()) {
                DSEmptyState(
                    message = stringResource(Res.string.no_logs_yet),
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.medium),
                    modifier = Modifier.weight(1f)
                ) {
                    items(logs) { log ->
                        LogCard(
                            log = log,
                            onClick = { selectedLog = log }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogCard(
    log: DailyLog,
    onClick: () -> Unit
) {
    DSCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Text(
            text = log.date.toString(),
            style = DSTheme.font.titleMedium,
            color = DSTheme.colors.primary
        )

        Spacer(Modifier.height(DSTheme.spacing.small))

        Text(
            text = pluralStringResource(Res.plurals.entry_count, log.entries.size, log.entries.size),
            style = DSTheme.font.labelMedium,
            color = DSTheme.colors.onSurfaceVariant
        )

        Spacer(Modifier.height(DSTheme.spacing.small))

        Column(
            verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.extraSmall)
        ) {
            log.entries.take(3).forEach { entry ->
                Text(
                    text = "\u2022 ${entry.content}",
                    style = DSTheme.font.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (log.entries.size > 3) {
                Text(
                    text = pluralStringResource(Res.plurals.and_more_entries, log.entries.size - 3, log.entries.size - 3),
                    style = DSTheme.font.bodySmall,
                    color = DSTheme.colors.onSurfaceVariant
                )
            }
        }

        if (log.tags.isNotEmpty()) {
            Spacer(Modifier.height(DSTheme.spacing.medium))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(DSTheme.spacing.small),
                verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.extraSmall)
            ) {
                log.tags.forEach { tag ->
                    DSTagBadge(tagName = tag.name, tagColor = tag.color)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogDetailView(
    log: DailyLog,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(DSTheme.spacing.screenPadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.sectionSpacing)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DSScreenTitle(stringResource(Res.string.log_detail))

            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = stringResource(Res.string.action_close))
            }
        }

        DSCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = log.date.toString(),
                    style = DSTheme.font.titleLarge,
                    color = DSTheme.colors.primary
                )
                Text(
                    text = pluralStringResource(Res.plurals.entry_count, log.entries.size, log.entries.size),
                    style = DSTheme.font.labelLarge,
                    color = DSTheme.colors.onSurfaceVariant
                )
            }
        }

        DSCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(Res.string.work_log),
                style = DSTheme.font.titleMedium,
                color = DSTheme.colors.primary
            )
            Spacer(Modifier.height(DSTheme.spacing.small))

            Column(
                verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.small)
            ) {
                log.entries.forEach { entry ->
                    Text(
                        text = "\u2022 ${entry.content}",
                        style = DSTheme.font.bodyLarge
                    )
                }
            }
        }

        if (log.tags.isNotEmpty()) {
            DSCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(Res.string.tags),
                    style = DSTheme.font.titleMedium,
                    color = DSTheme.colors.primary
                )
                Spacer(Modifier.height(DSTheme.spacing.small))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(DSTheme.spacing.small),
                    verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.small)
                ) {
                    log.tags.forEach { tag ->
                        SuggestionChip(
                            onClick = { },
                            label = { Text(tag.name) }
                        )
                    }
                }
            }
        }
    }
}
