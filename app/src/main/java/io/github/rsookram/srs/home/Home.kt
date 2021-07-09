package io.github.rsookram.srs.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.github.rsookram.srs.DeckWithCount
import io.github.rsookram.srs.R
import io.github.rsookram.srs.ui.BottomBar
import io.github.rsookram.srs.ui.TopLevelScreen
import io.github.rsookram.srs.ui.theme.SrsTheme
import kotlinx.coroutines.delay

typealias DeckName = String

@OptIn(ExperimentalFoundationApi::class) // For Modifier.combinedClickable
@Composable
fun Home(
    decks: List<DeckWithCount>,
    onCreateDeckClick: (DeckName) -> Unit,
    onNavItemClick: (TopLevelScreen) -> Unit,
    showAddCard: Boolean,
    onAddCardClick: () -> Unit,
    onDeckClick: (DeckWithCount) -> Unit,
    onDeckSaveClick: (deckId: Long, DeckName, IntervalModifier) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.app_name)) })
        },
        bottomBar = {
            BottomBar(selected = TopLevelScreen.HOME, onItemClick = onNavItemClick)
        },
        floatingActionButton = {
            if (showAddCard) {
                FloatingActionButton(onClick = onAddCardClick) {
                    Icon(Icons.Default.Add, contentDescription = "Add card")
                }
            }
        }
    ) {
        var showCreateDeckDialog by rememberSaveable { mutableStateOf(false) }
        var selectedDeck by remember { mutableStateOf<DeckWithCount?>(null) }

        LazyColumn {
            items(decks) { deck ->
                DeckItem(
                    Modifier.combinedClickable(
                        onClick = { onDeckClick(deck) },
                        onLongClick = { selectedDeck = deck }
                    ),
                    deck = deck
                )
            }

            item {
                CreateDeckItem(Modifier.clickable { showCreateDeckDialog = true })
            }
        }

        if (showCreateDeckDialog) {
            CreateDeckDialog(
                onCreateDeckClick,
                onDismiss = { showCreateDeckDialog = false },
            )
        }

        selectedDeck?.let { deck ->
            DeckSettingsDialog(
                deck,
                onSaveClick = { name, intervalModifier ->
                    onDeckSaveClick(deck.id, name, intervalModifier)
                },
                onDismiss = { selectedDeck = null },
            )
        }
    }
}

@Preview
@Composable
private fun HomePreview() = SrsTheme {
    Home(
        decks = listOf(
            DeckWithCount(id = 1, name = "中文", intervalModifier = 100, scheduledCardCount = 0),
            DeckWithCount(id = 2, name = "日本語", intervalModifier = 100, scheduledCardCount = 12),
        ),
        onCreateDeckClick = {},
        onNavItemClick = {},
        showAddCard = true,
        onAddCardClick = {},
        onDeckClick = {},
        onDeckSaveClick = { _, _, _ -> },
    )
}

@Composable
fun DeckItem(modifier: Modifier = Modifier, deck: DeckWithCount) {
    Row(modifier.heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            deck.name,
            Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        )

        Text(deck.scheduledCardCount.toString(), Modifier.padding(end = 16.dp))
    }
}

@Preview
@Composable
private fun DeckItemPreview() = SrsTheme {
    DeckItem(
        deck = DeckWithCount(
            id = 1,
            name = "日本語",
            intervalModifier = 100,
            scheduledCardCount = 12,
        )
    )
}

@Composable
fun CreateDeckItem(modifier: Modifier = Modifier) {
    Row(modifier.heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.padding(start = 16.dp),
        )

        Text(
            "Create deck",
            Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
        )
    }
}

@Preview
@Composable
private fun CreateDeckItemPreview() = SrsTheme {
    CreateDeckItem()
}

@Composable
private fun CreateDeckDialog(
    onCreateClick: (DeckName) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Card {
            var deckName by rememberSaveable { mutableStateOf("") }

            Column(Modifier.padding(16.dp)) {
                Text(text = "Create Deck", style = MaterialTheme.typography.h6)

                val focusRequester = FocusRequester()

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
                    label = { Text("Name") },
                )

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = "CANCEL")
                    }

                    TextButton(
                        onClick = {
                            onCreateClick(deckName)
                            onDismiss()
                        }
                    ) {
                        Text(text = "CREATE")
                    }
                }
            }
        }
    }
}
