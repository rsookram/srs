package io.github.rsookram.srs.home

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.rsookram.srs.DeckWithCount
import io.github.rsookram.srs.R
import io.github.rsookram.srs.ui.BottomBar
import io.github.rsookram.srs.ui.TopLevelScreen
import io.github.rsookram.srs.ui.theme.SrsTheme

@Composable
fun Home(decks: List<DeckWithCount>) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.app_name)) })
        },
        bottomBar = {
            BottomBar(selected = TopLevelScreen.HOME, onItemClick = { /*TODO*/ })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /*TODO*/ }) {
                Icon(Icons.Default.Add, contentDescription = "Add card")
            }
        }
    ) {
        LazyColumn {
            items(decks) { deck ->
                DeckItem(deck = deck)
            }

            item { AddDeckItem() }
        }
    }
}

@Preview
@Composable
private fun HomePreview() {
    SrsTheme {
        Home(
            decks = listOf(
                DeckWithCount(id = 1, name = "中文", scheduledCardCount = 0),
                DeckWithCount(id = 2, name = "日本語", scheduledCardCount = 12),
            )
        )
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
    DeckItem(deck = DeckWithCount(id = 1, name = "日本語", scheduledCardCount = 12))
}

@Composable
fun AddDeckItem(modifier: Modifier = Modifier) {
    Row(modifier.heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.padding(start = 16.dp),
        )

        Text(
            "Add deck",
            Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
        )
    }
}

@Preview
@Composable
private fun AddDeckItemPreview() = SrsTheme {
    AddDeckItem()
}
