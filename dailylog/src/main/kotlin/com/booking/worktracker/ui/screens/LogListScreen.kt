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
import com.booking.worktracker.data.models.DailyLog
import com.booking.worktracker.data.repository.LogRepository
import com.booking.worktracker.ui.designsystem.components.*
import com.booking.worktracker.ui.designsystem.tokens.SpacingTokens
import com.booking.worktracker.ui.localization.LocalStrings

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogListScreen(
    logRepository: LogRepository
) {
    val strings = LocalStrings.current
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
                .padding(SpacingTokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.sectionSpacing)
        ) {
            DSScreenTitle(strings.history)

            if (logs.isEmpty()) {
                DSEmptyState(
                    message = strings.noLogsYet,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(SpacingTokens.medium),
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
    val strings = LocalStrings.current
    DSCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Text(
            text = log.date.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(SpacingTokens.small))

        Text(
            text = strings.entryCount(log.entries.size),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(SpacingTokens.small))

        Column(
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.extraSmall)
        ) {
            log.entries.take(3).forEach { entry ->
                Text(
                    text = "\u2022 ${entry.content}",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (log.entries.size > 3) {
                Text(
                    text = strings.andMoreEntries(log.entries.size - 3),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (log.tags.isNotEmpty()) {
            Spacer(Modifier.height(SpacingTokens.medium))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.small),
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.extraSmall)
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
    val strings = LocalStrings.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SpacingTokens.screenPadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.sectionSpacing)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DSScreenTitle(strings.logDetail)

            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = strings.close)
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
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = strings.entryCount(log.entries.size),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        DSCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = strings.workLog,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(SpacingTokens.small))

            Column(
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.small)
            ) {
                log.entries.forEach { entry ->
                    Text(
                        text = "\u2022 ${entry.content}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        if (log.tags.isNotEmpty()) {
            DSCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = strings.tags,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(SpacingTokens.small))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.small),
                    verticalArrangement = Arrangement.spacedBy(SpacingTokens.small)
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
