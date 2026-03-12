package com.booking.worktracker.ui.designsystem.tokens

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object SpacingTokens {
    val extraSmall: Dp = 4.dp
    val small: Dp = 8.dp
    val medium: Dp = 12.dp
    val large: Dp = 16.dp
    val extraLarge: Dp = 24.dp

    // Numeric scale
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

    // Semantic spacing
    val screenPadding: Dp = 24.dp
    val cardPadding: Dp = 20.dp
    val sectionSpacing: Dp = 20.dp
    val itemSpacing: Dp = 12.dp
}

object ElevationTokens {
    val none: Dp = 0.dp
    val low: Dp = 1.dp
    val medium: Dp = 3.dp
    val high: Dp = 6.dp
    val highest: Dp = 8.dp
}

object ShapeTokens {
    val extraSmall = RoundedCornerShape(4.dp)
    val small = RoundedCornerShape(8.dp)
    val medium = RoundedCornerShape(16.dp)
    val large = RoundedCornerShape(20.dp)
    val extraLarge = RoundedCornerShape(28.dp)
    val full = RoundedCornerShape(50)
    val pill = RoundedCornerShape(50)
    val actionCard = RoundedCornerShape(24.dp)
}

object SizeTokens {
    val iconSmall: Dp = 16.dp
    val iconMedium: Dp = 24.dp
    val iconLarge: Dp = 32.dp
    val buttonHeight: Dp = 40.dp
    val inputHeight: Dp = 56.dp
    val cardMinHeight: Dp = 64.dp
    val navBarHeight: Dp = 64.dp
    val calendarDaySize: Dp = 40.dp
}
