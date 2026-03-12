package com.booking.worktracker.ui.designsystem.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun DSOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = if (label != null) {{ Text(label) }} else null,
        placeholder = if (placeholder != null) {{ Text(placeholder) }} else null,
        singleLine = singleLine,
        maxLines = maxLines,
        enabled = enabled
    )
}

@Composable
fun DSTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    enabled: Boolean = true
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = if (label != null) {{ Text(label) }} else null,
        placeholder = if (placeholder != null) {{ Text(placeholder) }} else null,
        singleLine = singleLine,
        maxLines = maxLines,
        enabled = enabled
    )
}
