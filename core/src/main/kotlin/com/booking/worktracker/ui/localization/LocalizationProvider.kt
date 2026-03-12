package com.booking.worktracker.ui.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.key
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

@Composable
fun ProvideLocalization(
    locale: AppLocale,
    content: @Composable () -> Unit
) {
    val layoutDirection = if (locale.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    SideEffect {
        java.util.Locale.setDefault(java.util.Locale(locale.code))
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides layoutDirection,
    ) {
        key(locale) {
            content()
        }
    }
}
