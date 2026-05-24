package com.amitshilo.menudeldia.ui.detail.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import menudeldia.composeapp.generated.resources.Res
import menudeldia.composeapp.generated.resources.read_less
import menudeldia.composeapp.generated.resources.read_more
import org.jetbrains.compose.resources.stringResource

@Composable
fun ExpandableText(
    text: String,
    collapsedMaxLines: Int = 4,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    var isOverflowing by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = if (expanded) Int.MAX_VALUE else collapsedMaxLines,
            overflow = if (expanded) TextOverflow.Clip else TextOverflow.Ellipsis,
            onTextLayout = { result ->
                if (!expanded) isOverflowing = result.hasVisualOverflow
            },
        )
        if (isOverflowing || expanded) {
            Text(
                text = if (expanded) stringResource(Res.string.read_less)
                else stringResource(Res.string.read_more),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { expanded = !expanded },
            )
        }
    }
}
