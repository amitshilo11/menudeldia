package com.amitshilo.menudeldia.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Material 3 type scale sourced from menudiz-design/colors_and_type.css.
// Font families (DM Sans / Quicksand / Spectral) deferred to task U17.
internal val MenuTypography = Typography(
    displayLarge = TextStyle(
        fontSize = 57.sp, fontWeight = FontWeight.Medium,
        lineHeight = 60.sp, letterSpacing = (-1.14f).sp,
    ),
    displayMedium = TextStyle(
        fontSize = 45.sp, fontWeight = FontWeight.Medium,
        lineHeight = 49.sp, letterSpacing = (-0.45f).sp,
    ),
    displaySmall = TextStyle(
        fontSize = 36.sp, fontWeight = FontWeight.Medium,
        lineHeight = 40.sp, letterSpacing = (-0.18f).sp,
    ),
    headlineLarge = TextStyle(
        fontSize = 32.sp, fontWeight = FontWeight.Bold,
        lineHeight = 37.sp, letterSpacing = (-0.32f).sp,
    ),
    headlineMedium = TextStyle(
        fontSize = 28.sp, fontWeight = FontWeight.Bold,
        lineHeight = 34.sp, letterSpacing = (-0.14f).sp,
    ),
    headlineSmall = TextStyle(
        fontSize = 24.sp, fontWeight = FontWeight.SemiBold,
        lineHeight = 30.sp, letterSpacing = 0.sp,
    ),
    titleLarge = TextStyle(
        fontSize = 22.sp, fontWeight = FontWeight.SemiBold,
        lineHeight = 28.sp, letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
        lineHeight = 22.sp, letterSpacing = 0.sp,
    ),
    titleSmall = TextStyle(
        fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
        lineHeight = 20.sp, letterSpacing = 0.14f.sp,
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp, fontWeight = FontWeight.Normal,
        lineHeight = 24.sp, letterSpacing = 0.sp,
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp, fontWeight = FontWeight.Normal,
        lineHeight = 20.sp, letterSpacing = 0.sp,
    ),
    bodySmall = TextStyle(
        fontSize = 12.sp, fontWeight = FontWeight.Normal,
        lineHeight = 17.sp, letterSpacing = 0.sp,
    ),
    labelLarge = TextStyle(
        fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
        lineHeight = 18.sp, letterSpacing = 0.14f.sp,
    ),
    labelMedium = TextStyle(
        fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
        lineHeight = 16.sp, letterSpacing = 0.24f.sp,
    ),
    labelSmall = TextStyle(
        fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
        lineHeight = 14.sp, letterSpacing = 0.33f.sp,
    ),
)
