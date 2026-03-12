package com.booking.worktracker.ui.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.booking.worktracker.ui.designsystem.tokens.ColorTokens
import com.booking.worktracker.ui.designsystem.tokens.TypographyTokens

private val LightColorScheme = lightColorScheme(
    primary = ColorTokens.Primary,
    onPrimary = ColorTokens.OnPrimary,
    primaryContainer = ColorTokens.PrimaryContainer,
    onPrimaryContainer = ColorTokens.OnPrimaryContainer,
    secondary = ColorTokens.Secondary,
    onSecondary = ColorTokens.OnSecondary,
    secondaryContainer = ColorTokens.SecondaryContainer,
    onSecondaryContainer = ColorTokens.OnSecondaryContainer,
    tertiary = ColorTokens.Tertiary,
    onTertiary = ColorTokens.OnTertiary,
    tertiaryContainer = ColorTokens.TertiaryContainer,
    onTertiaryContainer = ColorTokens.OnTertiaryContainer,
    background = ColorTokens.Background,
    onBackground = ColorTokens.OnBackground,
    surface = ColorTokens.Surface,
    onSurface = ColorTokens.OnSurface,
    surfaceVariant = ColorTokens.SurfaceVariant,
    onSurfaceVariant = ColorTokens.OnSurfaceVariant,
    error = ColorTokens.Error,
    onError = ColorTokens.OnError,
    errorContainer = ColorTokens.ErrorContainer,
    onErrorContainer = ColorTokens.OnErrorContainer,
    outline = ColorTokens.Outline,
    outlineVariant = ColorTokens.OutlineVariant,
    inverseSurface = ColorTokens.InverseSurface,
    inverseOnSurface = ColorTokens.InverseOnSurface,
    inversePrimary = ColorTokens.InversePrimary,
    scrim = ColorTokens.Scrim
)

@Composable
fun WorkTrackerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = TypographyTokens.typography,
        content = content
    )
}
