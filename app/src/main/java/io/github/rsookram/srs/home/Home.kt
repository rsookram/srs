package io.github.rsookram.srs.home

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.rsookram.srs.DeckWithCount
import io.github.rsookram.srs.ui.theme.SrsTheme

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
