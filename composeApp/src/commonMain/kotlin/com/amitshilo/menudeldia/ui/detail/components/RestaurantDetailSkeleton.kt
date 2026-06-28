package com.amitshilo.menudeldia.ui.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.amitshilo.menudeldia.ui.designsystem.component.ShimmerBone
import com.amitshilo.menudeldia.ui.designsystem.component.menuShimmer
import com.amitshilo.menudeldia.ui.designsystem.component.rememberMenuShimmer
import com.amitshilo.menudeldia.ui.theme.MenuTheme
import androidx.compose.ui.tooling.preview.PreviewLightDark

@Composable
fun RestaurantDetailSkeleton(modifier: Modifier = Modifier) {
    val shimmer = rememberMenuShimmer()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .menuShimmer(shimmer),
    ) {
        // Photo carousel placeholder
        ShimmerBone(
            modifier = Modifier.fillMaxWidth().height(240.dp),
            shape = RoundedCornerShape(0.dp),
        )

        Column(modifier = Modifier.padding(16.dp)) {
            // Restaurant name
            ShimmerBone(modifier = Modifier.fillMaxWidth(0.75f).height(28.dp))
            Spacer(Modifier.height(8.dp))
            // Meta row: cuisine · rating · distance
            ShimmerBone(modifier = Modifier.fillMaxWidth(0.55f).height(16.dp))
            Spacer(Modifier.height(6.dp))
            // Address
            ShimmerBone(modifier = Modifier.fillMaxWidth(0.85f).height(16.dp))
            Spacer(Modifier.height(12.dp))
            // Open status badge
            ShimmerBone(
                modifier = Modifier.width(180.dp).height(36.dp),
                shape = RoundedCornerShape(12.dp),
            )
            Spacer(Modifier.height(16.dp))
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ShimmerBone(
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                )
                ShimmerBone(
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                )
            }
            Spacer(Modifier.height(20.dp))
            // Menu card
            ShimmerBone(
                modifier = Modifier.fillMaxWidth().height(24.dp),
                shape = RoundedCornerShape(8.dp),
            )
            Spacer(Modifier.height(12.dp))
            repeat(4) {
                ShimmerBone(modifier = Modifier.fillMaxWidth(0.9f).height(14.dp))
                Spacer(Modifier.height(8.dp))
            }
            Spacer(Modifier.height(12.dp))
            ShimmerBone(modifier = Modifier.fillMaxWidth(0.9f).height(14.dp))
            Spacer(Modifier.height(8.dp))
            repeat(3) {
                ShimmerBone(modifier = Modifier.fillMaxWidth(0.8f).height(14.dp))
                Spacer(Modifier.height(8.dp))
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Preview ──────────────────────────────────────────────────────────────────

@PreviewLightDark
@Composable
private fun PreviewRestaurantDetailSkeleton() {
    MenuTheme { RestaurantDetailSkeleton() }
}
