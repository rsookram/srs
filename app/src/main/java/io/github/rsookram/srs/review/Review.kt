package io.github.rsookram.srs.review

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.rsookram.srs.CardToReview
import io.github.rsookram.srs.ui.theme.SrsTheme

@Composable
fun Review(
    deckName: String,
    card: CardToReview,
    showAnswer: Boolean,
    onShowAnswerClick: () -> Unit,
    onCorrectClick: () -> Unit,
    onWrongClick: () -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(deckName) }) },
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = card.front,
                Modifier.padding(horizontal = 16.dp, vertical = 48.dp),
                style = MaterialTheme.typography.h5,
            )

            if (showAnswer) {
                Divider()

                Text(
                    text = card.back,
                    Modifier
                        .padding(horizontal = 16.dp, vertical = 48.dp)
                        .weight(1f),
                    style = MaterialTheme.typography.h5,
                )

                AnswerButtons(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    onCorrectClick,
                    onWrongClick,
                )
            } else {
                Spacer(Modifier.weight(1f))

                TextButton(
                    onClick = onShowAnswerClick,
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                ) {
                    Text(text = "Show Answer")
                }
            }
        }
    }
}

@Composable
private fun AnswerButtons(
    modifier: Modifier,
    onCorrectClick: () -> Unit,
    onWrongClick: () -> Unit,
) {
    Row(modifier) {
        val size = Modifier
            .fillMaxHeight()
            .weight(1f)

        TextButton(onClick = onWrongClick, size) { Text(text = "X") }

        TextButton(onClick = onCorrectClick, size) { Text(text = "O") }
    }
}

@Preview
@Composable
fun ReviewPreview() {
    SrsTheme {
        var showAnswer by remember { mutableStateOf(false) }

        Review(
            deckName = "日本語",
            card = CardToReview(
                id = 1,
                front = "日",
                back = "本語",
            ),
            showAnswer = showAnswer,
            onShowAnswerClick = { showAnswer = true },
            onCorrectClick = { showAnswer = false },
            onWrongClick = { showAnswer = false },
        )
    }
}
