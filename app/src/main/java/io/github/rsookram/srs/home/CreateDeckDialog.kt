package io.github.rsookram.srs.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.github.rsookram.srs.R
import kotlinx.coroutines.delay

@Composable
fun CreateDeckDialog(onCreateClick: (DeckName) -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card {
            var deckName by rememberSaveable { mutableStateOf("") }

            Column(Modifier.padding(16.dp)) {
                Text(
                    stringResource(R.string.create_card_deck_title),
                    style = MaterialTheme.typography.h6,
                )

                val focusRequester = remember { FocusRequester() }

                LaunchedEffect(Unit) {
                    delay(16) // Workaround to make keyboard show
                    focusRequester.requestFocus()
                }

                OutlinedTextField(
                    value = deckName,
                    onValueChange = { deckName = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .focusRequester(focusRequester),
                    label = { Text(stringResource(R.string.card_deck_name)) },
                )

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel_action))
                    }

                    TextButton(
                        onClick = {
                            onCreateClick(deckName)
                            onDismiss()
                        }
                    ) {
                        Text(stringResource(R.string.create_deck_button))
                    }
                }
            }
        }
    }
}
