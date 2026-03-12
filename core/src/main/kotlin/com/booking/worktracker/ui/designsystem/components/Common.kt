package com.booking.worktracker.ui.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.booking.worktracker.ui.designsystem.tokens.DSColors
import com.booking.worktracker.ui.designsystem.tokens.ShapeTokens
import com.booking.worktracker.ui.designsystem.tokens.SizeTokens
import com.booking.worktracker.ui.designsystem.tokens.SpacingTokens
import com.booking.worktracker.core.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.pluralStringResource
import kotlinx.datetime.*

@Composable
fun DSScreenTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
    )
}

@Composable
fun DSSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        action?.invoke()
    }
}

@Composable
fun DSEmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun DSInfoBanner(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = ShapeTokens.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(SpacingTokens.cardPadding),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.small)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                icon?.invoke()
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun DSLoadingIndicator(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun DSDivider(
    modifier: Modifier = Modifier
) {
    Divider(
        modifier = modifier,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

// --- New components for warm/playful redesign ---

@Composable
fun GreetingHeader(
    modifier: Modifier = Modifier,
    streakCount: Int = 0
) {
    val hour = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
    val greeting = when {
        hour < 12 -> stringResource(Res.string.good_morning)
        hour < 17 -> stringResource(Res.string.good_afternoon)
        else -> stringResource(Res.string.good_evening)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "$greeting,",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(Res.string.ready_to_track),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (streakCount > 0) {
            Surface(
                shape = ShapeTokens.pill,
                color = DSColors.Primary.copy(alpha = 0.15f)
            ) {
                Text(
                    text = pluralStringResource(Res.plurals.day_streak, streakCount, streakCount),
                    style = MaterialTheme.typography.labelLarge,
                    color = DSColors.Primary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun MonthlyCalendarPicker(
    selectedDate: LocalDate,
    entryCountByDate: Map<LocalDate, Int>,
    modifier: Modifier = Modifier,
    onDayClick: (LocalDate) -> Unit = {}
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    var displayedYear by remember { mutableStateOf(selectedDate.year) }
    var displayedMonth by remember { mutableStateOf(selectedDate.month) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.small)
    ) {
        // Month/Year selector row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (displayedMonth == Month.JANUARY) {
                    displayedMonth = Month.DECEMBER
                    displayedYear--
                } else {
                    displayedMonth = Month.values()[displayedMonth.ordinal - 1]
                }
            }) {
                Icon(
                    Icons.Default.ChevronLeft,
                    contentDescription = stringResource(Res.string.previous_month),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "${displayedMonth.name.lowercase().replaceFirstChar { it.uppercase() }} $displayedYear",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            IconButton(onClick = {
                if (displayedMonth == Month.DECEMBER) {
                    displayedMonth = Month.JANUARY
                    displayedYear++
                } else {
                    displayedMonth = Month.values()[displayedMonth.ordinal + 1]
                }
            }) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = stringResource(Res.string.next_month),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Day-of-week headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(
                stringResource(Res.string.day_mon), stringResource(Res.string.day_tue), stringResource(Res.string.day_wed), stringResource(Res.string.day_thu),
                stringResource(Res.string.day_fri), stringResource(Res.string.day_sat), stringResource(Res.string.day_sun)
            ).forEach { name ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Calendar grid
        val firstOfMonth = LocalDate(displayedYear, displayedMonth, 1)
        val daysInMonth = when (displayedMonth) {
            Month.JANUARY, Month.MARCH, Month.MAY, Month.JULY,
            Month.AUGUST, Month.OCTOBER, Month.DECEMBER -> 31
            Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30
            Month.FEBRUARY -> if (displayedYear % 4 == 0 && (displayedYear % 100 != 0 || displayedYear % 400 == 0)) 29 else 28
        }
        // Monday = 0, Sunday = 6
        val startDayOffset = (firstOfMonth.dayOfWeek.ordinal) // MONDAY=0

        // Build rows of 7
        val totalCells = startDayOffset + daysInMonth
        val rows = (totalCells + 6) / 7

        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (col in 0..6) {
                    val cellIndex = row * 7 + col
                    val dayNum = cellIndex - startDayOffset + 1

                    if (dayNum in 1..daysInMonth) {
                        val date = LocalDate(displayedYear, displayedMonth, dayNum)
                        val isToday = date == today
                        val isSelected = date == selectedDate
                        val entryCount = entryCountByDate[date] ?: 0

                        MonthCalendarDayItem(
                            dayNumber = dayNum,
                            isToday = isToday,
                            isSelected = isSelected,
                            entryCount = entryCount,
                            modifier = Modifier.weight(1f),
                            onClick = { onDayClick(date) }
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthCalendarDayItem(
    dayNumber: Int,
    isToday: Boolean,
    isSelected: Boolean,
    entryCount: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val indicatorColor = when {
        entryCount >= 3 -> DSColors.CompletedGreen
        entryCount >= 1 -> DSColors.CompletedOrange
        else -> Color.Transparent
    }

    val bgColor = when {
        isSelected -> DSColors.Primary
        entryCount > 0 -> indicatorColor.copy(alpha = 0.15f)
        else -> Color.Transparent
    }

    val textColor = when {
        isSelected -> Color.White
        isToday -> DSColors.Primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(bgColor)
                .then(
                    if (isToday && !isSelected)
                        Modifier.border(1.5.dp, DSColors.Primary, RoundedCornerShape(10.dp))
                    else Modifier
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = dayNumber.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = textColor
                )
            }
        }

        // Entry count dot indicator below the day
        if (entryCount > 0 && !isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 1.dp)
                    .size(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(indicatorColor)
            )
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    subtitle: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    badge: String? = null,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(120.dp),
        shape = ShapeTokens.actionCard,
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(SpacingTokens.cardPadding)
        ) {
            Column(
                modifier = Modifier.align(Alignment.BottomStart),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            if (icon != null) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.TopEnd).size(32.dp),
                    tint = Color.White.copy(alpha = 0.4f)
                )
            }

            if (badge != null) {
                Surface(
                    modifier = Modifier.align(Alignment.TopStart),
                    shape = ShapeTokens.pill,
                    color = Color.White.copy(alpha = 0.25f)
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PillNavigationBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = ShapeTokens.pill,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }
    }
}

@Composable
fun PillNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (selected) DSColors.Primary else Color.Transparent
    val contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = ShapeTokens.pill,
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
            if (selected) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = contentColor
                )
            }
        }
    }
}
