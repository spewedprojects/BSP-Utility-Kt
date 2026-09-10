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
    inversePrimary = IndustrialNavy900,
    secondary = IndustrialAmber500,
    onSecondary = Color.White,
    secondaryContainer = IndustrialAmber900,
    onSecondaryContainer = IndustrialAmber100,
    tertiary = IndustrialTeal400,
    onTertiary = IndustrialNavy900,
    tertiaryContainer = IndustrialTeal900,
    onTertiaryContainer = IndustrialTeal100,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    surfaceDim = SurfaceDimDark,
    surfaceBright = SurfaceBrightDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
    inverseSurface = TextPrimaryDark,
    inverseOnSurface = IndustrialNavy900,
    outline = IndustrialNavy700,
    outlineVariant = OutlineVariantDark,
    error = IndustrialRed400,
    onError = IndustrialRed950,
    errorContainer = IndustrialRed900,
    onErrorContainer = IndustrialRed100,
    scrim = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = IndustrialNavy900,
    onPrimary = Color.White,
    primaryContainer = IndustrialNavy800,
    onPrimaryContainer = Color.White,
    inversePrimary = IndustrialBlue500,
    secondary = IndustrialAmber600,
    onSecondary = Color.Black,
    secondaryContainer = IndustrialAmber100,
    onSecondaryContainer = IndustrialAmber900,
    tertiary = IndustrialTeal600,
    onTertiary = Color.White,
    tertiaryContainer = IndustrialTeal100,
    onTertiaryContainer = IndustrialTeal800,
    background = BackgroundLight,
    onBackground = IndustrialNavy900,
    surface = SurfaceLight,
    onSurface = IndustrialNavy900,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    surfaceDim = SurfaceDimLight,
    surfaceBright = SurfaceBrightLight,
    surfaceContainerLowest = SurfaceContainerLowestLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighestLight,
    inverseSurface = IndustrialNavy900,
    inverseOnSurface = TextPrimaryDark,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    error = IndustrialRed600,
    onError = Color.White,
    errorContainer = IndustrialRed100,
    onErrorContainer = IndustrialRed900,
    scrim = Color.Black
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
