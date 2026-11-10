package com.ptstudyzone.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ثيم فاخر: كحلي + ذهبي + تركوازي طبي
val Navy = Color(0xFF0B1F3A)
val NavyDeep = Color(0xFF071426)
val Gold = Color(0xFFC9A227)
val GoldLight = Color(0xFFE8C96A)
val TealMed = Color(0xFF0E9A8B)
val TealLight = Color(0xFF2EC4B6)
val BgLight = Color(0xFFF6F8FB)
val CardLight = Color(0xFFFFFFFF)
val BgDark = Color(0xFF0A1626)
val CardDark = Color(0xFF12263F)
val Danger = Color(0xFFE85D6D)
val Success = Color(0xFF1FA97A)

private val LightColors = lightColorScheme(
    primary = Navy, onPrimary = Color.White,
    secondary = TealMed, onSecondary = Color.White,
    tertiary = Gold, onTertiary = Navy,
    background = BgLight, onBackground = Navy,
    surface = CardLight, onSurface = Navy,
    surfaceVariant = Color(0xFFE9EEF5),
    error = Danger
)
private val DarkColors = darkColorScheme(
    primary = Gold, onPrimary = NavyDeep,
    secondary = TealLight, onSecondary = NavyDeep,
    tertiary = GoldLight, onTertiary = NavyDeep,
    background = BgDark, onBackground = Color(0xFFEAF1F8),
    surface = CardDark, onSurface = Color(0xFFEAF1F8),
    surfaceVariant = Color(0xFF1B3A5C),
    error = Danger
)

@Composable
fun PTStudyZoneTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors, content = content)
}
