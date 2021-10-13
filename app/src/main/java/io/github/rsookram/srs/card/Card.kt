package io.github.rsookram.srs.card

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.navigationBarsWithImePadding
import com.google.accompanist.insets.systemBarsPadding
import io.github.rsookram.srs.Deck
import io.github.rsookram.srs.R
import io.github.rsookram.srs.home.DeckName
import io.github.rsookram.srs.ui.ConfirmDeleteCardDialog
import io.github.rsookram.srs.ui.OverflowMenu
import io.github.rsookram.srs.ui.theme.SrsTheme

/**
 * State for [Card] composable component.
 *
 * @param front the text that will be shown on the front side of the card (the question)
 * @param back the text that will be shown on the back side of the card (the answer)
 * @param selectedDeckName the name of the deck that the card is part of. Empty string for a new
 * card that isn't part of a deck yet.
 * @param frontFocusRequester used to move the focus to the field displaying [front]
 */
class CardState(
    val front: String,
    val onFrontChange: (String) -> Unit,
    val back: String,
    val onBackChange: (String) -> Unit,
    val selectedDeckName: DeckName,
    val onDeckClick: (Deck) -> Unit,
    val frontFocusRequester: FocusRequester,
)

/**
 * Screen which allows the user to enter the content of a new card, or to edit the content of an
 * existing one.
 */
@Composable
fun Card(
    cardState: CardState,
    decks: List<Deck>,
    onUpClick: () -> Unit,
    onConfirmClick: (() -> Unit)?,
    enableDeletion: Boolean,
    onDeleteCardClick: () -> Unit,
) {
    var showConfirmDeleteDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(Modifier.systemBarsPadding(bottom = false)) {
                Box(Modifier.width(68.dp)) {
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
                        IconButton(onClick = onUpClick) {
                            Icon(
                                Icons.Filled.ArrowBack,
                                contentDescription =
                                    stringResource(R.string.toolbar_up_description),
                            )
                        }
                    }
                }

                DeckDropdownMenu(
                    Modifier.weight(1f),
                    cardState.selectedDeckName,
                    decks,
                    cardState.onDeckClick
                )

                if (enableDeletion) {
                    DeleteOverflowMenu(onDeleteClick = { showConfirmDeleteDialog = true })
                }
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = onConfirmClick != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FloatingActionButton(
                    onConfirmClick ?: {},
                    Modifier.navigationBarsWithImePadding(),
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = stringResource(R.string.confirm_changes_to_card),
                    )
                }
            }
        }
    ) { contentPadding ->
        Column(
            Modifier.verticalScroll(rememberScrollState())
                .padding(16.dp)
                .navigationBarsWithImePadding()
                .padding(
                    PaddingValues(
                        top = contentPadding.calculateTopPadding(),
                        bottom = contentPadding.calculateBottomPadding(),
                    )
                )
        ) {
            OutlinedTextField(
                value = cardState.front,
                onValueChange = cardState.onFrontChange,
                modifier = Modifier.fillMaxWidth().focusRequester(cardState.frontFocusRequester),
                label = { Text(stringResource(R.string.front_side_of_card)) },
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = cardState.back,
                onValueChange = cardState.onBackChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.back_side_of_card)) },
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

@Preview
@Composable
private fun CardPreview() = SrsTheme {
    val decks =
        listOf(
            Deck(id = 1, name = "中文", creationTimestamp = 0, intervalModifier = 100),
            Deck(id = 2, name = "日本語", creationTimestamp = 0, intervalModifier = 100),
        )

    Card(
        CardState(
            front = "",
            onFrontChange = {},
            back = "",
            onBackChange = {},
            selectedDeckName = decks.first().name,
            onDeckClick = {},
            frontFocusRequester = FocusRequester(),
        ),
        decks = decks,
        onUpClick = {},
        onConfirmClick = {},
        enableDeletion = false,
        onDeleteCardClick = {},
    )
}

/** Exposed dropdown menu which allows the user to select which deck the card is part of. */
@Composable
private fun DeckDropdownMenu(
    modifier: Modifier = Modifier,
    selectedDeckName: DeckName,
    decks: List<Deck>,
    onDeckClick: (Deck) -> Unit,
) {
    var deckListExpanded by remember { mutableStateOf(false) }

    // Replace with ExposedDropdownMenu when available:
    // https://issuetracker.google.com/issues/172170247
    Box(modifier) {
        Row(
            Modifier.clickable { deckListExpanded = true }
                .fillMaxHeight()
                .widthIn(min = 128.dp, max = 144.dp)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = selectedDeckName, Modifier.weight(1f), overflow = TextOverflow.Ellipsis)

            Icon(
                Icons.Filled.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.rotate(if (deckListExpanded) 180f else 0f),
            )
        }

        DropdownMenu(
            expanded = deckListExpanded,
            onDismissRequest = { deckListExpanded = false },
        ) {
            decks.forEach { deck ->
                DropdownMenuItem(
                    onClick = {
                        onDeckClick(deck)
                        deckListExpanded = false
                    }
                ) { Text(deck.name) }
            }
        }
    }
}

@Preview(widthDp = 156, heightDp = 56)
@Composable
private fun DeckDropdownMenuPreview() = SrsTheme {
    DeckDropdownMenu(selectedDeckName = "日本語", decks = emptyList(), onDeckClick = {})
}

/** Overflow menu which presents the option to delete the card being edited. */
@Composable
private fun DeleteOverflowMenu(onDeleteClick: () -> Unit) {
    val expanded = remember { mutableStateOf(false) }

    OverflowMenu(expanded) {
        DropdownMenuItem(
            onClick = {
                expanded.value = false
                onDeleteClick()
            }
        ) { Text(stringResource(R.string.delete_card)) }
    }
}
