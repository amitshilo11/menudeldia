package com.amitshilo.menudeldia.ui.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

// Hand-rolled shimmer: com.valentinilk.shimmer draws via a raw Skia Shader cast that
// crashes on iOS (ClassCastException) against current Compose Multiplatform Skiko builds.
@Composable
fun rememberMenuShimmer(): State<Float> {
    val transition = rememberInfiniteTransition(label = "menuShimmer")
    return transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "menuShimmerProgress",
    )
}

@Composable
fun ShimmerBone(modifier: Modifier, shape: Shape = RoundedCornerShape(8.dp)) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
    )
}

fun Modifier.menuShimmer(shimmer: State<Float>): Modifier = drawWithContent {
    drawContent()
    val progress = shimmer.value
    val bandWidth = size.width.coerceAtLeast(1f)
    val brush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0f),
            Color.White.copy(alpha = 0.25f),
            Color.White.copy(alpha = 0f),
        ),
        start = Offset(progress * bandWidth - bandWidth, 0f),
        end = Offset(progress * bandWidth, size.height),
    )
    drawRect(brush = brush)
}
