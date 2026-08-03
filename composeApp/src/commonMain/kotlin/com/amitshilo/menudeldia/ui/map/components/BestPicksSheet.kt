package com.amitshilo.menudeldia.ui.map.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amitshilo.menudeldia.domain.model.Restaurant
import com.amitshilo.menudeldia.ui.preview.previewRestaurants
import com.amitshilo.menudeldia.ui.theme.MenuTheme
import menudeldia.composeapp.generated.resources.Res
import menudeldia.composeapp.generated.resources.best_picks_title
import org.jetbrains.compose.resources.stringResource

// ── Sheet ─────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BestPicksSheet(
    picks: List<Restaurant>,
    onDismiss: () -> Unit,
    onPickTap: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        BestPicksContent(
            picks = picks,
            onPickTap = { id ->
                onDismiss()
                onPickTap(id)
            },
        )
    }
}

// ── Content ───────────────────────────────────────────────────────────────────

@Composable
private fun BestPicksContent(
    picks: List<Restaurant>,
    onPickTap: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Text(
            text = stringResource(Res.string.best_picks_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "${picks.size} great options, every day near you.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(20.dp))
        picks.forEachIndexed { index, restaurant ->
            BestPickCard(
                restaurant = restaurant,
                pickType = pickTypeAt(index),
                onClick = { onPickTap(restaurant.id) },
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }
        Spacer(Modifier.height(4.dp))
        UpdatedFooter()
        Spacer(Modifier.navigationBarsPadding().height(16.dp))
    }
}

// ── Footer ────────────────────────────────────────────────────────────────────

@Composable
private fun UpdatedFooter() {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // TODO: Icon(painterResource(Res.drawable.clock), tint = primary, modifier = Modifier.size(20.dp))
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text("⏰", fontSize = 10.sp)
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Updated daily at 11:00",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Fresh deals from real restaurants",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            // TODO: Icon(painterResource(Res.drawable.sparkle), tint = primary, modifier = Modifier.size(20.dp))
            Text("✨", fontSize = 16.sp)
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@PreviewLightDark
@Composable
private fun PreviewBestPicksContent() {
    MenuTheme {
        Surface {
            BestPicksContent(
                picks = previewRestaurants.take(3),
                onPickTap = {},
            )
        }
    }
}
