package com.ecosort.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = EcoGreenPrimary,
    onPrimary = EcoSurface,
    primaryContainer = EcoGreenLight,
    onPrimaryContainer = EcoGreenDark,
    secondary = EcoEmerald,
    onSecondary = EcoSurface,
    tertiary = EcoAmber,
    onTertiary = EcoSurface,
    background = EcoBackground,
    onBackground = EcoTextPrimary,
    surface = EcoSurface,
    onSurface = EcoTextPrimary,
    surfaceVariant = EcoBackground,
    onSurfaceVariant = EcoTextSecondary,
    error = EcoError,
    onError = EcoSurface
)

private val DarkColorScheme = darkColorScheme(
    primary = EcoGreenPrimary,
    onPrimary = EcoSurface,
    secondary = EcoEmerald,
    background = EcoTextPrimary,
    surface = Color(0xFF111827)
)

@Composable
fun EcoSortTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
