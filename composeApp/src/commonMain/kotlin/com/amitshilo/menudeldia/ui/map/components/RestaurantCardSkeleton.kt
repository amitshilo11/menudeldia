package com.amitshilo.menudeldia.ui.map.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.amitshilo.menudeldia.ui.designsystem.component.ShimmerBone
import com.amitshilo.menudeldia.ui.designsystem.component.menuShimmer
import com.amitshilo.menudeldia.ui.designsystem.component.rememberMenuShimmer
import com.amitshilo.menudeldia.ui.theme.MenuTheme
import androidx.compose.ui.tooling.preview.PreviewLightDark

@Composable
fun RestaurantCardSkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column {
            ShimmerBone(
                modifier = Modifier.fillMaxWidth().height(160.dp),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            )
            Column(modifier = Modifier.padding(12.dp)) {
                ShimmerBone(modifier = Modifier.fillMaxWidth(0.7f).height(18.dp))
                Spacer(Modifier.height(8.dp))
                ShimmerBone(modifier = Modifier.width(140.dp).height(13.dp))
            }
        }
    }
}

// ── Preview ──────────────────────────────────────────────────────────────────

@PreviewLightDark
@Composable
private fun PreviewRestaurantCardSkeleton() {
    MenuTheme {
        val shimmer = rememberMenuShimmer()
        Box(Modifier.menuShimmer(shimmer).padding(16.dp)) {
            RestaurantCardSkeleton()
        }
    }
}
