package io.github.rsookram.srs.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.rsookram.srs.DeckStats
import io.github.rsookram.srs.GlobalStats
import io.github.rsookram.srs.R
import io.github.rsookram.srs.quantityStringResource
import io.github.rsookram.srs.ui.theme.SrsTheme

@Composable
fun Stats(
    contentPadding: PaddingValues = PaddingValues(0.dp),
    global: GlobalStats?,
    decks: List<DeckStats>,
) {
    LazyColumn(contentPadding = contentPadding) {
        if (global != null) {
            item { GlobalCard(global) }
        }

        items(decks) { deck -> DeckCard(deck) }
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
                Text(stringResource(R.string.deck_statistics), style = MaterialTheme.typography.h6)

                val total = global.activeCount + global.suspendedCount + global.leechCount
                Text(
                    stringResource(R.string.total_card_count, total.toInt(), total),
                    style = MaterialTheme.typography.body2,
                )
            }

            CardCounts(
                Modifier.padding(top = 16.dp),
                global.activeCount,
                global.suspendedCount,
                global.leechCount,
            )

            Text(
                stringResource(
                    R.string.cards_for_review_tomorrow,
                    global.forReviewCount.toInt(),
                    global.forReviewCount,
                ),
                Modifier.padding(top = 8.dp)
            )
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

                val total = deck.activeCount + deck.suspendedCount + deck.leechCount
                Text(
                    stringResource(R.string.total_card_count, total.toInt(), total),
                    style = MaterialTheme.typography.body2,
                )
            }

            CardCounts(
                Modifier.padding(top = 16.dp),
                deck.activeCount,
                deck.suspendedCount,
                deck.leechCount,
            )

            val answerCount = deck.correctCount + deck.wrongCount
            val percentCorrect = if (answerCount > 0) deck.correctCount * 100 / answerCount else 0
            Text(
                stringResource(
                    R.string.past_month_answer_accuracy,
                    percentCorrect,
                    deck.correctCount,
                    answerCount,
                ),
                Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun CardCounts(
    modifier: Modifier = Modifier,
    activeCount: Long,
    suspendedCount: Long,
    leechCount: Long,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(quantityStringResource(R.plurals.card_count_active, activeCount.toInt(), activeCount))

        VerticalDivider(Modifier.padding(horizontal = 4.dp))

        Text(
            quantityStringResource(
                R.plurals.card_count_suspended,
                suspendedCount.toInt(),
                suspendedCount
            )
        )

        if (leechCount > 0) {
            VerticalDivider(Modifier.padding(horizontal = 4.dp))

            Text(quantityStringResource(R.plurals.card_count_leech, leechCount.toInt(), leechCount))
        }
    }
}

@Composable
private fun VerticalDivider(modifier: Modifier = Modifier) {
    Box(
        modifier
            .height(16.dp)
            .width(1.dp)
            .background(MaterialTheme.colors.onSurface.copy(alpha = 0.32f))
    )
}

@Preview
@Composable
private fun StatsPreview() = SrsTheme {
    Stats(
        global =
            GlobalStats(
                activeCount = 1375,
                suspendedCount = 278,
                leechCount = 1,
                forReviewCount = 37,
            ),
        decks =
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
                    leechCount = 1,
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
    )
}
