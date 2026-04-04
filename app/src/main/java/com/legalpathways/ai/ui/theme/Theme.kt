package com.legalpathways.ai.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Enhanced Navy + Gold legal theme with better contrast ──────────────────
// Dark theme colors (improved contrast and visibility)
val NavyDeepDark    = Color(0xFF050D1A)  // Even darker background for contrast
val NavyMidDark     = Color(0xFF141F38)  // Improved surface color
val NavyLightDark   = Color(0xFF1E2D4A)  // Better container color
val GoldPrimaryDark = Color(0xFFE8B847)  // Brighter gold for dark theme
val GoldLightDark   = Color(0xFFF5C860)  // Lighter gold for better visibility
val GoldDarkDark    = Color(0xFFB8932D)  // Darker gold for depth

val SlateGray      = Color(0xFF8A9BB0)


// Light theme colors (unchanged, works well)
val NavyDeep       = Color(0xFF0A1628)
val NavyMid        = Color(0xFF1A2B4A)
val NavyLight      = Color(0xFF243B5E)
val GoldPrimary    = Color(0xFFD4A853)
val GoldLight      = Color(0xFFE8C37A)
val GoldDark       = Color(0xFFA8832E)

// Common accent colors (both themes)
val CrimsonAccent  = Color(0xFFE85555)  // Brighter red for better visibility
val EmeraldAccent  = Color(0xFF3FA967)  // Brighter emerald
val SlateGrayLight = Color(0xFFA5B4C8)  // Brighter for dark theme text
val SlateGrayDark  = Color(0xFF7A8BA0)  // For light theme

// Light theme support colors
val ParchmentLight = Color(0xFFF8F4ED)
val ParchmentMid   = Color(0xFFEFE8D8)
val SurfaceCard    = Color(0xFFFFFDF8)
val White          = Color(0xFFFFFFFF)
val DividerColor   = Color(0xFFE0D8C8)

// Dark theme support colors
val DarkText       = Color(0xFFE8DFD0)  // Off-white for dark theme text
val DarkSubtext    = Color(0xFFA8B4C4)  // Muted for dark theme secondary text
val DarkDivider    = Color(0xFF2A3A52)  // Dark divider color

val DarkColorScheme = darkColorScheme(
    primary          = GoldPrimaryDark,
    onPrimary        = NavyDeepDark,
    primaryContainer = NavyLightDark,
    onPrimaryContainer = GoldLightDark,

    secondary        = SlateGrayLight,
    onSecondary      = NavyDeepDark,
    secondaryContainer = NavyMidDark,
    onSecondaryContainer = DarkText,

    tertiary         = EmeraldAccent,
    onTertiary       = White,

    background       = NavyDeepDark,      // Very dark background
    onBackground     = DarkText,          // Light text on dark background

    surface          = NavyMidDark,       // Card surfaces
    onSurface        = DarkText,          // Text on surfaces (high contrast)

    surfaceVariant   = NavyLightDark,     // Variant surfaces
    onSurfaceVariant = DarkSubtext,       // Muted text for secondary info

    error            = CrimsonAccent,
    outline          = DarkDivider
)

val LightColorScheme = lightColorScheme(
    primary          = NavyMid,
    onPrimary        = White,
    primaryContainer = ParchmentMid,
    onPrimaryContainer = NavyDeep,

    secondary        = GoldPrimary,
    onSecondary      = NavyDeep,
    secondaryContainer = Color(0xFFFFF3DC),
    onSecondaryContainer = GoldDark,

    tertiary         = EmeraldAccent,
    onTertiary       = White,

    background       = ParchmentLight,
    onBackground     = NavyDeep,

    surface          = SurfaceCard,
    onSurface        = NavyDeep,

    surfaceVariant   = ParchmentMid,
    onSurfaceVariant = NavyLight,

    error            = CrimsonAccent,
    outline          = DividerColor
)

@Composable
fun LegalPathwaysTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = LegalTypography,
        content     = content
    )
}

// ── BONUS: Typography (add this if not already present) ──────────────────────
