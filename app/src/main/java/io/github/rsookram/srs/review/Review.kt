package io.github.rsookram.srs.review

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.TopAppBar
import io.github.rsookram.srs.R
import io.github.rsookram.srs.ui.ConfirmDeleteCardDialog
import io.github.rsookram.srs.ui.OverflowMenu
import io.github.rsookram.srs.ui.theme.SrsTheme

@Composable
fun Review(
    deckName: String,
    front: String,
    back: String,
    showAnswer: Boolean,
    onShowAnswerClick: () -> Unit,
    onCorrectClick: () -> Unit,
    onWrongClick: () -> Unit,
    onUpClick: () -> Unit,
    onEditCardClick: () -> Unit,
    onDeleteCardClick: () -> Unit,
) {
    var showConfirmDeleteDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(deckName) },
                contentPadding =
                    rememberInsetsPaddingValues(
                        insets = LocalWindowInsets.current.systemBars,
                        applyBottom = false,
                    ),
                navigationIcon = {
                    IconButton(onClick = onUpClick) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.toolbar_up_description),
                        )
                    }
                },
                actions = {
                    val expanded = rememberSaveable { mutableStateOf(false) }

                    OverflowMenu(expanded) {
                        DropdownMenuItem(
                            onClick = {
                                expanded.value = false
                                onEditCardClick()
                            }
                        ) { Text(stringResource(R.string.edit_card)) }

                        DropdownMenuItem(
                            onClick = {
                                expanded.value = false
                                showConfirmDeleteDialog = true
                            }
                        ) { Text(stringResource(R.string.delete_card)) }
                    }
                }
            )
        },
        bottomBar = { Spacer(Modifier.navigationBarsHeight().fillMaxWidth()) }
    ) { contentPadding ->
        Column(
            Modifier.padding(contentPadding).navigationBarsPadding(bottom = false),
        ) {
            Text(
                text = front,
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 48.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.h5,
            )

            if (showAnswer) {
                Divider()

                Text(
                    text = back,
                    Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 48.dp)
                        .weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.h5,
                )

                AnswerButtons(
                    Modifier.fillMaxWidth().height(56.dp),
                    onCorrectClick,
                    onWrongClick,
                )
            } else {
                Spacer(Modifier.weight(1f))

                TextButton(
                    onClick = onShowAnswerClick,
                    Modifier.fillMaxWidth().height(56.dp),
                ) { Text(stringResource(R.string.show_answer_to_card)) }
            }
        }

        if (showConfirmDeleteDialog) {
            ConfirmDeleteCardDialog(
                onConfirm = {
                    onDeleteCardClick()
                    showConfirmDeleteDialog = false
                },
                onDismiss = { showConfirmDeleteDialog = false },
            )
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
        val size = Modifier.fillMaxHeight().weight(1f)

        IconButton(onWrongClick, size) {
            Icon(
                Icons.Filled.Close,
                contentDescription = stringResource(R.string.answered_wrong),
            )
        }

        IconButton(onCorrectClick, size) {
            Icon(
                Icons.Filled.Check,
                contentDescription = stringResource(R.string.answered_correct),
            )
        }
    }
}

@Preview
@Composable
private fun ReviewPreview() {
    SrsTheme {
        var showAnswer by remember { mutableStateOf(false) }

        Review(
            deckName = "?????????",
            front = "???",
            back = "??????",
            showAnswer = showAnswer,
            onShowAnswerClick = { showAnswer = true },
            onCorrectClick = { showAnswer = false },
            onWrongClick = { showAnswer = false },
            onUpClick = {},
            onEditCardClick = {},
            onDeleteCardClick = {},
        )
    }
}
