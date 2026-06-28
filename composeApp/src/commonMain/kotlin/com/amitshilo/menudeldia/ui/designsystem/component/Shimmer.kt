package com.amitshilo.menudeldia.ui.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer

@Composable
fun rememberMenuShimmer() = rememberShimmer(shimmerBounds = ShimmerBounds.Window)

@Composable
fun ShimmerBone(modifier: Modifier, shape: Shape = RoundedCornerShape(8.dp)) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
    )
}

@Composable
fun Modifier.menuShimmer(shimmer: com.valentinilk.shimmer.Shimmer): Modifier =
    this.shimmer(shimmer)
