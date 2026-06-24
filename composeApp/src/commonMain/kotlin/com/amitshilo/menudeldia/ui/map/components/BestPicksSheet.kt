package com.amitshilo.menudeldia.ui.map.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.amitshilo.menudeldia.domain.model.Restaurant
import com.amitshilo.menudeldia.ui.preview.previewRestaurants
import com.amitshilo.menudeldia.ui.theme.MenuTheme
import menudeldia.composeapp.generated.resources.Res
import menudeldia.composeapp.generated.resources.best_picks_subtitle
import menudeldia.composeapp.generated.resources.best_picks_title
import org.jetbrains.compose.resources.stringResource

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

@Composable
private fun BestPicksContent(
    picks: List<Restaurant>,
    onPickTap: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
    ) {
        Text(
            text = stringResource(Res.string.best_picks_title),
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(Res.string.best_picks_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        picks.forEach { restaurant ->
            RestaurantCard(
                restaurant = restaurant,
                isSelected = false,
                onClick = { onPickTap(restaurant.id) },
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        Spacer(Modifier.navigationBarsPadding().height(16.dp))
    }
}

// ── Previews ────────────────────────────────────────────────────────────────

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
