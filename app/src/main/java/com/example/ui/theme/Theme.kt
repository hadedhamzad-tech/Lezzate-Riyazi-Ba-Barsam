package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.example.data.settings.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = JadeGreenDarkPrimary,
    onPrimary = JadeGreenDarkOnPrimary,
    primaryContainer = JadeGreenDarkPrimaryContainer,
    onPrimaryContainer = JadeGreenDarkOnPrimaryContainer,
    secondary = AmberDarkSecondary,
    onSecondary = AmberDarkOnSecondary,
    secondaryContainer = AmberDarkSecondaryContainer,
    onSecondaryContainer = AmberDarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant
)

private val LightColorScheme = lightColorScheme(
    primary = JadeGreenLightPrimary,
    onPrimary = JadeGreenLightOnPrimary,
    primaryContainer = JadeGreenLightPrimaryContainer,
    onPrimaryContainer = JadeGreenLightOnPrimaryContainer,
    secondary = AmberLightSecondary,
    onSecondary = AmberLightOnSecondary,
    secondaryContainer = AmberLightSecondaryContainer,
    onSecondaryContainer = AmberLightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant
)

@Composable
fun AppTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
