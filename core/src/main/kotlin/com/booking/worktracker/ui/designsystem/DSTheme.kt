package com.booking.worktracker.ui.designsystem

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import com.booking.worktracker.ui.designsystem.tokens.ElevationTokens
import com.booking.worktracker.ui.designsystem.tokens.ShapeTokens
import com.booking.worktracker.ui.designsystem.tokens.SizeTokens
import com.booking.worktracker.ui.designsystem.tokens.SpacingTokens

object DSTheme {
    val colors: ColorScheme @Composable get() = MaterialTheme.colorScheme
    val font: Typography @Composable get() = MaterialTheme.typography
    val spacing: SpacingTokens get() = SpacingTokens
    val shapes: ShapeTokens get() = ShapeTokens
    val sizes: SizeTokens get() = SizeTokens
    val elevation: ElevationTokens get() = ElevationTokens
}
