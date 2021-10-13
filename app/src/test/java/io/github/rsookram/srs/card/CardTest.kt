package io.github.rsookram.srs.card

import androidx.compose.runtime.Composable
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import io.github.rsookram.srs.Deck
import io.github.rsookram.srs.home.DeckName
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// https://github.com/robolectric/robolectric/issues/6593
@Config(instrumentedPackages = ["androidx.loader.content"])
@RunWith(RobolectricTestRunner::class)
class CardTest {

    @get:Rule val rule = createComposeRule()

    @Test
    fun canConfirmWhenCardIsFilledOut() {
        var clicked = false
        rule.setContent {
            CardWithDefaults(
                cardState = cardStateWithDefaults(front = "test front", back = "test back"),
                onConfirmClick = { clicked = true },
            )
        }

        rule.onNodeWithContentDescription("Confirm changes").performClick()

        assertTrue(clicked)
    }

    @Test
    fun cannotConfirmWhenCardIsFilledOut() {
        rule.setContent { CardWithDefaults(onConfirmClick = null) }

        rule.onNodeWithContentDescription("Confirm changes").assertDoesNotExist()
    }

    @Composable
    private fun CardWithDefaults(
        cardState: CardState = cardStateWithDefaults(),
        decks: List<Deck> = emptyList(),
        onUpClick: () -> Unit = {},
        onConfirmClick: (() -> Unit)? = null,
        enableDeletion: Boolean = false,
        onDeleteCardClick: () -> Unit = {},
    ) {
        Card(
            cardState,
            decks,
            onUpClick,
            onConfirmClick,
            enableDeletion,
            onDeleteCardClick,
        )
    }

    private fun cardStateWithDefaults(
        front: String = "",
        onFrontChange: (String) -> Unit = {},
        back: String = "",
        onBackChange: (String) -> Unit = {},
        selectedDeckName: DeckName = "",
        onDeckClick: (Deck) -> Unit = {},
        frontFocusRequester: FocusRequester = FocusRequester(),
    ) =
        CardState(
            front,
            onFrontChange,
            back,
            onBackChange,
            selectedDeckName,
            onDeckClick,
            frontFocusRequester
        )
}
