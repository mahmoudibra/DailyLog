package com.booking.worktracker.ui.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

val LocalStrings = staticCompositionLocalOf<Strings> { EnglishStrings }

@Composable
fun ProvideLocalization(
    locale: AppLocale,
    content: @Composable () -> Unit
) {
    val strings: Strings = when (locale) {
        AppLocale.ENGLISH -> EnglishStrings
        AppLocale.ARABIC -> ArabicStrings
    }

    val layoutDirection = if (locale.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(
        LocalStrings provides strings,
        LocalLayoutDirection provides layoutDirection,
        content = content
    )
}
