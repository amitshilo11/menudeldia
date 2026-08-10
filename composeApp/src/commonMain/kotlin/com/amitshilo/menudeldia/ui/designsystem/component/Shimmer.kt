package com.amitshilo.menudeldia.ui.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter

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

/**
 * A remote image that shimmers while it loads.
 *
 * Deliberately built on [AsyncImage] rather than Coil's `SubcomposeAsyncImage`: the subcompose
 * variant starts a whole subcomposition per call to host its `loading` slot, which is a real cost
 * once a scrolling list is fling-inflating cards — most visibly on iOS. Here the placeholder is an
 * ordinary sibling, so a loaded image is a single layout node and the infinite shimmer animation
 * only exists while there is actually something to wait for.
 */
@Composable
fun ShimmerAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    var isPending by remember(model) { mutableStateOf(true) }
    Box(modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest)) {
        if (isPending) {
            ShimmerOverlay(Modifier.matchParentSize())
        }
        AsyncImage(
            model = model,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = Modifier.fillMaxSize(),
            onState = { state ->
                isPending = state is AsyncImagePainter.State.Loading ||
                        state is AsyncImagePainter.State.Empty
            },
        )
    }
}

@Composable
private fun ShimmerOverlay(modifier: Modifier) {
    val shimmer = rememberMenuShimmer()
    Box(modifier.menuShimmer(shimmer))
}

private val ShimmerColors = listOf(
    Color.White.copy(alpha = 0f),
    Color.White.copy(alpha = 0.25f),
    Color.White.copy(alpha = 0f),
)

/**
 * The gradient is built once per size rather than once per frame: [Brush.linearGradient] allocates
 * a backing shader, so rebuilding it inside the draw lambda cost one allocation per shimmering
 * card per frame — with a list of loading cards on screen that was the bulk of the frame. The band
 * is swept by translating the canvas instead, which leaves the shader untouched.
 */
fun Modifier.menuShimmer(shimmer: State<Float>): Modifier = drawWithCache {
    val bandWidth = size.width.coerceAtLeast(1f)
    val brush = Brush.linearGradient(
        colors = ShimmerColors,
        start = Offset(-bandWidth, 0f),
        end = Offset(0f, size.height),
    )
    onDrawWithContent {
        drawContent()
        val shift = shimmer.value * bandWidth
        translate(left = shift) {
            drawRect(brush = brush, topLeft = Offset(-shift, 0f), size = size)
        }
    }
}
