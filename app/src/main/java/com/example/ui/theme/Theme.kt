package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val CockpitDarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = Color.Black,
    primaryContainer = ElectricCyanDim,
    onPrimaryContainer = Color.White,
    secondary = ElectricBlue,
    onSecondary = Color.Black,
    tertiary = EvGreen,
    onTertiary = Color.Black,
    background = CockpitBackground,
    onBackground = TextPrimary,
    surface = CockpitSurface,
    onSurface = TextPrimary,
    surfaceVariant = CockpitSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = CockpitSurfaceHighlight,
    error = AlertRed,
    onError = Color.White
)

val CockpitDayColorScheme = lightColorScheme(
    primary = Color(0xFF007791),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCCE8F2),
    onPrimaryContainer = Color(0xFF002028),
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    tertiary = Color(0xFF059669),
    onTertiary = Color.White,
    background = DayBackground,
    onBackground = DayTextPrimary,
    surface = DaySurface,
    onSurface = DayTextPrimary,
    surfaceVariant = DaySurfaceElevated,
    onSurfaceVariant = DayTextSecondary,
    outline = Color(0xFFCBD5E1),
    error = AlertRed,
    onError = Color.White
)

/**
 * Ultra-low glare night driving color scheme.
 * Uses pure OLED pitch black (#000000) and soft warm amber/sky tones
 * to preserve driver scotopic (night) dark adaptation and eliminate windshield reflections.
 */
val CockpitAntiGlareNightColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF0F2B48),
    onPrimaryContainer = Color(0xFFBAE6FD),
    secondary = Color(0xFFF59E0B),
    onSecondary = Color.Black,
    tertiary = Color(0xFF10B981),
    onTertiary = Color.Black,
    background = Color(0xFF000000),
    onBackground = Color(0xFFCBD5E1),
    surface = Color(0xFF050811),
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF0B101E),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF1E293B),
    error = AlertRed,
    onError = Color.White
)

@Composable
fun HPlayTheme(
    forceNightMode: Boolean? = null,
    isAntiGlareDeepNight: Boolean = true,
    content: @Composable () -> Unit
) {
    val isDark = forceNightMode ?: true // Automotive head units default to high-contrast dark
    val colorScheme = when {
        !isDark -> CockpitDayColorScheme
        isAntiGlareDeepNight -> CockpitAntiGlareNightColorScheme
        else -> CockpitDarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
