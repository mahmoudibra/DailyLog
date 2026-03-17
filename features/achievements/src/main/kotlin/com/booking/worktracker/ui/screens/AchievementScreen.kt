package com.booking.worktracker.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.booking.worktracker.data.models.Achievement
import com.booking.worktracker.data.models.AchievementCategory
import com.booking.worktracker.data.models.Rank
import com.booking.worktracker.data.models.UserLevel
import com.booking.worktracker.di.AchievementComponent
import com.booking.worktracker.presentation.viewmodels.DailyXpTotal
import com.booking.worktracker.ui.designsystem.DSTheme
import com.booking.worktracker.ui.designsystem.components.*
import com.booking.worktracker.core.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun AchievementScreen() {
    val component = remember { AchievementComponent.instance }
    val viewModel = viewModel { component.achievementViewModel }

    val userLevel by viewModel.userLevel.collectAsState()
    val achievements by viewModel.achievements.collectAsState()
    val weeklyXpData by viewModel.weeklyXpData.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(DSTheme.spacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.sectionSpacing)
    ) {
        item {
            DSScreenTitle(stringResource(Res.string.achievements_title))
        }

        item {
            userLevel?.let { level ->
                LevelHeaderCard(level)
            }
        }

        item {
            if (weeklyXpData.isNotEmpty()) {
                WeeklyXpChartCard(weeklyXpData)
            }
        }

        item {
            CategoryFilterChips(
                selectedCategory = selectedCategory,
                onCategorySelected = { viewModel.filterByCategory(it) }
            )
        }

        item {
            if (achievements.isEmpty()) {
                DSEmptyState(message = stringResource(Res.string.achievements_empty))
            } else {
                AchievementsGrid(achievements)
            }
        }
    }
}

@Composable
private fun LevelHeaderCard(userLevel: UserLevel) {
    val rank = Rank.fromName(userLevel.rankTitle)

    DSCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = rank?.let { stringResource(it.nameRes) } ?: userLevel.rankTitle,
                style = DSTheme.font.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = DSTheme.colors.primary
            )

            Surface(
                shape = CircleShape,
                color = DSTheme.colors.primary,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${userLevel.currentLevel}",
                        style = DSTheme.font.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = DSTheme.colors.onPrimary
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.extraSmall),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(Res.string.achievements_level, userLevel.currentLevel),
                        style = DSTheme.font.bodySmall,
                        color = DSTheme.colors.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(Res.string.achievements_next_level, userLevel.currentLevel + 1),
                        style = DSTheme.font.bodySmall,
                        color = DSTheme.colors.onSurfaceVariant
                    )
                }

                LinearProgressIndicator(
                    progress = { userLevel.progressPercent.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = DSTheme.colors.primary,
                    trackColor = DSTheme.colors.surfaceVariant
                )

                Text(
                    text = stringResource(Res.string.achievements_xp_progress, userLevel.totalXp, userLevel.xpForNextLevel),
                    style = DSTheme.font.bodyMedium,
                    color = DSTheme.colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Surface(
                shape = DSTheme.shapes.pill,
                color = DSTheme.colors.primary.copy(alpha = 0.15f)
            ) {
                Text(
                    text = stringResource(Res.string.achievements_total_xp, userLevel.totalXp),
                    style = DSTheme.font.labelLarge,
                    color = DSTheme.colors.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun WeeklyXpChartCard(weeklyXpData: List<DailyXpTotal>) {
    DSCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.medium)) {
            DSSectionHeader(title = stringResource(Res.string.achievements_weekly_xp))

            val totalWeekXp = weeklyXpData.sumOf { it.totalXp }
            Text(
                text = stringResource(Res.string.achievements_weekly_total, totalWeekXp),
                style = DSTheme.font.bodyMedium,
                color = DSTheme.colors.onSurfaceVariant
            )

            val maxXp = weeklyXpData.maxOfOrNull { it.totalXp } ?: 1L
            val barColor = DSTheme.colors.primary
            val trackColor = DSTheme.colors.surfaceVariant

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                val barCount = weeklyXpData.size
                if (barCount == 0) return@Canvas

                val barWidth = size.width / (barCount * 2f)
                val spacing = barWidth
                val chartHeight = size.height

                weeklyXpData.forEachIndexed { index, dailyXp ->
                    val x = index * (barWidth + spacing) + spacing / 2
                    val barHeight = if (maxXp > 0) (dailyXp.totalXp.toFloat() / maxXp.toFloat()) * (chartHeight * 0.85f) else 0f

                    drawRoundRect(
                        color = trackColor,
                        topLeft = Offset(x, 0f),
                        size = Size(barWidth, chartHeight),
                        cornerRadius = CornerRadius(barWidth / 4, barWidth / 4)
                    )

                    if (barHeight > 0) {
                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(x, chartHeight - barHeight),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(barWidth / 4, barWidth / 4)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                weeklyXpData.forEach { dailyXp ->
                    Text(
                        text = dailyXp.date,
                        style = DSTheme.font.labelSmall,
                        color = DSTheme.colors.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryFilterChips(
    selectedCategory: AchievementCategory?,
    onCategorySelected: (AchievementCategory?) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(DSTheme.spacing.small)
    ) {
        FilterChip(
            selected = selectedCategory == null,
            onClick = { onCategorySelected(null) },
            label = { Text(stringResource(Res.string.achievements_filter_all)) },
            leadingIcon = if (selectedCategory == null) {
                { Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(18.dp)) }
            } else null
        )

        AchievementCategory.entries.forEach { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(stringResource(category.nameRes)) },
                leadingIcon = if (selectedCategory == category) {
                    { Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else null
            )
        }
    }
}

@Composable
private fun AchievementsGrid(achievements: List<Achievement>) {
    val columns = 2
    val rows = (achievements.size + columns - 1) / columns

    Column(verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.itemSpacing)) {
        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DSTheme.spacing.itemSpacing)
            ) {
                for (col in 0 until columns) {
                    val index = row * columns + col
                    if (index < achievements.size) {
                        Box(modifier = Modifier.weight(1f)) {
                            AchievementCard(achievements[index])
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementCard(achievement: Achievement) {
    val def = achievement.definition
    val alpha = if (achievement.isUnlocked) 1f else 0.5f
    val containerColor = if (achievement.isUnlocked) {
        categoryColor(def.category).copy(alpha = 0.12f)
    } else {
        DSTheme.colors.surfaceVariant.copy(alpha = 0.5f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha),
        shape = DSTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(DSTheme.spacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.small)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(def.subtitleRes),
                    style = DSTheme.font.headlineSmall,
                    color = DSTheme.colors.onSurface
                )
                if (achievement.isUnlocked) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = stringResource(Res.string.achievements_unlocked),
                        tint = DSTheme.colors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = stringResource(Res.string.achievements_locked),
                        tint = DSTheme.colors.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Text(
                text = stringResource(def.titleRes),
                style = DSTheme.font.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = DSTheme.colors.onSurface
            )

            Text(
                text = stringResource(def.descriptionRes),
                style = DSTheme.font.bodySmall,
                color = DSTheme.colors.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Surface(
                shape = DSTheme.shapes.pill,
                color = if (achievement.isUnlocked) {
                    DSTheme.colors.primary.copy(alpha = 0.15f)
                } else {
                    DSTheme.colors.surfaceVariant
                }
            ) {
                Text(
                    text = stringResource(Res.string.achievements_xp_reward, def.xpReward),
                    style = DSTheme.font.labelSmall,
                    color = if (achievement.isUnlocked) DSTheme.colors.primary else DSTheme.colors.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            if (achievement.isUnlocked && achievement.unlockedAt != null) {
                Text(
                    text = stringResource(Res.string.achievements_unlocked_date, achievement.unlockedAt!!),
                    style = DSTheme.font.labelSmall,
                    color = DSTheme.colors.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun categoryColor(category: AchievementCategory): Color {
    return when (category) {
        AchievementCategory.CONSISTENCY -> DSTheme.colors.tertiary
        AchievementCategory.MASTERY -> DSTheme.colors.primary
        AchievementCategory.EXPLORER -> DSTheme.colors.secondary
        AchievementCategory.CHALLENGE -> DSTheme.colors.error
    }
}
