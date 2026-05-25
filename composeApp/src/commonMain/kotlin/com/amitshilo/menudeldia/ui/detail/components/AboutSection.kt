package com.amitshilo.menudeldia.ui.detail.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import com.amitshilo.menudeldia.domain.model.Restaurant
import menudeldia.composeapp.generated.resources.Res
import menudeldia.composeapp.generated.resources.about_header
import org.jetbrains.compose.resources.stringResource

@Composable
fun AboutSection(restaurant: Restaurant, modifier: Modifier = Modifier) {
    val lang = Locale.current.language
    val localizedDesc = when (lang) {
        "es", "ca" -> restaurant.descriptionEs ?: restaurant.descriptionEn
        else -> restaurant.descriptionEn ?: restaurant.descriptionEs
    }
    val about = restaurant.aiSummary?.takeIf { it.isNotBlank() }
        ?: localizedDesc?.takeIf { it.isNotBlank() }
        ?: restaurant.editorialSummary?.takeIf { it.isNotBlank() }
        ?: return

    Spacer(Modifier.height(24.dp))
    SectionHeader(stringResource(Res.string.about_header))
    Spacer(Modifier.height(6.dp))
    ExpandableText(text = about)
}
