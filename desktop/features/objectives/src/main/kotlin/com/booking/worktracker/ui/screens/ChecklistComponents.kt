package com.booking.worktracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.booking.worktracker.data.models.ChecklistItem
import com.booking.worktracker.ui.designsystem.DSTheme
import com.booking.worktracker.ui.designsystem.components.*
import com.booking.worktracker.core.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun ChecklistItemRow(
    item: ChecklistItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = DSTheme.spacing.extraSmall),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(DSTheme.spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.completed,
                onCheckedChange = { onToggle() }
            )
            Text(
                text = item.text,
                style = if (item.completed) {
                    DSTheme.font.bodyMedium.copy(
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                    )
                } else {
                    DSTheme.font.bodyMedium
                },
                color = if (item.completed) {
                    DSTheme.colors.onSurfaceVariant
                } else {
                    DSTheme.colors.onSurface
                }
            )
        }

        IconButton(onClick = onDelete) {
            Icon(
                Icons.Default.Close,
                contentDescription = stringResource(Res.string.action_delete),
                tint = DSTheme.colors.error
            )
        }
    }
}

@Composable
fun AddChecklistItemDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {

    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.add_checklist_item_title)) },
        text = {
            DSOutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = stringResource(Res.string.task),
                placeholder = stringResource(Res.string.task_placeholder),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            DSButton(
                text = stringResource(Res.string.action_add),
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
                text = stringResource(Res.string.action_cancel),
                onClick = onDismiss
            )
        }
    )
}
