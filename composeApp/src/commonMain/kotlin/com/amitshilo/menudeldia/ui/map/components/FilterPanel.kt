package com.amitshilo.menudeldia.ui.map.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.amitshilo.menudeldia.domain.model.Restaurant
import com.amitshilo.menudeldia.domain.model.SearchFilterState
import com.amitshilo.menudeldia.ui.preview.previewRestaurants
import com.amitshilo.menudeldia.ui.theme.MenuTheme
import menudeldia.composeapp.generated.resources.Res
import menudeldia.composeapp.generated.resources.filter_clear_all
import menudeldia.composeapp.generated.resources.filter_cuisine_all
import menudeldia.composeapp.generated.resources.filter_distance_1km
import menudeldia.composeapp.generated.resources.filter_distance_2km
import menudeldia.composeapp.generated.resources.filter_distance_500m
import menudeldia.composeapp.generated.resources.filter_distance_5km
import menudeldia.composeapp.generated.resources.filter_distance_any
import menudeldia.composeapp.generated.resources.filter_gluten_free
import menudeldia.composeapp.generated.resources.filter_open_now
import menudeldia.composeapp.generated.resources.filter_price_10_15
import menudeldia.composeapp.generated.resources.filter_price_any
import menudeldia.composeapp.generated.resources.filter_price_over_15
import menudeldia.composeapp.generated.resources.filter_price_under_10
import menudeldia.composeapp.generated.resources.filter_section_availability
import menudeldia.composeapp.generated.resources.filter_section_cuisine
import menudeldia.composeapp.generated.resources.filter_section_diet
import menudeldia.composeapp.generated.resources.filter_section_distance
import menudeldia.composeapp.generated.resources.filter_section_price
import menudeldia.composeapp.generated.resources.filter_vegan
import menudeldia.composeapp.generated.resources.filters
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterPanel(
    filterState: SearchFilterState,
    allRestaurants: List<Restaurant>,
    onFilterChange: (SearchFilterState) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    val cuisineTypes = allRestaurants.mapNotNull { it.cuisineType }.distinct().sorted()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        FilterPanelContent(
            filterState = filterState,
            cuisineTypes = cuisineTypes,
            onFilterChange = onFilterChange,
        )
    }
}

@Composable
private fun FilterPanelContent(
    filterState: SearchFilterState,
    cuisineTypes: List<String>,
    onFilterChange: (SearchFilterState) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(Res.string.filters), style = MaterialTheme.typography.titleLarge)
            if (filterState.isActive) {
                TextButton(onClick = { onFilterChange(SearchFilterState()) }) {
                    Text(stringResource(Res.string.filter_clear_all))
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        FilterSectionLabel(stringResource(Res.string.filter_section_availability))
        ChipRow {
            FilterChip(
                selected = filterState.openNowOnly,
                onClick = { onFilterChange(filterState.copy(openNowOnly = !filterState.openNowOnly)) },
                label = { Text(stringResource(Res.string.filter_open_now)) },
                leadingIcon = { Text("🕒") },
                shape = RoundedCornerShape(24.dp),
            )
        }

        Spacer(Modifier.height(16.dp))

        FilterSectionLabel(stringResource(Res.string.filter_section_diet))
        ChipRow {
            FilterChip(
                selected = filterState.isVegan,
                onClick = { onFilterChange(filterState.copy(isVegan = !filterState.isVegan)) },
                label = { Text(stringResource(Res.string.filter_vegan)) },
                leadingIcon = { Text("🌱") },
                shape = RoundedCornerShape(24.dp),
            )
            FilterChip(
                selected = filterState.isGlutenFree,
                onClick = { onFilterChange(filterState.copy(isGlutenFree = !filterState.isGlutenFree)) },
                label = { Text(stringResource(Res.string.filter_gluten_free)) },
                leadingIcon = { Text("🌾") },
                shape = RoundedCornerShape(24.dp),
            )
        }

        Spacer(Modifier.height(16.dp))

        FilterSectionLabel(stringResource(Res.string.filter_section_price))
        ChipRow {
            PriceOption(
                null,
                null,
                stringResource(Res.string.filter_price_any),
                filterState,
                onFilterChange
            )
            PriceOption(
                null,
                10.0,
                stringResource(Res.string.filter_price_under_10),
                filterState,
                onFilterChange
            )
            PriceOption(
                10.0,
                15.0,
                stringResource(Res.string.filter_price_10_15),
                filterState,
                onFilterChange
            )
            PriceOption(
                15.0,
                null,
                stringResource(Res.string.filter_price_over_15),
                filterState,
                onFilterChange
            )
        }

        if (cuisineTypes.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            FilterSectionLabel(stringResource(Res.string.filter_section_cuisine))
            ChipRow {
                FilterChip(
                    selected = filterState.cuisineType == null,
                    onClick = { onFilterChange(filterState.copy(cuisineType = null)) },
                    label = { Text(stringResource(Res.string.filter_cuisine_all)) },
                    shape = RoundedCornerShape(24.dp),
                )
                cuisineTypes.forEach { type ->
                    FilterChip(
                        selected = filterState.cuisineType == type,
                        onClick = {
                            onFilterChange(
                                filterState.copy(
                                    cuisineType = if (filterState.cuisineType == type) null else type,
                                ),
                            )
                        },
                        label = { Text(type) },
                        shape = RoundedCornerShape(24.dp),
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        FilterSectionLabel(stringResource(Res.string.filter_section_distance))
        ChipRow {
            DistanceOption(
                null,
                stringResource(Res.string.filter_distance_any),
                filterState,
                onFilterChange
            )
            DistanceOption(
                500.0,
                stringResource(Res.string.filter_distance_500m),
                filterState,
                onFilterChange
            )
            DistanceOption(
                1000.0,
                stringResource(Res.string.filter_distance_1km),
                filterState,
                onFilterChange
            )
            DistanceOption(
                2000.0,
                stringResource(Res.string.filter_distance_2km),
                filterState,
                onFilterChange
            )
            DistanceOption(
                5000.0,
                stringResource(Res.string.filter_distance_5km),
                filterState,
                onFilterChange
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun FilterSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

@Composable
private fun ChipRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        content()
    }
}

@Composable
private fun PriceOption(
    min: Double?,
    max: Double?,
    label: String,
    state: SearchFilterState,
    onChange: (SearchFilterState) -> Unit,
) {
    val isSelected = state.minPrice == min && state.maxPrice == max
    FilterChip(
        selected = isSelected,
        onClick = {
            onChange(
                if (isSelected) state.copy(minPrice = null, maxPrice = null)
                else state.copy(minPrice = min, maxPrice = max),
            )
        },
        label = { Text(label) },
        shape = RoundedCornerShape(24.dp),
    )
}

@Composable
private fun DistanceOption(
    maxMeters: Double?,
    label: String,
    state: SearchFilterState,
    onChange: (SearchFilterState) -> Unit,
) {
    val isSelected = state.maxDistanceMeters == maxMeters
    FilterChip(
        selected = isSelected,
        onClick = {
            onChange(
                if (isSelected) state.copy(maxDistanceMeters = null)
                else state.copy(maxDistanceMeters = maxMeters),
            )
        },
        label = { Text(label) },
        shape = RoundedCornerShape(24.dp),
    )
}

// ── Previews ────────────────────────────────────────────────────────────────

@PreviewLightDark
@Composable
private fun PreviewFilterPanelDefault() {
    MenuTheme {
        FilterPanelContent(
            filterState = SearchFilterState(),
            cuisineTypes = previewRestaurants.mapNotNull { it.cuisineType }.distinct().sorted(),
            onFilterChange = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun PreviewFilterPanelActive() {
    MenuTheme {
        FilterPanelContent(
            filterState = SearchFilterState(
                openNowOnly = true,
                minPrice = 10.0,
                maxPrice = 15.0,
                cuisineType = "Catalana",
            ),
            cuisineTypes = previewRestaurants.mapNotNull { it.cuisineType }.distinct().sorted(),
            onFilterChange = {},
        )
    }
}
