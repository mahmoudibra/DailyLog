package com.booking.worktracker.ui.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.booking.worktracker.ui.designsystem.tokens.DSColors
import com.booking.worktracker.ui.designsystem.tokens.TypographyTokens

private val LightColorScheme = lightColorScheme(
    primary = DSColors.Primary,
    onPrimary = DSColors.OnPrimary,
    primaryContainer = DSColors.PrimaryContainer,
    onPrimaryContainer = DSColors.OnPrimaryContainer,
    secondary = DSColors.Secondary,
    onSecondary = DSColors.OnSecondary,
    secondaryContainer = DSColors.SecondaryContainer,
    onSecondaryContainer = DSColors.OnSecondaryContainer,
    tertiary = DSColors.Tertiary,
    onTertiary = DSColors.OnTertiary,
    tertiaryContainer = DSColors.TertiaryContainer,
    onTertiaryContainer = DSColors.OnTertiaryContainer,
    background = DSColors.Background,
    onBackground = DSColors.OnBackground,
    surface = DSColors.Surface,
    onSurface = DSColors.OnSurface,
    surfaceVariant = DSColors.SurfaceVariant,
    onSurfaceVariant = DSColors.OnSurfaceVariant,
    error = DSColors.Error,
    onError = DSColors.OnError,
    errorContainer = DSColors.ErrorContainer,
    onErrorContainer = DSColors.OnErrorContainer,
    outline = DSColors.Outline,
    outlineVariant = DSColors.OutlineVariant,
    inverseSurface = DSColors.InverseSurface,
    inverseOnSurface = DSColors.InverseOnSurface,
    inversePrimary = DSColors.InversePrimary,
    scrim = DSColors.Scrim
)

private val DarkColorScheme = darkColorScheme(
    primary = DSColors.DarkPrimary,
    onPrimary = DSColors.DarkOnPrimary,
    primaryContainer = DSColors.DarkPrimaryContainer,
    onPrimaryContainer = DSColors.DarkOnPrimaryContainer,
    secondary = DSColors.DarkSecondary,
    onSecondary = DSColors.DarkOnSecondary,
    secondaryContainer = DSColors.DarkSecondaryContainer,
    onSecondaryContainer = DSColors.DarkOnSecondaryContainer,
    tertiary = DSColors.DarkTertiary,
    onTertiary = DSColors.DarkOnTertiary,
    tertiaryContainer = DSColors.DarkTertiaryContainer,
    onTertiaryContainer = DSColors.DarkOnTertiaryContainer,
    background = DSColors.DarkBackground,
    onBackground = DSColors.DarkOnBackground,
    surface = DSColors.DarkSurface,
    onSurface = DSColors.DarkOnSurface,
    surfaceVariant = DSColors.DarkSurfaceVariant,
    onSurfaceVariant = DSColors.DarkOnSurfaceVariant,
    error = DSColors.DarkError,
    onError = DSColors.DarkOnError,
    errorContainer = DSColors.DarkErrorContainer,
    onErrorContainer = DSColors.DarkOnErrorContainer,
    outline = DSColors.DarkOutline,
    outlineVariant = DSColors.DarkOutlineVariant,
    inverseSurface = DSColors.DarkInverseSurface,
    inverseOnSurface = DSColors.DarkInverseOnSurface,
    inversePrimary = DSColors.DarkInversePrimary,
    scrim = DSColors.Scrim
)

@Composable
fun WorkTrackerTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TypographyTokens.typography,
        content = content
    )
}
