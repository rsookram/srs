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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.github.rsookram.srs.DeckWithCount
import io.github.rsookram.srs.R
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
                Text(
                    stringResource(R.string.card_deck_settings_title),
                    style = MaterialTheme.typography.h6,
                )

                OutlinedTextField(
                    value = deckName,
                    onValueChange = { deckName = it },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    label = { Text(stringResource(R.string.card_deck_name)) },
                )

                OutlinedTextField(
                    value = intervalModifier.toString(),
                    onValueChange = {
                        val parsed = it.toLongOrNull()
                        if (parsed != null) {
                            intervalModifier = parsed
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    label = { Text(stringResource(R.string.review_interval_modifier)) },
                )

                Row(Modifier.fillMaxWidth()) {
                    TextButton(onClick = { showConfirmDeleteDialog = true }) {
                        Text(stringResource(R.string.delete_content_button))
                    }

                    Spacer(Modifier.weight(1f))

                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel_action)) }

                    TextButton(onClick = { onSaveClick(deckName, intervalModifier) }) {
                        Text(stringResource(R.string.save_changes_button))
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
        title = { Text(stringResource(R.string.delete_card_deck_title)) },
        text = { Text(stringResource(R.string.delete_card_deck_confirmation)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.confirm_changes_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.dismiss_dialog_button)) }
        }
    )
}

@Preview
@Composable
private fun DeckSettingsDialogPreview() = SrsTheme {
    DeckSettingsDialog(
        deckWithCount =
            DeckWithCount(
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
