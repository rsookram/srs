package io.github.rsookram.srs.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.TopAppBar
import io.github.rsookram.srs.DeckStats
import io.github.rsookram.srs.GlobalStats
import io.github.rsookram.srs.ui.BottomBar
import io.github.rsookram.srs.ui.TopLevelScreen
import io.github.rsookram.srs.ui.theme.SrsTheme

@Composable
fun Stats(
    global: GlobalStats?,
    decks: List<DeckStats>,
    onNavItemClick: (TopLevelScreen) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stats") },
                contentPadding = rememberInsetsPaddingValues(
                    insets = LocalWindowInsets.current.systemBars,
                    applyBottom = false,
                ),
            )
        },
        bottomBar = {
            BottomBar(
                contentPadding = rememberInsetsPaddingValues(
                    insets = LocalWindowInsets.current.navigationBars
                ),
                selected = TopLevelScreen.STATS,
                onItemClick = onNavItemClick,
            )
        },
    ) { contentPadding ->
        LazyColumn(
            contentPadding = rememberInsetsPaddingValues(
                insets = LocalWindowInsets.current.navigationBars,
                applyBottom = false,
                additionalTop = contentPadding.calculateTopPadding(),
                additionalBottom = contentPadding.calculateBottomPadding(),
            )
        ) {
            if (global != null) {
                item { GlobalCard(global) }
            }

            items(decks) { deck -> DeckCard(deck) }
        }
    }
}

@Composable
private fun GlobalCard(global: GlobalStats) {
    Card(Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("All", style = MaterialTheme.typography.h6)
                Text(
                    "Total ${global.activeCount + global.suspendedCount + global.leechCount}",
                    style = MaterialTheme.typography.body2,
                )
            }

            Text(
                "${global.activeCount} active, " +
                    "${global.suspendedCount} suspended, " +
                    "${global.leechCount} leeches",
                Modifier.padding(top = 16.dp),
            )

            Text("Review tomorrow: ${global.forReviewCount}", Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun DeckCard(deck: DeckStats) {
    Card(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(deck.name, style = MaterialTheme.typography.h6)
                Text(
                    "Total ${deck.activeCount + deck.suspendedCount + deck.leechCount}",
                    style = MaterialTheme.typography.body2,
                )
            }

            Text(
                "${deck.activeCount} active, " +
                    "${deck.suspendedCount} suspended, " +
                    "${deck.leechCount} leeches",
                Modifier.padding(top = 16.dp),
            )

            val answerCount = deck.correctCount + deck.wrongCount
            val percentCorrect =
                if (answerCount > 0) deck.correctCount * 100 / answerCount else 0
            Text(
                "Past month accuracy: $percentCorrect% (${deck.correctCount} / $answerCount)",
                Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Preview
@Composable
private fun StatsPreview() = SrsTheme {
    Stats(
        global = GlobalStats(
            activeCount = 1375,
            suspendedCount = 278,
            leechCount = 0,
            forReviewCount = 37,
        ),
        decks = listOf(
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
        onNavItemClick = {},
    )
}
