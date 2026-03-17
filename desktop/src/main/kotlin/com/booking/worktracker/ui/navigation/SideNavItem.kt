package com.booking.worktracker.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.booking.worktracker.ui.designsystem.DSTheme

@Composable
fun SideNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    expanded: Boolean,
    onClick: () -> Unit,
    isTitle: Boolean = false
) {
    val bgColor = when {
        isTitle -> Color.Transparent
        selected -> DSTheme.colors.primary.copy(alpha = 0.12f)
        else -> Color.Transparent
    }
    val contentColor = when {
        isTitle || selected -> DSTheme.colors.primary
        else -> DSTheme.colors.onSurfaceVariant
    }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = DSTheme.shapes.medium,
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(if (isTitle) 24.dp else 20.dp)
            )
            if (expanded) {
                Text(
                    text = label,
                    style = if (isTitle) DSTheme.font.titleMedium else DSTheme.font.bodyMedium,
                    color = contentColor,
                    maxLines = 1
                )
            }
        }
    }
}
