package com.booking.worktracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.booking.worktracker.data.models.ChecklistItem
import com.booking.worktracker.ui.designsystem.components.*
import com.booking.worktracker.ui.designsystem.tokens.SpacingTokens
import com.booking.worktracker.ui.localization.LocalStrings

@Composable
fun ChecklistItemRow(
    item: ChecklistItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val strings = LocalStrings.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = SpacingTokens.extraSmall),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.completed,
                onCheckedChange = { onToggle() }
            )
            Text(
                text = item.text,
                style = if (item.completed) {
                    MaterialTheme.typography.bodyMedium.copy(
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                    )
                } else {
                    MaterialTheme.typography.bodyMedium
                },
                color = if (item.completed) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }

        IconButton(onClick = onDelete) {
            Icon(
                Icons.Default.Close,
                contentDescription = strings.delete,
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun AddChecklistItemDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    val strings = LocalStrings.current
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.addChecklistItemTitle) },
        text = {
            DSOutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = strings.task,
                placeholder = strings.taskPlaceholder,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            DSButton(
                text = strings.add,
                onClick = {
                    if (text.isNotBlank()) {
                        onAdd(text)
                    }
                },
                enabled = text.isNotBlank()
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
