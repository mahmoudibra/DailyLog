package com.booking.worktracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.booking.worktracker.data.models.UserLevel
import com.booking.worktracker.ui.designsystem.DSTheme

@Composable
fun XpProgressBar(
    userLevel: UserLevel?,
    onNavigateToAchievements: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (userLevel == null) return

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onNavigateToAchievements() },
        shape = DSTheme.shapes.large,
        color = DSTheme.colors.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DSTheme.spacing.large, vertical = DSTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DSTheme.spacing.medium)
        ) {
            // Rank icon / level badge
            Surface(
                shape = CircleShape,
                color = DSTheme.colors.primary,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${userLevel.currentLevel}",
                        style = DSTheme.font.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = DSTheme.colors.onPrimary
                    )
                }
            }

            // Rank title
            Text(
                text = userLevel.rankTitle,
                style = DSTheme.font.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = DSTheme.colors.onSurface
            )

            // Progress bar
            LinearProgressIndicator(
                progress = { userLevel.progressPercent.coerceIn(0f, 1f) },
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = DSTheme.colors.primary,
                trackColor = DSTheme.colors.surfaceVariant
            )

            // XP text
            Text(
                text = "${userLevel.totalXp} XP",
                style = DSTheme.font.labelMedium,
                color = DSTheme.colors.onSurfaceVariant
            )
        }
    }
}
