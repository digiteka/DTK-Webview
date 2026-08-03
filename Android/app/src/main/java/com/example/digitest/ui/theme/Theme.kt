package com.example.digitest.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DigiColorScheme = darkColorScheme(
    primary = DigiBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1A3A6E),
    onPrimaryContainer = Color.White,
    secondary = DigiBlueLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1A2A3A),
    onSecondaryContainer = Color.White,
    background = DigiBlack,
    onBackground = DigiTextPrimary,
    surface = DigiSurface,
    onSurface = DigiTextPrimary,
    surfaceVariant = DigiCard,
    onSurfaceVariant = DigiTextSecondary,
    outline = DigiCardBorder,
    error = DigiError,
    onError = Color.White,
    surfaceContainer = DigiCard,
    surfaceContainerHigh = Color(0xFF222222),
    surfaceContainerLow = Color(0xFF111111),
    inverseSurface = Color(0xFFE6E1E5),
    inverseOnSurface = Color(0xFF1C1B1F),
)

@Composable
fun DIGITESTTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DigiColorScheme,
        typography = Typography,
        content = content
    )
}
