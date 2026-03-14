package com.booking.worktracker.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.booking.worktracker.core.generated.resources.*
import com.booking.worktracker.ui.designsystem.DSTheme
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 600)
    )

    LaunchedEffect(Unit) {
        visible = true
        delay(5000)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DSTheme.colors.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(alpha)
        ) {
            Text(
                text = stringResource(Res.string.splash_app_name),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = DSTheme.colors.onPrimary
            )

            Spacer(modifier = Modifier.height(DSTheme.spacing.small))

            Text(
                text = stringResource(Res.string.splash_slogan),
                style = MaterialTheme.typography.bodyLarge,
                color = DSTheme.colors.onPrimary.copy(alpha = 0.8f)
            )
        }
    }
}
