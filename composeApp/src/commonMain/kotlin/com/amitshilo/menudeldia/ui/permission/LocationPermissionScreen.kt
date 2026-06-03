package com.amitshilo.menudeldia.ui.permission

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.amitshilo.menudeldia.location.rememberLocationState
import com.amitshilo.menudeldia.ui.theme.MenuRadius
import com.amitshilo.menudeldia.ui.theme.MenuSpacing
import com.amitshilo.menudeldia.ui.theme.MenuTheme
import menudeldia.composeapp.generated.resources.Res
import menudeldia.composeapp.generated.resources.location_permission_approve
import menudeldia.composeapp.generated.resources.location_permission_body
import menudeldia.composeapp.generated.resources.location_permission_deny
import menudeldia.composeapp.generated.resources.location_permission_title
import menudeldia.composeapp.generated.resources.my_location
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun LocationPermissionScreen(onDone: () -> Unit) {
    val locationState = rememberLocationState()

    LaunchedEffect(locationState.hasPermission) {
        if (locationState.hasPermission) onDone()
    }

    if (!locationState.hasPermission) {
        LocationPermissionContent(
            onApprove = locationState.requestPermission,
            onDeny = onDone,
        )
    }
}

@Composable
private fun LocationPermissionContent(
    onApprove: () -> Unit,
    onDeny: () -> Unit,
) {
    Scaffold { padding: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = MenuSpacing.xxxl),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.my_location),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(48.dp),
                )
            }

            Spacer(Modifier.height(MenuSpacing.xxxl))

            Text(
                text = stringResource(Res.string.location_permission_title),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(MenuSpacing.lg))

            Text(
                text = stringResource(Res.string.location_permission_body),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(MenuSpacing.huge))

            Button(
                onClick = onApprove,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(MenuRadius.button),
            ) {
                Text(stringResource(Res.string.location_permission_approve))
            }

            TextButton(onClick = onDeny) {
                Text(stringResource(Res.string.location_permission_deny))
            }
        }
    }
}

// ── Previews ────────────────────────────────────────────────────────────────

@PreviewLightDark
@Composable
private fun PreviewLocationPermission() {
    MenuTheme {
        LocationPermissionContent(onApprove = {}, onDeny = {})
    }
}
