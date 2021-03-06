package io.github.rsookram.srs.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.BackdropScaffold
import androidx.compose.material.BackdropValue
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.rememberBackdropScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

typealias DeckName = String

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
    val scaffoldState =
        rememberBackdropScaffoldState(
            BackdropValue.Concealed,
            snackbarHostState = snackbarHostState,
        )

    var showImportWarningDialog by rememberSaveable { mutableStateOf(false) }

    BoxWithConstraints {
        val tallDisplay = maxHeight > 480.dp

        BackdropScaffold(
            scaffoldState = scaffoldState,
            peekHeight = if (tallDisplay) 256.dp else 128.dp,
            headerHeight = if (tallDisplay) 128.dp else 64.dp,
            appBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name)) },
                    contentPadding =
                        rememberInsetsPaddingValues(
                            insets = LocalWindowInsets.current.systemBars,
                            applyBottom = false,
                        ),
                    actions = {
                        IconButton(onClick = onSearchClick) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = stringResource(R.string.search_for_card),
                            )
                        }

                        val expanded = rememberSaveable { mutableStateOf(false) }
                        OverflowMenu(expanded) {
                            DropdownMenuItem(
                                onClick = {
                                    expanded.value = false
                                    onExportClick()
                                }
                            ) { Text(stringResource(R.string.export_data)) }
                            DropdownMenuItem(
                                onClick = {
                                    expanded.value = false
                                    showImportWarningDialog = true
                                }
                            ) { Text(stringResource(R.string.import_data)) }
                        }
                    }
                )
            },
            backLayerContent = {
                Stats(
                    contentPadding =
                        rememberInsetsPaddingValues(
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

                Box(Modifier.fillMaxHeight()) {
                    DeckList(
                        decks,
                        onDeckClick,
                        onItemLongClick = { selectedDeck = it },
                        onCreateClick = { showCreateDeckDialog = true },
                    )

                    if (showAddCard) {
                        FloatingActionButton(
                            onClick = onAddCardClick,
                            modifier =
                                Modifier.align(Alignment.BottomEnd)
                                    .navigationBarsPadding()
                                    .padding(16.dp),
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = stringResource(R.string.create_card),
                            )
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
}

@Preview
@Composable
private fun HomePreview() = SrsTheme {
    val snackbarHostState = remember { SnackbarHostState() }

    Home(
        snackbarHostState,
        decks =
            listOf(
                DeckWithCount(id = 1, name = "??????", intervalModifier = 100, scheduledCardCount = 0),
                DeckWithCount(
                    id = 2,
                    name = "?????????",
                    intervalModifier = 100,
                    scheduledCardCount = 12
                ),
            ),
        globalStats =
            GlobalStats(
                activeCount = 1375,
                suspendedCount = 278,
                leechCount = 0,
                forReviewCount = 37,
            ),
        deckStats =
            listOf(
                DeckStats(
                    name = "prog",
                    activeCount = 1177,
                    suspendedCount = 266,
                    leechCount = 0,
                    correctCount = 391,
                    wrongCount = 17,
                ),
                DeckStats(
                    name = "??????",
                    activeCount = 60,
                    suspendedCount = 7,
                    leechCount = 0,
                    correctCount = 40,
                    wrongCount = 1,
                ),
                DeckStats(
                    name = "?????????",
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

@Composable
private fun DeckList(
    decks: List<DeckWithCount>,
    onItemClick: (DeckWithCount) -> Unit,
    onItemLongClick: (DeckWithCount) -> Unit,
    onCreateClick: () -> Unit,
) {
    LazyColumn(
        contentPadding =
            rememberInsetsPaddingValues(
                insets = LocalWindowInsets.current.navigationBars,
            )
    ) {
        item {
            Text(
                stringResource(R.string.card_deck_list),
                Modifier.padding(16.dp),
                style = MaterialTheme.typography.h6,
            )
        }

        items(decks) { deck ->
            DeckItem(
                Modifier.combinedClickable(
                    onClick = { onItemClick(deck) },
                    onLongClick = { onItemLongClick(deck) }
                ),
                deck.name,
                deck.scheduledCardCount,
            )
        }

        item { CreateDeckItem(Modifier.clickable(onClick = onCreateClick)) }
    }
}

@Composable
private fun DeckItem(
    modifier: Modifier = Modifier,
    deckName: String,
    scheduledCardCount: Long,
) {
    Row(modifier.heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            deckName,
            Modifier.weight(1f).padding(horizontal = 16.dp),
        )

        Text(scheduledCardCount.toString(), Modifier.padding(end = 16.dp))
    }
}

@Preview
@Composable
private fun DeckItemPreview() = SrsTheme {
    DeckItem(
        deckName = "?????????",
        scheduledCardCount = 12,
    )
}

@Preview
@Composable
private fun CreateDeckItem(modifier: Modifier = Modifier) {
    Row(modifier.heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.padding(start = 16.dp),
        )

        Text(
            stringResource(R.string.create_card_deck),
            Modifier.weight(1f).padding(horizontal = 16.dp),
        )
    }
}
