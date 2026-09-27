package com.amitshilo.menudeldia.ui.drawer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.amitshilo.menudeldia.APP_VERSION_CODE
import com.amitshilo.menudeldia.APP_VERSION_NAME
import com.amitshilo.menudeldia.domain.auth.model.AuthSession
import com.amitshilo.menudeldia.domain.auth.model.AuthState
import com.amitshilo.menudeldia.domain.auth.model.AuthUser
import com.amitshilo.menudeldia.ui.theme.MenuSpacing
import com.amitshilo.menudeldia.ui.theme.MenuTheme
import menudeldia.composeapp.generated.resources.Res
import menudeldia.composeapp.generated.resources.ic_add_business
import menudeldia.composeapp.generated.resources.ic_description
import menudeldia.composeapp.generated.resources.ic_favorite
import menudeldia.composeapp.generated.resources.ic_help
import menudeldia.composeapp.generated.resources.ic_logout
import menudeldia.composeapp.generated.resources.ic_mail
import menudeldia.composeapp.generated.resources.ic_person
import menudeldia.composeapp.generated.resources.ic_shield
import menudeldia.composeapp.generated.resources.ic_star
import menudeldia.composeapp.generated.resources.info
import menudeldia.composeapp.generated.resources.menu_add_restaurant
import menudeldia.composeapp.generated.resources.menu_coming_soon
import menudeldia.composeapp.generated.resources.menu_favorites
import menudeldia.composeapp.generated.resources.menu_help_support
import menudeldia.composeapp.generated.resources.menu_how_it_works
import menudeldia.composeapp.generated.resources.menu_log_out
import menudeldia.composeapp.generated.resources.menu_privacy_policy
import menudeldia.composeapp.generated.resources.menu_profile
import menudeldia.composeapp.generated.resources.menu_send_feedback
import menudeldia.composeapp.generated.resources.menu_sign_in
import menudeldia.composeapp.generated.resources.menu_terms
import menudeldia.composeapp.generated.resources.menu_todays_picks
import menudeldia.composeapp.generated.resources.menu_version
import org.jetbrains.compose.resources.stringResource


/**
 * Contents of the left-hand navigation drawer. Purely presentational — every row reports up
 * so the caller can close the drawer before navigating, which keeps the slide-out animation
 * from racing the screen transition.
 */
@Composable
fun AppDrawerContent(
    authState: AuthState,
    onProfile: () -> Unit,
    onTodaysPicks: () -> Unit,
    onHowItWorks: () -> Unit,
    onHelpSupport: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    onTerms: () -> Unit,
    onSendFeedback: () -> Unit,
    onLogOut: () -> Unit,
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isAuthenticated = authState is AuthState.Authenticated

    ModalDrawerSheet(
        modifier = modifier,
        drawerContainerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
        ) {
            DrawerHeader(authState)

            HorizontalDivider(Modifier.padding(horizontal = MenuSpacing.lg))
            Spacer(Modifier.height(MenuSpacing.sm))

            DrawerItem(
                icon = Res.drawable.ic_person,
                label = stringResource(Res.string.menu_profile),
                onClick = onProfile,
            )
            DrawerItem(
                icon = Res.drawable.ic_star,
                label = stringResource(Res.string.menu_todays_picks),
                onClick = onTodaysPicks,
            )
            DrawerItem(
                icon = Res.drawable.ic_favorite,
                label = stringResource(Res.string.menu_favorites),
                onClick = {},
                enabled = false,
                badge = stringResource(Res.string.menu_coming_soon),
            )
            DrawerItem(
                icon = Res.drawable.ic_add_business,
                label = stringResource(Res.string.menu_add_restaurant),
                onClick = {},
                enabled = false,
                badge = stringResource(Res.string.menu_coming_soon),
            )

            Spacer(Modifier.height(MenuSpacing.sm))
            HorizontalDivider(Modifier.padding(horizontal = MenuSpacing.lg))
            Spacer(Modifier.height(MenuSpacing.sm))

            DrawerItem(
                icon = Res.drawable.info,
                label = stringResource(Res.string.menu_how_it_works),
                onClick = onHowItWorks,
            )
            DrawerItem(
                icon = Res.drawable.ic_help,
                label = stringResource(Res.string.menu_help_support),
                onClick = onHelpSupport,
            )
            DrawerItem(
                icon = Res.drawable.ic_shield,
                label = stringResource(Res.string.menu_privacy_policy),
                onClick = onPrivacyPolicy,
            )
            DrawerItem(
                icon = Res.drawable.ic_description,
                label = stringResource(Res.string.menu_terms),
                onClick = onTerms,
            )
            DrawerItem(
                icon = Res.drawable.ic_mail,
                label = stringResource(Res.string.menu_send_feedback),
                onClick = onSendFeedback,
            )

            Spacer(Modifier.height(MenuSpacing.sm))
            HorizontalDivider(Modifier.padding(horizontal = MenuSpacing.lg))
            Spacer(Modifier.height(MenuSpacing.sm))

            DrawerItem(
                icon = Res.drawable.ic_logout,
                label = stringResource(
                    if (isAuthenticated) Res.string.menu_log_out else Res.string.menu_sign_in
                ),
                onClick = if (isAuthenticated) onLogOut else onSignIn,
            )

            Spacer(Modifier.height(MenuSpacing.lg))
            Text(
                text = stringResource(Res.string.menu_version, APP_VERSION_NAME, APP_VERSION_CODE),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    start = MenuSpacing.xxl,
                    bottom = MenuSpacing.lg,
                ),
            )
        }
    }
}

// ── Previews ────────────────────────────────────────────────────────────────

@PreviewLightDark
@Composable
private fun PreviewAppDrawerAuthenticated() {
    MenuTheme {
        AppDrawerContent(
            authState = AuthState.Authenticated(
                AuthSession(
                    accessToken = "token",
                    user = AuthUser(
                        id = "1",
                        email = "amit@example.com",
                        displayName = "Amit Shilo",
                        avatarUrl = null,
                    ),
                ),
            ),
            onProfile = {},
            onTodaysPicks = {},
            onHowItWorks = {},
            onHelpSupport = {},
            onPrivacyPolicy = {},
            onTerms = {},
            onSendFeedback = {},
            onLogOut = {},
            onSignIn = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun PreviewAppDrawerGuest() {
    MenuTheme {
        AppDrawerContent(
            authState = AuthState.Guest,
            onProfile = {},
            onTodaysPicks = {},
            onHowItWorks = {},
            onHelpSupport = {},
            onPrivacyPolicy = {},
            onTerms = {},
            onSendFeedback = {},
            onLogOut = {},
            onSignIn = {},
        )
    }
}
