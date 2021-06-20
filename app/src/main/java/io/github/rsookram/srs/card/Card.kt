package io.github.rsookram.srs.card

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.ContentAlpha
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.rsookram.srs.Card
import io.github.rsookram.srs.Deck
import io.github.rsookram.srs.ui.theme.SrsTheme

/**
 * Screen which allows the user to enter the content of a new card, or to edit the content of an
 * existing one.
 *
 * @param card The card to edit. `null` if creating a new card.
 * @param selectedDeck The deck that [card] is part of. `null` if creating a new card.
 */
@Composable
fun Card(decks: List<Deck>, selectedDeck: Deck?, card: Card?) {
    Scaffold(
        topBar = {
            TopAppBar {
                Row(Modifier.width(68.dp), verticalAlignment = Alignment.CenterVertically) {
                    CompositionLocalProvider(
                        LocalContentAlpha provides ContentAlpha.high,
                        content = {
                            IconButton(onClick = { /*TODO*/ }) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = null)
                            }
                        }
                    )
                }

                var expanded by remember { mutableStateOf(false) }

                Box(Modifier.fillMaxSize()) {
                    Box(
                        Modifier
                            .clickable { expanded = true }
                            .fillMaxHeight()
                            .widthIn(min = 128.dp),
                        Alignment.CenterStart,
                    ) {
                        Text(
                            text = selectedDeck?.name ?: decks.first().name,
                            Modifier.padding(horizontal = 16.dp),
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        decks.forEach { deck ->
                            DropdownMenuItem(onClick = { /*TODO*/ }) {
                                Text(deck.name)
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /*TODO*/ }) {
                Icon(Icons.Default.Check, contentDescription = "Confirm changes")
            }
        }
    ) {
        Column(Modifier.padding(16.dp)) {
            var front by rememberSaveable { mutableStateOf(card?.front.orEmpty()) }

            OutlinedTextField(
                value = front,
                onValueChange = { front = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Front") },
            )

            Spacer(Modifier.height(16.dp))

            var back by rememberSaveable { mutableStateOf(card?.back.orEmpty()) }

            OutlinedTextField(
                value = back,
                onValueChange = { back = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Back") },
            )
        }
    }
}

@Preview
@Composable
fun CardPreview() = SrsTheme {
    Card(
        decks = listOf(
            Deck(id = 1, name = "中文", creationTimestamp = "", intervalModifier = 100),
            Deck(id = 2, name = "日本語", creationTimestamp = "", intervalModifier = 100),
        ),
        selectedDeck = null,
        card = null,
    )
}
