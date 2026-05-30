package com.amitshilo.menudeldia.ui.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.amitshilo.menudeldia.domain.model.Dish
import com.amitshilo.menudeldia.domain.model.Menu
import com.amitshilo.menudeldia.domain.model.Restaurant
import com.amitshilo.menudeldia.util.format
import menudeldia.composeapp.generated.resources.Res
import menudeldia.composeapp.generated.resources.daily_menu_with_price
import menudeldia.composeapp.generated.resources.dish_desserts
import menudeldia.composeapp.generated.resources.dish_firsts
import menudeldia.composeapp.generated.resources.dish_seconds
import menudeldia.composeapp.generated.resources.includes_dessert
import menudeldia.composeapp.generated.resources.includes_drink
import menudeldia.composeapp.generated.resources.no_menu_today_long
import org.jetbrains.compose.resources.stringResource

@Composable
fun MenuCard(restaurant: Restaurant, menu: Menu?, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (menu != null) {
            Text(
                text = stringResource(Res.string.daily_menu_with_price, menu.price.format(2)),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
            IncludesRow(restaurant = restaurant)
            if (menu.firsts.isNotEmpty()) DishSection(
                stringResource(Res.string.dish_firsts),
                menu.firsts
            )
            if (menu.seconds.isNotEmpty()) DishSection(
                stringResource(Res.string.dish_seconds),
                menu.seconds
            )
            if (menu.desserts.isNotEmpty()) DishSection(
                stringResource(Res.string.dish_desserts),
                menu.desserts
            )
            menu.notes?.let { notes ->
                Spacer(Modifier.height(12.dp))
                Text(
                    notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            Text(
                text = stringResource(Res.string.no_menu_today_long),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun IncludesRow(restaurant: Restaurant) {
    val dessertLabel = stringResource(Res.string.includes_dessert)
    val drinkLabel = stringResource(Res.string.includes_drink)
    val includes: List<Pair<String, String>> = when {
        restaurant.menuIncludes.isNotEmpty() -> restaurant.menuIncludes.map { "✓" to it }
        restaurant.includesDessert || restaurant.includesDrink -> buildList {
            if (restaurant.includesDessert) add("🍮" to dessertLabel)
            if (restaurant.includesDrink) add("🥤" to drinkLabel)
        }

        else -> emptyList()
    }
    if (includes.isEmpty()) return
    Spacer(Modifier.height(6.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        includes.forEach { (icon, label) ->
            FeatureChip(text = label, icon = icon)
        }
    }
}

@Composable
fun DishSection(title: String, dishes: List<Dish>) {
    Spacer(Modifier.height(12.dp))
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.secondary,
    )
    Spacer(Modifier.height(4.dp))
    Column {
        dishes.forEach { dish ->
            Text(
                text = dish.name,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 2.dp),
            )
        }
    }
}
