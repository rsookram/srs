package io.github.rsookram.srs.card

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ContentAlpha
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.primarySurface
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
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.navigationBarsWithImePadding
import com.google.accompanist.insets.rememberInsetsPaddingValues
import io.github.rsookram.srs.Deck
import io.github.rsookram.srs.home.DeckName
import io.github.rsookram.srs.ui.ConfirmDeleteCardDialog
import io.github.rsookram.srs.ui.OverflowMenu
import io.github.rsookram.srs.ui.theme.SrsTheme

/**
 * Screen which allows the user to enter the content of a new card, or to edit the content of an
 * existing one.
 */
@Composable
fun Card(
    front: String,
    onFrontChange: (String) -> Unit,
    back: String,
    onBackChange: (String) -> Unit,
    selectedDeckName: DeckName,
    onDeckClick: (Deck) -> Unit,
    decks: List<Deck>,
    onUpClick: () -> Unit,
    onConfirmClick: () -> Unit,
    enableDeletion: Boolean,
    onDeleteCardClick: () -> Unit,
) {
    var showConfirmDeleteDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colors.primarySurface) {
                TopAppBar(
                    modifier = Modifier.padding(
                        rememberInsetsPaddingValues(
                            insets = LocalWindowInsets.current.systemBars,
                            applyBottom = false,
                        )
                    ),
                ) {
                    Row(Modifier.width(68.dp), verticalAlignment = Alignment.CenterVertically) {
                        CompositionLocalProvider(
                            LocalContentAlpha provides ContentAlpha.high,
                            content = {
                                IconButton(onClick = onUpClick) {
                                    Icon(Icons.Filled.ArrowBack, contentDescription = null)
                                }
                            }
                        )
                    }

                    var deckListExpanded by remember { mutableStateOf(false) }

                    Box(Modifier.weight(1f)) {
                        Box(
                            Modifier
                                .clickable { deckListExpanded = true }
                                .fillMaxHeight()
                                .widthIn(min = 128.dp),
                            Alignment.CenterStart,
                        ) {
                            Text(
                                text = selectedDeckName,
                                Modifier.padding(horizontal = 16.dp),
                            )
                        }

                        DropdownMenu(
                            expanded = deckListExpanded,
                            onDismissRequest = { deckListExpanded = false }
                        ) {
                            decks.forEach { deck ->
                                DropdownMenuItem(onClick = { onDeckClick(deck) }) {
                                    Text(deck.name)
                                }
                            }
                        }
                    }

                    if (enableDeletion) {
                        DeleteOverflowMenu(onDeleteClick = { showConfirmDeleteDialog = true })
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onConfirmClick,
                modifier = Modifier.navigationBarsWithImePadding(),
            ) {
                Icon(Icons.Default.Check, contentDescription = "Confirm changes")
            }
        }
    ) { contentPadding ->
        val windowInsets = LocalWindowInsets.current

        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(
                    rememberInsetsPaddingValues(
                        insets = windowInsets.navigationBars + LocalWindowInsets.current.ime,
                        additionalTop = contentPadding.calculateTopPadding(),
                        additionalBottom = contentPadding.calculateBottomPadding(),
                    )
                )
        ) {
            OutlinedTextField(
                value = front,
                onValueChange = onFrontChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Front") },
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = back,
                onValueChange = onBackChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Back") },
            )
        }

        if (showConfirmDeleteDialog) {
            ConfirmDeleteCardDialog(
                onDeleteCardClick,
                onDismiss = { showConfirmDeleteDialog = false },
            )
        }
    }
}

@Composable
private fun DeleteOverflowMenu(onDeleteClick: () -> Unit) {
    val expanded = remember { mutableStateOf(false) }

    OverflowMenu(expanded) {
        DropdownMenuItem(
            onClick = {
                expanded.value = false
                onDeleteClick()
            }
        ) {
            Text("Delete card")
        }
    }
}

@Preview
@Composable
private fun CardPreview() = SrsTheme {
    val decks = listOf(
        Deck(id = 1, name = "中文", creationTimestamp = "", intervalModifier = 100),
        Deck(id = 2, name = "日本語", creationTimestamp = "", intervalModifier = 100),
    )

    Card(
        front = "",
        onFrontChange = {},
        back = "",
        onBackChange = {},
        selectedDeckName = decks.first().name,
        decks = decks,
        onUpClick = {},
        onDeckClick = {},
        onConfirmClick = {},
        enableDeletion = false,
        onDeleteCardClick = {},
    )
}
