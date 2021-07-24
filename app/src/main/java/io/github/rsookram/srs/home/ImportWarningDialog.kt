package io.github.rsookram.srs.home

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.github.rsookram.srs.R

@Composable
fun ImportWarningDialog(onImportClick: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.import_data_title)) },
        text = {
            Text(stringResource(R.string.import_warning))
        },
        confirmButton = {
            TextButton(onClick = onImportClick) {
                Text(stringResource(R.string.proceed_with_import))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_action))
            }
        },
    )
}
