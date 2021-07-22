package io.github.rsookram.srs.home

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable

@Composable
fun ImportWarningDialog(onImportClick: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import data") },
        text = {
            Text(
                "Importing data will overwrite the stored data and will require an app restart. " +
                    "After the import file is selected, " +
                    "the app will close and you will need to start it again."
            )
        },
        confirmButton = {
            TextButton(onClick = onImportClick) {
                Text("Continue")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
