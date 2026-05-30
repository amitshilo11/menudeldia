package com.amitshilo.menudeldia.ui.detail.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.amitshilo.menudeldia.util.rememberUriLauncher
import menudeldia.composeapp.generated.resources.Res
import menudeldia.composeapp.generated.resources.phone_header
import org.jetbrains.compose.resources.stringResource

@Composable
fun ContactSection(phone: String, modifier: Modifier = Modifier) {
    val uriLauncher = rememberUriLauncher()
    SectionHeader(stringResource(Res.string.phone_header))
    Spacer(Modifier.height(4.dp))
    Text(
        text = phone,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary,
        textDecoration = TextDecoration.Underline,
        modifier = Modifier.clickable { uriLauncher.open("tel:$phone") },
    )
}
