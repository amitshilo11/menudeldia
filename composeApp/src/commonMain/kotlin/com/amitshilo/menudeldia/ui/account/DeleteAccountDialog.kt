package com.amitshilo.menudeldia.ui.account

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.amitshilo.menudeldia.ui.theme.MenuTheme
import menudeldia.composeapp.generated.resources.Res
import menudeldia.composeapp.generated.resources.account_delete_cancel
import menudeldia.composeapp.generated.resources.account_delete_confirm
import menudeldia.composeapp.generated.resources.account_delete_dialog_body
import menudeldia.composeapp.generated.resources.account_delete_dialog_title
import org.jetbrains.compose.resources.stringResource

/**
 * Irreversible-action confirmation required before hitting `DELETE /api/v1/me`. Spells out
 * exactly what goes, which is what App Review looks for alongside the deletion itself.
 */
@Composable
fun DeleteAccountDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.account_delete_dialog_title)) },
        text = { Text(stringResource(Res.string.account_delete_dialog_body)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(Res.string.account_delete_confirm),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.account_delete_cancel))
            }
        },
    )
}

@PreviewLightDark
@Composable
private fun PreviewDeleteAccountDialog() {
    MenuTheme {
        DeleteAccountDialog(onConfirm = {}, onDismiss = {})
    }
}
