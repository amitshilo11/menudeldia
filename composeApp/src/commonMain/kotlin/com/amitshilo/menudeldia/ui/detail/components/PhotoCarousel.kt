package com.amitshilo.menudeldia.ui.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import com.amitshilo.menudeldia.ui.designsystem.component.menuShimmer
import com.amitshilo.menudeldia.ui.designsystem.component.rememberMenuShimmer

@Composable
fun PhotoCarousel(
    photos: List<String>,
    thumbnailUrl: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val images = if (photos.isNotEmpty()) photos else listOfNotNull(thumbnailUrl)
    if (images.isEmpty()) {
        PhotoFallback(modifier = modifier.fillMaxWidth().height(240.dp))
        return
    }
    val shimmer = rememberMenuShimmer()
    if (images.size == 1) {
        SubcomposeAsyncImage(
            model = images[0],
            contentDescription = contentDescription,
            modifier = modifier.fillMaxWidth().height(240.dp),
            contentScale = ContentScale.Crop,
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .menuShimmer(shimmer)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                )
            },
        )
        return
    }
    val pagerState = rememberPagerState { images.size }
    Box(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().height(240.dp),
        ) { page ->
            SubcomposeAsyncImage(
                model = images[page],
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxWidth().height(240.dp),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .menuShimmer(shimmer)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                    )
                },
            )
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(images.size) { index ->
                val selected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .size(if (selected) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (selected) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                )
            }
        }
    }
}

@Composable
fun PhotoFallback(modifier: Modifier = Modifier, cornerShape: RoundedCornerShape? = null) {
    val base = modifier.background(MaterialTheme.colorScheme.primaryContainer)
    val shaped = if (cornerShape != null) base.clip(cornerShape) else base
    Box(modifier = shaped, contentAlignment = Alignment.Center) {
        Text(text = "🍽", fontSize = 64.sp)
    }
}
