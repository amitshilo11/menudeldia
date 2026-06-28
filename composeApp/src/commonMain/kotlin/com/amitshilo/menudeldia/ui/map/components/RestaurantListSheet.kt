package com.amitshilo.menudeldia.ui.map.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.amitshilo.menudeldia.ui.designsystem.component.menuShimmer
import com.amitshilo.menudeldia.ui.designsystem.component.rememberMenuShimmer
import com.amitshilo.menudeldia.domain.model.Restaurant
import com.amitshilo.menudeldia.domain.model.SearchFilterState
import com.amitshilo.menudeldia.ui.preview.previewRestaurant
import com.amitshilo.menudeldia.ui.preview.previewRestaurantNoMenu
import com.amitshilo.menudeldia.ui.preview.previewRestaurants
import com.amitshilo.menudeldia.ui.theme.MenuTheme
import menudeldia.composeapp.generated.resources.Res
import menudeldia.composeapp.generated.resources.clear_filters
import menudeldia.composeapp.generated.resources.empty_filtered
import menudeldia.composeapp.generated.resources.empty_filtered_sub
import menudeldia.composeapp.generated.resources.empty_no_menus
import menudeldia.composeapp.generated.resources.empty_no_menus_sub
import menudeldia.composeapp.generated.resources.placeholder_no_result_found
import menudeldia.composeapp.generated.resources.recenter
import menudeldia.composeapp.generated.resources.restaurants_nearby
import menudeldia.composeapp.generated.resources.restaurants_of_total
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RestaurantListSheet(
    restaurants: List<Restaurant>,
    selectedRestaurantId: String?,
    filterState: SearchFilterState,
    totalCount: Int,
    isLoading: Boolean,
    onRestaurantTap: (String) -> Unit,
    onClearFilters: () -> Unit,
    onRecenter: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isLoading && restaurants.isEmpty()) {
        val shimmer = rememberMenuShimmer()
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            modifier = modifier.fillMaxWidth().menuShimmer(shimmer),
        ) {
            items(5) {
                RestaurantCardSkeleton(modifier = Modifier.padding(bottom = 8.dp))
            }
            item { Spacer(Modifier.navigationBarsPadding().height(16.dp)) }
        }
        return
    }

    if (restaurants.isEmpty()) {
        EmptySheetState(
            isFiltered = filterState.isActive,
            onClearFilters = onClearFilters,
            onRecenter = onRecenter,
            modifier = modifier,
        )
        return
    }

    val headerText = if (filterState.isActive) {
        stringResource(Res.string.restaurants_of_total, restaurants.size, totalCount)
    } else {
        stringResource(Res.string.restaurants_nearby, restaurants.size)
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        item {
            Text(
                text = headerText,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        items(restaurants, key = { it.id }) { restaurant ->
            RestaurantCard(
                restaurant = restaurant,
                isSelected = restaurant.id == selectedRestaurantId,
                onClick = { onRestaurantTap(restaurant.id) },
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        item { Spacer(Modifier.navigationBarsPadding().height(16.dp)) }
    }
}

@Composable
private fun EmptySheetState(
    isFiltered: Boolean,
    onClearFilters: () -> Unit,
    onRecenter: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(Res.drawable.placeholder_no_result_found),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(160.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(if (isFiltered) Res.string.empty_filtered else Res.string.empty_no_menus),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(if (isFiltered) Res.string.empty_filtered_sub else Res.string.empty_no_menus_sub),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(20.dp))
        if (isFiltered) {
            Button(onClick = onClearFilters) { Text(stringResource(Res.string.clear_filters)) }
        } else {
            OutlinedButton(onClick = onRecenter) { Text(stringResource(Res.string.recenter)) }
        }
    }
}

// ── Previews ────────────────────────────────────────────────────────────────

@PreviewLightDark
@Composable
private fun PreviewRestaurantListSheet() {
    MenuTheme {
        Surface {
            RestaurantListSheet(
                restaurants = previewRestaurants,
                selectedRestaurantId = previewRestaurant.id,
                filterState = SearchFilterState(),
                totalCount = previewRestaurants.size,
                isLoading = false,
                onRestaurantTap = {},
                onClearFilters = {},
                onRecenter = {},
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewRestaurantListSheetLoading() {
    MenuTheme {
        Surface {
            RestaurantListSheet(
                restaurants = emptyList(),
                selectedRestaurantId = null,
                filterState = SearchFilterState(),
                totalCount = 0,
                isLoading = true,
                onRestaurantTap = {},
                onClearFilters = {},
                onRecenter = {},
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewRestaurantListSheetFiltered() {
    MenuTheme {
        Surface {
            RestaurantListSheet(
                restaurants = listOf(previewRestaurantNoMenu),
                selectedRestaurantId = null,
                filterState = SearchFilterState(openNowOnly = true, isVegan = true),
                totalCount = previewRestaurants.size,
                isLoading = false,
                onRestaurantTap = {},
                onClearFilters = {},
                onRecenter = {},
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewRestaurantListSheetEmpty() {
    MenuTheme {
        Surface {
            RestaurantListSheet(
                restaurants = emptyList(),
                selectedRestaurantId = null,
                filterState = SearchFilterState(),
                totalCount = 0,
                isLoading = false,
                onRestaurantTap = {},
                onClearFilters = {},
                onRecenter = {},
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewRestaurantListSheetEmptyFiltered() {
    MenuTheme {
        Surface {
            RestaurantListSheet(
                restaurants = emptyList(),
                selectedRestaurantId = null,
                filterState = SearchFilterState(openNowOnly = true),
                totalCount = previewRestaurants.size,
                isLoading = false,
                onRestaurantTap = {},
                onClearFilters = {},
                onRecenter = {},
            )
        }
    }
}
