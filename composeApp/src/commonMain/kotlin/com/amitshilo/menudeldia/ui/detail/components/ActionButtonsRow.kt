package com.amitshilo.menudeldia.ui.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.amitshilo.menudeldia.util.rememberUriLauncher
import com.amitshilo.menudeldia.util.walkingDirectionsUri
import menudeldia.composeapp.generated.resources.Res
import menudeldia.composeapp.generated.resources.call_restaurant
import menudeldia.composeapp.generated.resources.get_directions
import menudeldia.composeapp.generated.resources.my_location
import menudeldia.composeapp.generated.resources.phone
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ActionButtonsRow(
    lat: Double,
    lng: Double,
    phone: String?,
    modifier: Modifier = Modifier,
) {
    val uriLauncher = rememberUriLauncher()
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(
            onClick = { uriLauncher.open(walkingDirectionsUri(lat, lng)) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
        ) {
            Icon(
                painter = painterResource(Res.drawable.my_location),
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp),
            )
            Text(stringResource(Res.string.get_directions), fontWeight = FontWeight.SemiBold)
        }
        if (phone != null) {
            OutlinedButton(
                onClick = { uriLauncher.open("tel:$phone") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.phone),
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp),
                )
                Text(stringResource(Res.string.call_restaurant), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
