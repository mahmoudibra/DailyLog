package com.booking.worktracker.ui.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.booking.worktracker.ui.designsystem.tokens.ShapeTokens
import com.booking.worktracker.ui.designsystem.tokens.SpacingTokens

@Composable
fun DSCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = ShapeTokens.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(SpacingTokens.cardPadding)
            ) {
                content()
            }
        }
    } else {
        Card(
            modifier = modifier,
            shape = ShapeTokens.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(SpacingTokens.cardPadding)
            ) {
                content()
            }
        }
    }
}

@Composable
fun DSElevatedCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = modifier,
        shape = ShapeTokens.large
    ) {
        Column(
            modifier = Modifier.padding(SpacingTokens.cardPadding)
        ) {
            content()
        }
    }
}

@Composable
fun DSOutlinedCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    OutlinedCard(
        modifier = modifier,
        shape = ShapeTokens.large
    ) {
        Column(
            modifier = Modifier.padding(SpacingTokens.cardPadding)
        ) {
            content()
        }
    }
}
