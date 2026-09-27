package com.amitshilo.menudeldia.ui.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.amitshilo.menudeldia.domain.auth.model.AuthState
import com.amitshilo.menudeldia.ui.theme.MenuRadius
import com.amitshilo.menudeldia.ui.theme.MenuSpacing
import menudeldia.composeapp.generated.resources.Res
import menudeldia.composeapp.generated.resources.menu_guest_name
import menudeldia.composeapp.generated.resources.menu_guest_subtitle
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DrawerHeader(authState: AuthState) {
    val user = (authState as? AuthState.Authenticated)?.session?.user

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(
                start = MenuSpacing.xxl,
                end = MenuSpacing.lg,
                top = MenuSpacing.lg,
                bottom = MenuSpacing.xl,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MenuSpacing.md),
    ) {
        Avatar(avatarUrl = user?.avatarUrl, fallbackLetter = user?.displayName?.firstOrNull())
        Column(Modifier.weight(1f)) {
            Text(
                text = user?.displayName
                    ?: user?.email
                    ?: stringResource(Res.string.menu_guest_name),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = user?.email?.takeIf { it != user.displayName }
                    ?: stringResource(Res.string.menu_guest_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun Avatar(avatarUrl: String?, fallbackLetter: Char?) {
    val shape = CircleShape
    if (avatarUrl != null) {
        AsyncImage(
            model = avatarUrl,
            contentDescription = null,
            modifier = Modifier.size(48.dp).clip(shape),
        )
        return
    }
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = (fallbackLetter ?: '·').uppercaseChar().toString(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
internal fun DrawerItem(
    icon: DrawableResource,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    badge: String? = null,
) {
    // NavigationDrawerItem has no disabled state, so a disabled row is drawn by dimming its
    // own colors and swallowing the click rather than by leaving it visually interactive.
    val contentColor = if (enabled) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
    }

    NavigationDrawerItem(
        icon = {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
            )
        },
        label = { Text(label, style = MaterialTheme.typography.bodyLarge) },
        badge = badge?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .clip(RoundedCornerShape(MenuRadius.chip))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(horizontal = MenuSpacing.sm, vertical = MenuSpacing.xs / 2),
                )
            }
        },
        selected = false,
        onClick = { if (enabled) onClick() },
        colors = NavigationDrawerItemDefaults.colors(
            unselectedContainerColor = Color.Transparent,
            unselectedIconColor = contentColor,
            unselectedTextColor = contentColor,
        ),
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
    )
}
