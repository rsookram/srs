package io.github.rsookram.srs.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.BackdropScaffold
import androidx.compose.material.BackdropValue
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.rememberBackdropScaffoldState
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
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.TopAppBar
import io.github.rsookram.srs.DeckStats
import io.github.rsookram.srs.DeckWithCount
import io.github.rsookram.srs.GlobalStats
import io.github.rsookram.srs.R
import io.github.rsookram.srs.ui.OverflowMenu
import io.github.rsookram.srs.ui.theme.SrsTheme
import kotlinx.coroutines.delay

typealias DeckName = String

@OptIn(ExperimentalMaterialApi::class) // For BackdropScaffold
@Composable
fun Home(
    snackbarHostState: SnackbarHostState,
    decks: List<DeckWithCount>,
    globalStats: GlobalStats?,
    deckStats: List<DeckStats>,
    onSearchClick: () -> Unit,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    onCreateDeckClick: (DeckName) -> Unit,
    showAddCard: Boolean,
    onAddCardClick: () -> Unit,
    onDeckClick: (DeckWithCount) -> Unit,
    onDeckSaveClick: (deckId: Long, DeckName, IntervalModifier) -> Unit,
    onDeckDeleteClick: (deckId: Long) -> Unit,
) {
    val scaffoldState = rememberBackdropScaffoldState(
        BackdropValue.Concealed,
        snackbarHostState = snackbarHostState,
    )

    var showImportWarningDialog by rememberSaveable { mutableStateOf(false) }

    BackdropScaffold(
        scaffoldState = scaffoldState,
        peekHeight = 256.dp,
        headerHeight = 128.dp,
        appBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                contentPadding = rememberInsetsPaddingValues(
                    insets = LocalWindowInsets.current.systemBars,
                    applyBottom = false,
                ),
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, contentDescription = null)
                    }

                    val expanded = rememberSaveable { mutableStateOf(false) }
                    OverflowMenu(expanded) {
                        DropdownMenuItem(
                            onClick = {
                                expanded.value = false
                                onExportClick()
                            }
                        ) {
                            Text("Export")
                        }
                        DropdownMenuItem(
                            onClick = {
                                expanded.value = false
                                showImportWarningDialog = true
                            }
                        ) {
                            Text("Import")
                        }
                    }
                }
            )
        },
        backLayerContent = {
            Stats(
                contentPadding = rememberInsetsPaddingValues(
                    insets = LocalWindowInsets.current.navigationBars,
                    applyBottom = false,
                ),
                global = globalStats,
                decks = deckStats,
            )
        },
        frontLayerContent = {
            var showCreateDeckDialog by rememberSaveable { mutableStateOf(false) }
            var selectedDeck by remember { mutableStateOf<DeckWithCount?>(null) }

            Box {
                DeckList(
                    decks,
                    onDeckClick,
                    onItemLongClick = { selectedDeck = it },
                    onCreateClick = { showCreateDeckDialog = true },
                )

                if (showAddCard) {
                    FloatingActionButton(
                        onClick = onAddCardClick,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .navigationBarsPadding()
                            .padding(16.dp),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add card")
                    }
                }
            }


            if (showCreateDeckDialog) {
                CreateDeckDialog(
                    onCreateDeckClick,
                    onDismiss = { showCreateDeckDialog = false },
                )
            }

            if (showImportWarningDialog) {
                ImportWarningDialog(
                    onImportClick,
                    onDismiss = { showImportWarningDialog = false },
                )
            }

            selectedDeck?.let { deck ->
                DeckSettingsDialog(
                    deck,
                    onDeleteClick = {
                        onDeckDeleteClick(deck.id)
                        selectedDeck = null
                    },
                    onSaveClick = { name, intervalModifier ->
                        onDeckSaveClick(deck.id, name, intervalModifier)
                        selectedDeck = null
                    },
                    onDismiss = { selectedDeck = null },
                )
            }
        },
    )
}

@Preview
@Composable
private fun HomePreview() = SrsTheme {
    val snackbarHostState = remember { SnackbarHostState() }

    Home(
        snackbarHostState,
        decks = listOf(
            DeckWithCount(id = 1, name = "中文", intervalModifier = 100, scheduledCardCount = 0),
            DeckWithCount(id = 2, name = "日本語", intervalModifier = 100, scheduledCardCount = 12),
        ),
        globalStats = GlobalStats(
            activeCount = 1375,
            suspendedCount = 278,
            leechCount = 0,
            forReviewCount = 37,
        ),
        deckStats = listOf(
            DeckStats(
                name = "prog",
                activeCount = 1177,
                suspendedCount = 266,
                leechCount = 0,
                correctCount = 391,
                wrongCount = 17,
            ),
            DeckStats(
                name = "中文",
                activeCount = 60,
                suspendedCount = 7,
                leechCount = 0,
                correctCount = 40,
                wrongCount = 1,
            ),
            DeckStats(
                name = "日本語",
                activeCount = 138,
                suspendedCount = 5,
                leechCount = 0,
                correctCount = 52,
                wrongCount = 1,
            ),
        ),
        onSearchClick = {},
        onExportClick = {},
        onImportClick = {},
        onCreateDeckClick = {},
        showAddCard = true,
        onAddCardClick = {},
        onDeckClick = {},
        onDeckSaveClick = { _, _, _ -> },
        onDeckDeleteClick = {},
    )
}

@OptIn(ExperimentalFoundationApi::class) // For Modifier.combinedClickable
@Composable
private fun DeckList(
    decks: List<DeckWithCount>,
    onItemClick: (DeckWithCount) -> Unit,
    onItemLongClick: (DeckWithCount) -> Unit,
    onCreateClick: () -> Unit,
) {
    LazyColumn(
        contentPadding = rememberInsetsPaddingValues(
            insets = LocalWindowInsets.current.navigationBars,
        )
    ) {
        items(decks) { deck ->
            DeckItem(
                Modifier.combinedClickable(
                    onClick = { onItemClick(deck) },
                    onLongClick = { onItemLongClick(deck) }
                ),
                deck = deck
            )
        }

        item {
            CreateDeckItem(Modifier.clickable(onClick = onCreateClick))
        }
    }
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

@Composable
private fun ImportWarningDialog(onImportClick: () -> Unit, onDismiss: () -> Unit) {
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
        }
    )
}
