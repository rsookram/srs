package io.github.rsookram.srs.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.github.rsookram.srs.DeckWithCount
import io.github.rsookram.srs.ui.theme.SrsTheme

typealias IntervalModifier = Long

@Composable
fun DeckSettingsDialog(
    deckWithCount: DeckWithCount,
    onDeleteClick: () -> Unit,
    onSaveClick: (DeckName, IntervalModifier) -> Unit,
    onDismiss: () -> Unit,
) {
    var showConfirmDeleteDialog by rememberSaveable { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card {
            var deckName by rememberSaveable { mutableStateOf(deckWithCount.name) }
            var intervalModifier by rememberSaveable {
                mutableStateOf(deckWithCount.intervalModifier)
            }

            Column(Modifier.padding(16.dp)) {
                Text(text = "Deck Settings", style = MaterialTheme.typography.h6)

                OutlinedTextField(
                    value = deckName,
                    onValueChange = { deckName = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    label = { Text("Name") },
                )

                OutlinedTextField(
                    value = intervalModifier.toString(),
                    onValueChange = {
                        val parsed = it.toLongOrNull()
                        if (parsed != null) {
                            intervalModifier = parsed
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    label = { Text("Interval Modifier") },
                )

                Row(Modifier.fillMaxWidth()) {
                    TextButton(onClick = { showConfirmDeleteDialog = true }) {
                        Text(text = "DELETE")
                    }

                    Spacer(Modifier.weight(1f))

                    TextButton(onClick = onDismiss) {
                        Text(text = "CANCEL")
                    }

                    TextButton(
                        onClick = {
                            onSaveClick(deckName, intervalModifier)
                        }
                    ) {
                        Text(text = "SAVE")
                    }
                }
            }
        }
    }

    if (showConfirmDeleteDialog) {
        ConfirmDeleteDeckDialog(
            onConfirm = {
                onDeleteClick()
                onDismiss()
            },
            onDismiss = { showConfirmDeleteDialog = false }
        )
    }
}

@Composable
private fun ConfirmDeleteDeckDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Deck") },
        text = { Text("Are you sure you want to delete this deck?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    )
}

@Preview
@Composable
private fun DeckSettingsDialogPreview() = SrsTheme {
    DeckSettingsDialog(
        deckWithCount = DeckWithCount(
            id = 1,
            name = "日本語",
            intervalModifier = 100,
            scheduledCardCount = 12,
        ),
        onDeleteClick = {},
        onSaveClick = { _, _ -> },
        onDismiss = {},
    )
}
