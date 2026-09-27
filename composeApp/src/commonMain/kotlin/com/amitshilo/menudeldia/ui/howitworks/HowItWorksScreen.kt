package com.amitshilo.menudeldia.ui.howitworks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.amitshilo.menudeldia.ui.theme.MenuSpacing
import com.amitshilo.menudeldia.ui.theme.MenuTheme
import menudeldia.composeapp.generated.resources.Res
import menudeldia.composeapp.generated.resources.arrow_back
import menudeldia.composeapp.generated.resources.back
import menudeldia.composeapp.generated.resources.filter_list
import menudeldia.composeapp.generated.resources.how_it_works_filters_body
import menudeldia.composeapp.generated.resources.how_it_works_filters_title
import menudeldia.composeapp.generated.resources.how_it_works_footer
import menudeldia.composeapp.generated.resources.how_it_works_included_body
import menudeldia.composeapp.generated.resources.how_it_works_included_title
import menudeldia.composeapp.generated.resources.how_it_works_intro
import menudeldia.composeapp.generated.resources.how_it_works_map_body
import menudeldia.composeapp.generated.resources.how_it_works_map_title
import menudeldia.composeapp.generated.resources.how_it_works_picks_body
import menudeldia.composeapp.generated.resources.how_it_works_picks_title
import menudeldia.composeapp.generated.resources.how_it_works_title
import menudeldia.composeapp.generated.resources.ic_money_bag
import menudeldia.composeapp.generated.resources.ic_star
import menudeldia.composeapp.generated.resources.my_location
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Plain-language explainer of what a menú del día is and how the app surfaces one. Written for
 * visitors who've never heard the term — the concept is the whole premise of the app, so it
 * can't only live in the App Store description.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HowItWorksScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.how_it_works_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(Res.drawable.arrow_back),
                            contentDescription = stringResource(Res.string.back),
                        )
                    }
                },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = MenuSpacing.xxl),
        ) {
            Text(
                text = stringResource(Res.string.how_it_works_intro),
                style = MaterialTheme.typography.bodyLarge,
            )

            Spacer(Modifier.height(MenuSpacing.xxxl))

            Step(
                icon = Res.drawable.ic_money_bag,
                title = stringResource(Res.string.how_it_works_included_title),
                body = stringResource(Res.string.how_it_works_included_body),
            )
            Step(
                icon = Res.drawable.my_location,
                title = stringResource(Res.string.how_it_works_map_title),
                body = stringResource(Res.string.how_it_works_map_body),
            )
            Step(
                icon = Res.drawable.ic_star,
                title = stringResource(Res.string.how_it_works_picks_title),
                body = stringResource(Res.string.how_it_works_picks_body),
            )
            Step(
                icon = Res.drawable.filter_list,
                title = stringResource(Res.string.how_it_works_filters_title),
                body = stringResource(Res.string.how_it_works_filters_body),
            )

            Spacer(Modifier.height(MenuSpacing.lg))
            Text(
                text = stringResource(Res.string.how_it_works_footer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(MenuSpacing.xxxl))
        }
    }
}

@Composable
private fun Step(icon: DrawableResource, title: String, body: String) {
    Row(Modifier.padding(bottom = MenuSpacing.xxl)) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.size(MenuSpacing.lg))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(MenuSpacing.xs))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ── Previews ────────────────────────────────────────────────────────────────

@PreviewLightDark
@Composable
private fun PreviewHowItWorks() {
    MenuTheme {
        HowItWorksScreen(onBack = {})
    }
}
