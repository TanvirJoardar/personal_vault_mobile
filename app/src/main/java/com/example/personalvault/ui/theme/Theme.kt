package com.example.personalvault.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AccentPrimary,
    onPrimary = Color.Black,
    secondary = AccentSecondary,
    onSecondary = Color.Black,
    tertiary = AccentPurple,
    background = VaultBgRoot,
    onBackground = TextPrimary,
    surface = VaultBgSurface,
    onSurface = TextPrimary,
    surfaceVariant = VaultBgCard,
    onSurfaceVariant = TextSecondary,
    outline = BorderDefault,
    error = AccentDanger
)

@Composable
fun PersonalVaultTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
