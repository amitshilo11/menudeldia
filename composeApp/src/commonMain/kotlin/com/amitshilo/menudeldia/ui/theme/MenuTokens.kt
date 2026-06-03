package com.amitshilo.menudeldia.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Design tokens sourced from menudiz-design/colors_and_type.css.
// Use these instead of raw literal dp/ms values throughout the UI.

// ── Spacing — 4-pt grid ───────────────────────────────────────────────────────
object MenuSpacing {
    val xs   =  4.dp   // --space-1
    val sm   =  8.dp   // --space-2
    val md   = 12.dp   // --space-3
    val lg   = 16.dp   // --space-4
    val xl   = 20.dp   // --space-5
    val xxl  = 24.dp   // --space-6
    val xxxl = 32.dp   // --space-7
    val huge = 40.dp   // --space-8
    val max  = 48.dp   // --space-9
    val sheetPeek = 160.dp  // bottom sheet peek height
}

// ── Corner radii ─────────────────────────────────────────────────────────────
object MenuRadius {
    val chip   = 12.dp   // --r-chip   filter chips, thumbnails
    val button = 16.dp   // --r-button action buttons in detail card
    val card   = 20.dp   // --r-card   restaurant cards
    val rating = 24.dp   // --r-rating rating / open-status badges
    val sheet  = 32.dp   // --r-sheet  bottom sheet top corners
    val pill   = 999.dp  // --r-pill   search bar, filter chips
}

// ── Motion — durations in ms ─────────────────────────────────────────────────
object MenuMotion {
    const val fast   = 150  // --dur-fast
    const val medium = 300  // --dur-medium
    const val slow   = 600  // --dur-slow (also map camera user-move)
    const val mapSelection = 400  // --dur-map-selection  pin tap → recenter
    const val mapRecenter  = 400  // --dur-map-recenter   locate-me FAB
}

// ── Brand gradient — menudiz wordmark only ───────────────────────────────────
// green → olive → amber → saffron (left → right)
// Do NOT use this gradient on any UI surface — wordmark only.
object MenuBrand {
    val gradientGreen   = Color(0xFF2E8B3F)  // --brand-green
    val gradientOlive   = Color(0xFF7A8E33)  // --brand-olive
    val gradientAmber   = Color(0xFFC25F17)  // --brand-amber
    val gradientSaffron = Color(0xFFE68A1A)  // --brand-saffron
}
