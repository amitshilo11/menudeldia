package com.amitshilo.menudeldia.ui.map

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal class BubblePinShape(
    private val cornerRadius: Dp,
    private val notchWidth: Dp,
    private val notchHeight: Dp,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val cr = with(density) { cornerRadius.toPx() }
        val nw = with(density) { notchWidth.toPx() }
        val nh = with(density) { notchHeight.toPx() }
        val pillBottom = size.height - nh
        val cx = size.width / 2f
        val path = Path().apply {
            moveTo(cr, 0f)
            lineTo(size.width - cr, 0f)
            arcTo(Rect(size.width - 2 * cr, 0f, size.width, 2 * cr), -90f, 90f, false)
            lineTo(size.width, pillBottom - cr)
            arcTo(
                Rect(size.width - 2 * cr, pillBottom - 2 * cr, size.width, pillBottom),
                0f,
                90f,
                false
            )
            lineTo(cx + nw / 2, pillBottom)
            lineTo(cx, size.height)
            lineTo(cx - nw / 2, pillBottom)
            lineTo(cr, pillBottom)
            arcTo(Rect(0f, pillBottom - 2 * cr, 2 * cr, pillBottom), 90f, 90f, false)
            lineTo(0f, cr)
            arcTo(Rect(0f, 0f, 2 * cr, 2 * cr), 180f, 90f, false)
            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * MarkerComposable bakes its content into a static bitmap once (cached by its `keys`), so
 * animating anything inside that content lambda never redraws. `alpha` is the one marker
 * property maps-compose applies reactively to the live native marker on every recomposition, so
 * that's what drives the fade for newly added pins.
 */
@Composable
internal fun rememberPinAppearAlpha(): Float {
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        alpha.animateTo(1f, tween(MapDefaults.pinFadeInAnimMs))
    }
    return alpha.value
}

@Composable
internal fun DotMarker() {
    Box(
        modifier = Modifier
            .size(12.dp)
            .shadow(2.dp, CircleShape)
            .background(MaterialTheme.colorScheme.primary, CircleShape)
            .border(1.5.dp, Color.White, CircleShape),
    )
}

@Composable
internal fun PriceMarker(
    emoji: String,
    price: Double?,
    isSelected: Boolean,
    hasMenu: Boolean,
) {
    val shape =
        remember { BubblePinShape(cornerRadius = 16.dp, notchWidth = 12.dp, notchHeight = 8.dp) }
    val bgColor = Color.White
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        !hasMenu -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurface
    }
    val borderModifier = when {
        isSelected -> Modifier.border(2.dp, MaterialTheme.colorScheme.primary, shape)
        else -> Modifier.border(0.5.dp, Color(0xFFCCCCCC), shape)
    }

    Box(
        modifier = Modifier
            .shadow(elevation = if (isSelected) 8.dp else 3.dp, shape = shape)
            .background(color = bgColor, shape = shape)
            .then(borderModifier)
            .padding(start = 10.dp, end = 10.dp, top = 6.dp, bottom = 14.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = emoji, fontSize = 15.sp)
            if (price != null) {
                val cents = (price * 100).toLong()
                val priceStr = "${cents / 100}.${(cents % 100).toString().padStart(2, '0')}"
                Text(
                    text = "€$priceStr",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                )
            }
        }
    }
}
