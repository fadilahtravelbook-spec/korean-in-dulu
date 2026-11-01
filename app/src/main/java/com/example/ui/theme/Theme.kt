package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = HaneulBlueLighter,
    onPrimary = InkBlack,
    primaryContainer = HaneulBlue,
    onPrimaryContainer = PureWhite,
    secondary = NunbitYellow,
    onSecondary = InkBlack,
    secondaryContainer = NunbitYellowDark,
    onSecondaryContainer = PureWhite,
    tertiary = SaessakGreen,
    background = DarkBg,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    error = SarangRed
)

private val LightColorScheme = lightColorScheme(
    primary = HaneulBlue,
    onPrimary = PureWhite,
    primaryContainer = HaneulContainer,
    onPrimaryContainer = HaneulOnContainer,
    secondary = NunbitYellow,
    onSecondary = InkBlack,
    secondaryContainer = NunbitYellowContainer,
    onSecondaryContainer = NunbitYellowDark,
    tertiary = SaessakGreen,
    background = SurfaceLight,
    surface = PureWhite,
    surfaceVariant = SurfaceVariantLight,
    onBackground = InkBlack,
    onSurface = InkBlack,
    onSurfaceVariant = TextSecondary,
    error = SarangRed,
    errorContainer = SarangRedContainer
)

@Composable
fun KoreanInDuluTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Backward-compatible alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = KoreanInDuluTheme(darkTheme = darkTheme, content = content)
