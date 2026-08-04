package com.amitshilo.menudeldia.ui.map.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import menudeldia.composeapp.generated.resources.Res
import menudeldia.composeapp.generated.resources.my_location
import menudeldia.composeapp.generated.resources.recenter_on_me
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Sits above whatever currently covers the bottom of the map. Kept as its own composable so the
 * animation driving [bottomPadding] only ever recomposes the FAB, never the map or the sheet.
 */
@Composable
internal fun BoxScope.RecenterFab(
    visible: Boolean,
    bottomPadding: Dp,
    onClick: () -> Unit,
) {
    val animatedPadding by animateDpAsState(bottomPadding)
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { it },
        exit = slideOutVertically { it },
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 16.dp, bottom = animatedPadding + 16.dp),
    ) {
        FloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
        ) {
            Icon(
                painter = painterResource(Res.drawable.my_location),
                contentDescription = stringResource(Res.string.recenter_on_me),
            )
        }
    }
}
