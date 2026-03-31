package com.yulapps.dailylockscreen.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DailyLockscreenColors = darkColorScheme(
    primary = AccentPrimary,
    secondary = AccentSecondary,
    tertiary = SuccessTone,
    background = BackgroundPrimary,
    surface = SurfacePrimary,
    surfaceVariant = SurfaceSecondary,
    surfaceTint = AccentPrimary,
    onPrimary = BackgroundPrimary,
    onSecondary = BackgroundPrimary,
    onTertiary = BackgroundPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = DividerTone,
    error = ErrorTone,
    onError = BackgroundPrimary,
)

@Composable
fun DailyLockscreenTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DailyLockscreenColors,
        typography = DailyLockscreenTypography,
        content = content,
    )
}
