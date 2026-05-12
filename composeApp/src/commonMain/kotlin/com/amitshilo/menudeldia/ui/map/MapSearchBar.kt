package com.amitshilo.menudeldia.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Badge
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.amitshilo.menudeldia.domain.model.SearchFilterState
import com.amitshilo.menudeldia.ui.theme.MenuTheme
import menudeldia.composeapp.generated.resources.Res
import menudeldia.composeapp.generated.resources.clear_search
import menudeldia.composeapp.generated.resources.close
import menudeldia.composeapp.generated.resources.filter_gluten_free
import menudeldia.composeapp.generated.resources.filter_list
import menudeldia.composeapp.generated.resources.filter_open_now
import menudeldia.composeapp.generated.resources.filter_vegan
import menudeldia.composeapp.generated.resources.filters
import menudeldia.composeapp.generated.resources.search
import menudeldia.composeapp.generated.resources.search_placeholder
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun MapSearchBar(
    filterState: SearchFilterState,
    onFilterChange: (SearchFilterState) -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
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
                value = filterState.query,
                onValueChange = { onFilterChange(filterState.copy(query = it)) },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                decorationBox = { innerTextField ->
                    if (filterState.query.isEmpty()) {
                        Text(
                            text = stringResource(Res.string.search_placeholder),
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

            if (filterState.query.isNotEmpty()) {
                IconButton(onClick = { onFilterChange(filterState.copy(query = "")) }) {
                    Icon(
                        painter = painterResource(Res.drawable.close),
                        contentDescription = stringResource(Res.string.clear_search),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            QuickFilterChip(
                selected = filterState.openNowOnly,
                onClick = { onFilterChange(filterState.copy(openNowOnly = !filterState.openNowOnly)) },
                label = stringResource(Res.string.filter_open_now),
                leadingEmoji = "🕒",
            )
            QuickFilterChip(
                selected = filterState.isVegan,
                onClick = { onFilterChange(filterState.copy(isVegan = !filterState.isVegan)) },
                label = stringResource(Res.string.filter_vegan),
                leadingEmoji = "🌱",
            )
            QuickFilterChip(
                selected = filterState.isGlutenFree,
                onClick = { onFilterChange(filterState.copy(isGlutenFree = !filterState.isGlutenFree)) },
                label = stringResource(Res.string.filter_gluten_free),
                leadingEmoji = "🌾",
            )

            val activeFilterCount = filterState.activeCount.let {
                if (filterState.query.isNotBlank()) it - 1 else it
            }

            FilterChip(
                selected = activeFilterCount > 0,
                onClick = onFilterClick,
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(Res.string.filters),
                            style = MaterialTheme.typography.labelMedium
                        )
                        if (activeFilterCount > 0) {
                            Spacer(Modifier.width(4.dp))
                            Badge { Text(activeFilterCount.toString()) }
                        }
                    }
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(Res.drawable.filter_list),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                shape = RoundedCornerShape(24.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = activeFilterCount > 0,
                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                    selectedBorderColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }
    }
}

@Composable
private fun QuickFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    leadingEmoji: String? = null,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        leadingIcon = leadingEmoji?.let { { Text(it) } },
        shape = RoundedCornerShape(24.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outlineVariant,
            selectedBorderColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

// ── Previews ────────────────────────────────────────────────────────────────

@PreviewLightDark
@Composable
private fun PreviewMapSearchBarEmpty() {
    MenuTheme {
        MapSearchBar(
            filterState = SearchFilterState(),
            onFilterChange = {},
            onFilterClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@PreviewLightDark
@Composable
private fun PreviewMapSearchBarQuery() {
    MenuTheme {
        MapSearchBar(
            filterState = SearchFilterState(query = "Paella"),
            onFilterChange = {},
            onFilterClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@PreviewLightDark
@Composable
private fun PreviewMapSearchBarFiltered() {
    MenuTheme {
        MapSearchBar(
            filterState = SearchFilterState(openNowOnly = true, isVegan = true),
            onFilterChange = {},
            onFilterClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
