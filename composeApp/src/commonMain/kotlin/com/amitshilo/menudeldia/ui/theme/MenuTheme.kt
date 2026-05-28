package com.amitshilo.menudeldia.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Palette ──────────────────────────────────────────────────────────────────
// Primary  — Vibrant Saffron Orange (Appetising, high-energy)
private val Orange10 = Color(0xFF3E0E00)
private val Orange20 = Color(0xFF601A00)
private val Orange30 = Color(0xFF872900)
private val Orange40 = Color(0xFFD46642)   // Primary Light (Vibrant)
private val Orange80 = Color(0xFFFFB74D)   // Primary Dark (Soft Glow)
private val Orange90 = Color(0xFFFFDCC0)
private val Orange95 = Color(0xFFFFEDDE)

// Secondary — Mocha Brown (Indulgent, warm)
private val Brown10 = Color(0xFF2D1600)
private val Brown20 = Color(0xFF4D2700)
private val Brown30 = Color(0xFF6F3B00)
private val Brown40 = Color(0xFF6D4C41)   // Secondary Light
private val Brown80 = Color(0xFFD7CCC8)
private val Brown90 = Color(0xFFEFEBE9)

// Tertiary — Fresh Basil Green (Freshness, healthy)
private val Green10 = Color(0xFF002107)
private val Green40 = Color(0xFF388E3C)   // Tertiary Light
private val Green80 = Color(0xFFA5D6A7)
private val Green90 = Color(0xFFE8F5E9)

// Neutrals — Warm Cream & Greige
private val Neutral10 = Color(0xFF1F1B16)
private val Neutral20 = Color(0xFF35302A)
private val Neutral90 = Color(0xFFEAE1D9)
private val Neutral95 = Color(0xFFF8F0E8)
private val Neutral99 = Color(0xFFFFFBFF)

private val NeutralVar30 = Color(0xFF51443B)
private val NeutralVar50 = Color(0xFF84746A)
private val NeutralVar80 = Color(0xFFD6C3B7)
private val NeutralVar90 = Color(0xFFF3DFD2)

// Surface containers — warm cream tiers (prevents M3 from deriving lavender defaults)
private val CreamCard = Color(0xFFFFF8EE)
private val CreamSheet = Color(0xFFFBF3E8)
private val CreamHighest = Color(0xFFF2E6D4)

private val ErrorRed = Color(0xFFBA1A1A)
private val ErrorRedContainer = Color(0xFFFFDAD6)

// ── Light scheme ─────────────────────────────────────────────────────────────
private val LightColors = lightColorScheme(
    primary = Orange40,
    onPrimary = Color.White,
    primaryContainer = Orange90,
    onPrimaryContainer = Orange10,

    secondary = Brown40,
    onSecondary = Color.White,
    secondaryContainer = Brown90,
    onSecondaryContainer = Brown10,

    tertiary = Green40,
    onTertiary = Color.White,
    tertiaryContainer = Green90,
    onTertiaryContainer = Green10,

    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorRedContainer,
    onErrorContainer = Color(0xFF410002),

    background = Neutral99,
    onBackground = Neutral10,
    surface = Neutral99,
    onSurface = Neutral10,
    surfaceVariant = NeutralVar90,
    onSurfaceVariant = NeutralVar30,
    outline = NeutralVar50,
    outlineVariant = NeutralVar80,
    surfaceTint = Orange40,
    surfaceContainerLowest = Neutral99,
    surfaceContainerLow = CreamCard,
    surfaceContainer = CreamSheet,
    surfaceContainerHigh = Neutral95,
    surfaceContainerHighest = CreamHighest,

    inverseSurface = Neutral20,
    inverseOnSurface = Neutral90,
    inversePrimary = Orange80,
)

// ── Dark scheme ──────────────────────────────────────────────────────────────
// TODO: re-enable dark theme in next phase
/*
private val DarkColors = darkColorScheme(
    primary = Orange80,
    onPrimary = Orange20,
    primaryContainer = Orange30,
    onPrimaryContainer = Orange90,

    secondary = Brown80,
    onSecondary = Brown20,
    secondaryContainer = Brown30,
    onSecondaryContainer = Brown90,

    tertiary = Green80,
    onTertiary = Green10,
    tertiaryContainer = Green40,
    onTertiaryContainer = Green90,

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    background = Neutral10,
    onBackground = Neutral90,
    surface = Neutral10,
    onSurface = Neutral90,
    surfaceVariant = NeutralVar30,
    onSurfaceVariant = NeutralVar80,
    outline = NeutralVar50,
    outlineVariant = NeutralVar30,

    inverseSurface = Neutral90,
    inverseOnSurface = Neutral20,
    inversePrimary = Orange40,
)
*/

// ── Theme entry point ─────────────────────────────────────────────────────────
@Composable
fun MenuTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content,
    )
}
