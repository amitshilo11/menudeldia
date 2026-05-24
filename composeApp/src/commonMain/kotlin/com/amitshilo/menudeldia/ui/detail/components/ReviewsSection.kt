package com.amitshilo.menudeldia.ui.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.amitshilo.menudeldia.domain.model.Review
import menudeldia.composeapp.generated.resources.Res
import menudeldia.composeapp.generated.resources.reviews_header
import menudeldia.composeapp.generated.resources.show_all_reviews
import org.jetbrains.compose.resources.stringResource

private const val INITIAL_COUNT = 3

@Composable
fun ReviewsSection(reviews: List<Review>, modifier: Modifier = Modifier) {
    if (reviews.isEmpty()) return
    var showAll by remember { mutableStateOf(false) }
    val visible = if (showAll) reviews else reviews.take(INITIAL_COUNT)

    HorizontalDivider()
    Spacer(Modifier.height(12.dp))
    Text(
        text = "${stringResource(Res.string.reviews_header)} (${reviews.size})",
        style = MaterialTheme.typography.titleMedium,
    )
    Spacer(Modifier.height(12.dp))
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        visible.forEach { review ->
            ReviewCard(review = review)
        }
    }
    if (!showAll && reviews.size > INITIAL_COUNT) {
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = { showAll = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                text = stringResource(Res.string.show_all_reviews),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(vertical = 2.dp),
            )
        }
    }
}
