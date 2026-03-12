package com.booking.worktracker.ui.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.booking.worktracker.ui.designsystem.tokens.DSColors
import com.booking.worktracker.ui.designsystem.tokens.SpacingTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DSTagChip(
    tagName: String,
    tagColor: String?,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val chipColor = tagColor?.let { parseColor(it) } ?: DSColors.Primary

    FilterChip(
        selected = selected,
        onClick = { onClick?.invoke() },
        label = { Text(tagName) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = chipColor.copy(alpha = 0.3f),
            selectedLabelColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun DSTagBadge(
    tagName: String,
    tagColor: String?
) {
    val badgeColor = tagColor?.let { parseColor(it) } ?: DSColors.Primary

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = badgeColor.copy(alpha = 0.2f),
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Text(
            text = tagName,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun DSColorPicker(
    selectedColorIndex: Int,
    onColorSelected: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.small)
    ) {
        DSColors.TagColors.forEachIndexed { index, color ->
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(color)
                    .clickable { onColorSelected(index) },
                contentAlignment = Alignment.Center
            ) {
                if (index == selectedColorIndex) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White)
                    )
                }
            }
        }
    }
}

private fun parseColor(hex: String): Color {
    return try {
        val colorString = hex.removePrefix("#")
        val colorLong = colorString.toLong(16)
        Color(
            red = ((colorLong shr 16) and 0xFF).toInt(),
            green = ((colorLong shr 8) and 0xFF).toInt(),
            blue = (colorLong and 0xFF).toInt()
        )
    } catch (e: Exception) {
        DSColors.Primary
    }
}
