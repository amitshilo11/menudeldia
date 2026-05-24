package com.amitshilo.menudeldia.ui.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.amitshilo.menudeldia.domain.model.Review

@Composable
fun ReviewCard(review: Review, modifier: Modifier = Modifier) {
    val displayText = review.text?.takeIf { it.isNotBlank() }
        ?: review.originalText?.takeIf { it.isNotBlank() }
        ?: return

    Row(modifier = modifier.fillMaxWidth()) {
        ReviewAvatar(
            photoUri = review.authorPhotoUri,
            authorName = review.authorName,
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = review.authorName ?: "Anonymous",
                    style = MaterialTheme.typography.labelLarge,
                )
                review.rating?.let { rating ->
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "★".repeat(rating.coerceIn(0, 5)),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                    )
                }
            }
            review.relativeTime?.let { time ->
                Text(
                    text = time,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(4.dp))
            ExpandableText(text = displayText, collapsedMaxLines = 4)
        }
    }
}

@Composable
private fun ReviewAvatar(photoUri: String?, authorName: String?) {
    if (photoUri != null) {
        AsyncImage(
            model = photoUri,
            contentDescription = null,
            modifier = Modifier.size(36.dp).clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
    } else {
        val initial = authorName?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initial,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}
