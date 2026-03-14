package com.booking.worktracker.ui.navigation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WorkHistory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.dp
import com.booking.worktracker.ui.Screen
import com.booking.worktracker.ui.designsystem.DSTheme
import com.booking.worktracker.core.generated.resources.*
import org.jetbrains.compose.resources.stringResource

private val COLLAPSED_WIDTH = 64.dp
private val EXPANDED_WIDTH = 220.dp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SideNavBar(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
    navItems: List<NavItem>,
    dividerAfterIndex: Int,
) {
    var isExpanded by remember { mutableStateOf(false) }
    val width by animateDpAsState(
        targetValue = if (isExpanded) EXPANDED_WIDTH else COLLAPSED_WIDTH,
        animationSpec = tween(durationMillis = 200)
    )

    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(width)
            .onPointerEvent(PointerEventType.Enter) { isExpanded = true }
            .onPointerEvent(PointerEventType.Exit) { isExpanded = false },
        color = DSTheme.colors.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp, horizontal = 8.dp)
        ) {
            SideNavItem(
                icon = Icons.Default.WorkHistory,
                label = stringResource(Res.string.daily_tracker),
                selected = false,
                expanded = isExpanded,
                onClick = { onScreenSelected(Screen.DAILY_LOG) },
                isTitle = true
            )

            Spacer(Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                navItems.forEachIndexed { index, item ->
                    if (index == dividerAfterIndex + 1) {
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            color = DSTheme.colors.outlineVariant
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    SideNavItem(
                        icon = item.icon,
                        label = item.label,
                        selected = currentScreen == item.screen,
                        expanded = isExpanded,
                        onClick = { onScreenSelected(item.screen) }
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 8.dp),
                color = DSTheme.colors.outlineVariant
            )
            Spacer(Modifier.height(4.dp))

            SideNavItem(
                icon = Icons.Default.Settings,
                label = stringResource(Res.string.nav_settings),
                selected = currentScreen == Screen.SETTINGS,
                expanded = isExpanded,
                onClick = { onScreenSelected(Screen.SETTINGS) }
            )
        }
    }
}
