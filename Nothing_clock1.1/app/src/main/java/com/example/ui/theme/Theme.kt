package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NothingDarkColorScheme = darkColorScheme(
    primary = PureWhite,
    onPrimary = AmoledBlack,
    secondary = MutedGrey,
    onSecondary = PureWhite,
    tertiary = NothingRed,
    onTertiary = PureWhite,
    background = AmoledBlack,
    onBackground = PureWhite,
    surface = AmoledBlack,
    onSurface = PureWhite,
    surfaceVariant = SurfaceGrey,
    onSurfaceVariant = MutedGrey,
    outline = BorderGrey
)

@Composable
fun NothingClockTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NothingDarkColorScheme,
        typography = Typography,
        content = content
    )
}
