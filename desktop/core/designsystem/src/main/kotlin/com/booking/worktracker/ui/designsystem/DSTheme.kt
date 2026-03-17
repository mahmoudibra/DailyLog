package com.booking.worktracker.ui.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.booking.worktracker.ui.designsystem.tokens.DSColors

object DSTheme {
    val colors: ColorScheme @Composable get() = MaterialTheme.colorScheme
    val font: Typography @Composable get() = MaterialTheme.typography

    // Custom colors (not part of Material ColorScheme)
    val cardBlue: Color get() = DSColors.CardBlue
    val cardGreen: Color get() = DSColors.CardGreen
    val cardOrange: Color get() = DSColors.CardOrange
    val cardPurple: Color get() = DSColors.CardPurple
    val completedGreen: Color get() = DSColors.CompletedGreen
    val completedOrange: Color get() = DSColors.CompletedOrange
    val completedPink: Color get() = DSColors.CompletedPink
    val incompleteGray: Color get() = DSColors.IncompleteGray
    val tagColors: List<Color> get() = DSColors.TagColors

    object spacing {
        val extraSmall: Dp = 4.dp
        val small: Dp = 8.dp
        val medium: Dp = 12.dp
        val large: Dp = 16.dp
        val extraLarge: Dp = 24.dp
        val space0: Dp = 0.dp
        val space1: Dp = 4.dp
        val space2: Dp = 8.dp
        val space3: Dp = 12.dp
        val space4: Dp = 16.dp
        val space6: Dp = 24.dp
        val space8: Dp = 32.dp
        val space10: Dp = 40.dp
        val space12: Dp = 48.dp
        val space16: Dp = 64.dp
        val space20: Dp = 80.dp
        val space24: Dp = 96.dp
        val screenPadding: Dp = 24.dp
        val cardPadding: Dp = 20.dp
        val sectionSpacing: Dp = 20.dp
        val itemSpacing: Dp = 12.dp
    }

    object shapes {
        val extraSmall = RoundedCornerShape(4.dp)
        val small = RoundedCornerShape(8.dp)
        val medium = RoundedCornerShape(16.dp)
        val large = RoundedCornerShape(20.dp)
        val extraLarge = RoundedCornerShape(28.dp)
        val full = RoundedCornerShape(50)
        val pill = RoundedCornerShape(50)
        val actionCard = RoundedCornerShape(24.dp)
    }

    object sizes {
        val iconSmall: Dp = 16.dp
        val iconMedium: Dp = 24.dp
        val iconLarge: Dp = 32.dp
        val buttonHeight: Dp = 40.dp
        val inputHeight: Dp = 56.dp
        val cardMinHeight: Dp = 64.dp
        val navBarHeight: Dp = 64.dp
        val calendarDaySize: Dp = 40.dp
    }

    object elevation {
        val none: Dp = 0.dp
        val low: Dp = 1.dp
        val medium: Dp = 3.dp
        val high: Dp = 6.dp
        val highest: Dp = 8.dp
    }
}
