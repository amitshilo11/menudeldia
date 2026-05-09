package com.amitshilo.menudeldia.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import menudeldia.composeapp.generated.resources.Res
import menudeldia.composeapp.generated.resources.close
import menudeldia.composeapp.generated.resources.filter_list
import menudeldia.composeapp.generated.resources.search
import org.jetbrains.compose.resources.painterResource

@Composable
fun MapSearchBar(
    query: String,
    activeFilterCount: Int,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(28.dp))
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(28.dp),
            )
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(Res.drawable.search),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 12.dp, end = 4.dp),
        )

        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                    Text(
                        text = "Buscar restaurantes...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                innerTextField()
            },
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .padding(vertical = 12.dp),
        )

        if (query.isNotEmpty()) {
            IconButton(onClick = { onQueryChange("") }) {
                Icon(
                    painter = painterResource(Res.drawable.close),
                    contentDescription = "Clear search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        BadgedBox(
            badge = {
                if (activeFilterCount > 0) {
                    Badge { Text("$activeFilterCount") }
                }
            },
        ) {
            IconButton(onClick = onFilterClick) {
                Icon(
                    painter = painterResource(Res.drawable.filter_list),
                    contentDescription = "Filters",
                    tint = if (activeFilterCount > 0) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
