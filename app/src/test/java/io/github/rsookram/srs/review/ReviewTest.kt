package io.github.rsookram.srs.review

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ReviewTest {

    @get:Rule val rule = createComposeRule()

    @Test
    fun displaysDeckName() {
        rule.setContent { ReviewWithDefaults(deckName = "Trivia") }

        rule.onNodeWithText("Trivia").assertIsDisplayed()
    }

    @Test
    fun displaysFront() {
        rule.setContent { ReviewWithDefaults(front = "The front of the card") }

        rule.onNodeWithText("The front of the card").assertIsDisplayed()
    }

    @Test
    fun doesNotDisplayBackWhenAnswerIsNotShown() {
        rule.setContent {
            ReviewWithDefaults(
                back = "The back of the card",
                showAnswer = false,
            )
        }

        rule.onNodeWithText("The back of the card").assertDoesNotExist()
    }

    @Test
    fun displaysBackWhenAnswerIsShown() {
        rule.setContent {
            ReviewWithDefaults(
                back = "The back of the card",
                showAnswer = true,
            )
        }

        rule.onNodeWithText("The back of the card").assertIsDisplayed()
    }

    @Test
    fun clickToShowAnswer() {
        var clicked = false

        rule.setContent {
            ReviewWithDefaults(
                showAnswer = false,
                onShowAnswerClick = { clicked = true },
            )
        }

        rule.onNodeWithText("Show Answer").performClick()

        assertTrue(clicked)
    }

    @Test
    fun answerCorrectly() {
        var clicked = false

        rule.setContent {
            ReviewWithDefaults(
                showAnswer = true,
                onCorrectClick = { clicked = true },
            )
        }

        rule.onNodeWithContentDescription("Correct").performClick()

        assertTrue(clicked)
    }

    @Test
    fun answerWrongly() {
        var clicked = false

        rule.setContent {
            ReviewWithDefaults(
                showAnswer = true,
                onWrongClick = { clicked = true },
            )
        }

        rule.onNodeWithContentDescription("Wrong").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickUp() {
        var clicked = false

        rule.setContent { ReviewWithDefaults(onUpClick = { clicked = true }) }

        rule.onNodeWithContentDescription("Navigate up").performClick()

        assertTrue(clicked)
    }

    @Composable
    private fun ReviewWithDefaults(
        deckName: String = "",
        front: String = "",
        back: String = "",
        showAnswer: Boolean = false,
        onShowAnswerClick: () -> Unit = {},
        onCorrectClick: () -> Unit = {},
        onWrongClick: () -> Unit = {},
        onUpClick: () -> Unit = {},
        onEditCardClick: () -> Unit = {},
        onDeleteCardClick: () -> Unit = {},
    ) {
        Review(
            deckName,
            front,
            back,
            showAnswer,
            onShowAnswerClick,
            onCorrectClick,
            onWrongClick,
            onUpClick,
            onEditCardClick,
            onDeleteCardClick,
        )
    }
}
