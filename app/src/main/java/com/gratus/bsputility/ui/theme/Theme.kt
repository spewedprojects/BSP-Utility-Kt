package com.gratus.bsputility.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = IndustrialBlue500,
    onPrimary = Color.White,
    primaryContainer = IndustrialNavy800,
    onPrimaryContainer = Color.White,
    secondary = IndustrialAmber500,
    onSecondary = Color.Black,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    outline = IndustrialNavy700
)

private val LightColorScheme = lightColorScheme(
    primary = IndustrialNavy900,
    onPrimary = Color.White,
    primaryContainer = IndustrialNavy800,
    onPrimaryContainer = Color.White,
    secondary = IndustrialAmber600,
    onSecondary = Color.White,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onBackground = IndustrialNavy900,
    onSurface = IndustrialNavy900,
    outline = OutlineLight
)

@Composable
fun MyApplicationTheme(
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
