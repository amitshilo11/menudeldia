package com.amitshilo.menudeldia.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Palette ──────────────────────────────────────────────────────────────────
// Primary  — warm saffron-orange (appetising, high-energy)
private val Orange10 = Color(0xFF3A0C00)
private val Orange20 = Color(0xFF611800)
private val Orange30 = Color(0xFF8C2700)
private val Orange40 = Color(0xFFB93800)   // primary light
private val Orange80 = Color(0xFFFFB59B)
private val Orange90 = Color(0xFFFFDBCB)
private val Orange95 = Color(0xFFFFEDE5)

// Secondary — warm terracotta-brown (earth, bread)
private val Brown10 = Color(0xFF2C1200)
private val Brown20 = Color(0xFF48210A)
private val Brown30 = Color(0xFF65341A)
private val Brown40 = Color(0xFF84482C)   // secondary light
private val Brown80 = Color(0xFFFFB78E)
private val Brown90 = Color(0xFFFFDCBF)

// Tertiary — herb olive-green (freshness, salad)
private val Olive10 = Color(0xFF1A1D00)
private val Olive40 = Color(0xFF535B00)
private val Olive80 = Color(0xFFCAD16E)
private val Olive90 = Color(0xFFE7EE88)

// Neutrals — warm greige (not cold grey)
private val Neutral10 = Color(0xFF1F1A17)
private val Neutral20 = Color(0xFF352F2B)
private val Neutral90 = Color(0xFFEDE0D9)
private val Neutral95 = Color(0xFFFBF3EF)
private val Neutral99 = Color(0xFFFFFBFF)

private val NeutralVar30 = Color(0xFF52433D)
private val NeutralVar50 = Color(0xFF87736C)
private val NeutralVar80 = Color(0xFFD8C2BB)
private val NeutralVar90 = Color(0xFFF5DDD6)

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

    tertiary = Olive40,
    onTertiary = Color.White,
    tertiaryContainer = Olive90,
    onTertiaryContainer = Olive10,

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

    inverseSurface = Neutral20,
    inverseOnSurface = Neutral90,
    inversePrimary = Orange80,
)

// ── Dark scheme ──────────────────────────────────────────────────────────────
private val DarkColors = darkColorScheme(
    primary = Orange80,
    onPrimary = Orange20,
    primaryContainer = Orange30,
    onPrimaryContainer = Orange90,

    secondary = Brown80,
    onSecondary = Brown20,
    secondaryContainer = Brown30,
    onSecondaryContainer = Brown90,

    tertiary = Olive80,
    onTertiary = Olive10,
    tertiaryContainer = Olive40,
    onTertiaryContainer = Olive90,

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

// ── Theme entry point ─────────────────────────────────────────────────────────
@Composable
fun MenuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
