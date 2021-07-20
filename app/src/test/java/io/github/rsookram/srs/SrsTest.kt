package io.github.rsookram.srs

import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class SrsTest {

    private val inMemorySqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).apply {
        Database.Schema.create(this)
    }

    private val now = Instant.parse("2021-07-19T21:52:30.00Z")

    private val clock = AdjustableClock(now)

    private val srs = Srs(
        Database(inMemorySqlDriver),
        NotRandom(),
        clock,
        Dispatchers.Unconfined,
        getZoneId = { ZoneOffset.UTC },
    )

    @Test
    fun emptyDb() = runBlocking {
        assertEquals(emptyList<Deck>(), srs.getDecks().first())
        assertEquals(emptyList<DeckWithCount>(), srs.getDecksWithCount().first())

        assertEquals(null, srs.getCardAndDeck(id = 1))

        assertEquals(
            Pair(
                GlobalStats(
                    activeCount = 0, suspendedCount = 0, leechCount = 0, forReviewCount = 0
                ),
                emptyList<DeckStats>(),
            ),
            srs.stats().first()
        )
    }

    @Test
    fun createDeck() = runBlocking {
        val deck = createAndReturnDeck("testName")

        val decks = srs.getDecks().first()
        assertEquals(1, decks.size)

        assertEquals("testName", deck.name)
        assertEquals(100, deck.intervalModifier)

        assertEquals(deck, srs.getDeck(deck.id).first())

        val deckWithCounts = srs.getDecksWithCount().first()
        assertEquals(1, deckWithCounts.size)

        val deckWithCount = deckWithCounts.first()
        assertEquals("testName", deckWithCount.name)
        assertEquals(100, deckWithCount.intervalModifier)
        assertEquals(0, deckWithCount.scheduledCardCount)
    }

    @Test
    fun editDeck() = runBlocking {
        val deck = createAndReturnDeck("testName")

        srs.editDeck(deck.id, name = "anotherName", intervalModifier = 120)

        val editedDeck = srs.getDeck(deck.id).first()
        assertEquals("anotherName", editedDeck.name)
        assertEquals(120, editedDeck.intervalModifier)
    }

    @Test
    fun deleteDeck() = runBlocking {
        val deck = createAndReturnDeck("testName")

        srs.deleteDeck(deck.id)

        assertEquals(emptyList<Deck>(), srs.getDecks().first())
        assertEquals(emptyList<DeckWithCount>(), srs.getDecksWithCount().first())
    }

    @Test
    fun createCard() = runBlocking {
        val deck = createAndReturnDeck("testName")

        val card = createAndReturnCard(deck, "front", "back")

        assertEquals(deck.id, card.deckId)
        assertEquals("front", card.front)
        assertEquals("back", card.back)

        val deckWithCounts = srs.getDecksWithCount().first()
        assertEquals(
            DeckWithCount(deck.id, deck.name, deck.intervalModifier, scheduledCardCount = 1),
            deckWithCounts.first()
        )

        assertEquals(
            Pair(
                GlobalStats(
                    activeCount = 1, suspendedCount = 0, leechCount = 0, forReviewCount = 0
                ),
                listOf(
                    DeckStats(
                        name = deck.name,
                        activeCount = 1,
                        suspendedCount = 0,
                        leechCount = 0,
                        correctCount = 0,
                        wrongCount = 0,
                    )
                ),
            ),
            srs.stats().first()
        )
    }

    @Test
    fun editCard() = runBlocking {
        val deck = createAndReturnDeck("testName")
        val card = createAndReturnCard(deck, "front", "back")

        srs.editCard(card.id, deck.id, "new front", "new back")

        val (editedCard, _) = srs.getCardAndDeck(card.id)!!
        assertEquals(deck.id, editedCard.deckId)
        assertEquals("new front", editedCard.front)
        assertEquals("new back", editedCard.back)
    }

    @Test
    fun moveCardBetweenDecks() = runBlocking {
        val deck = createAndReturnDeck("testName")
        val deck2 = createAndReturnDeck("another deck")
        val card = createAndReturnCard(deck, "front", "back")

        srs.editCard(card.id, deck2.id, card.front, card.back)

        val (editedCard, _) = srs.getCardAndDeck(card.id)!!
        assertEquals(deck2.id, editedCard.deckId)
        assertEquals("front", editedCard.front)
        assertEquals("back", editedCard.back)

        val deckWithCounts = srs.getDecksWithCount().first()
        assertEquals(
            setOf(
                DeckWithCount(deck.id, deck.name, deck.intervalModifier, scheduledCardCount = 0),
                DeckWithCount(deck2.id, deck2.name, deck2.intervalModifier, scheduledCardCount = 1),
            ),
            deckWithCounts.toSet()
        )
    }

    @Test
    fun deleteCard() = runBlocking {
        val deck = createAndReturnDeck("testName")
        val card = createAndReturnCard(deck, "front", "back")

        srs.deleteCard(card.id)

        assertNull(srs.getCardAndDeck(card.id))
    }

    @Test
    fun answeringCorrectRemovesCardFromReviewQueue() = runBlocking {
        val deck = createAndReturnDeck("testName")
        val card = createAndReturnCard(deck, "front", "back")

        srs.answerCorrect(card.id)

        val deckWithCounts = srs.getDecksWithCount().first()
        assertEquals(
            DeckWithCount(deck.id, deck.name, deck.intervalModifier, scheduledCardCount = 0),
            deckWithCounts.first()
        )

        assertEquals(emptyList<CardToReview>(), srs.getCardsToReview(deck.id).first())
    }

    @Test
    fun answeringWrongLeavesCardInReviewQueue() = runBlocking {
        val deck = createAndReturnDeck("testName")
        val card = createAndReturnCard(deck, "front", "back")

        srs.answerWrong(card.id)

        val deckWithCounts = srs.getDecksWithCount().first()
        assertEquals(
            DeckWithCount(deck.id, deck.name, deck.intervalModifier, scheduledCardCount = 1),
            deckWithCounts.first()
        )

        assertEquals(
            listOf(CardToReview(card.id, card.front, card.back)),
            srs.getCardsToReview(deck.id).first()
        )
    }

    @Test
    fun answeringCorrectIncreasesInterval() = runBlocking {
        val deck = createAndReturnDeck("testName")
        val card = createAndReturnCard(deck, "front", "back")

        var days = 0L

        srs.answerCorrect(card.id)

        days += 1
        card.assertScheduledIn(days)

        srs.answerCorrect(card.id)

        days += 4
        card.assertScheduledIn(days)

        srs.answerCorrect(card.id)

        days += 10
        card.assertScheduledIn(days)

        srs.answerCorrect(card.id)

        days += 25
        card.assertScheduledIn(days)

        srs.answerCorrect(card.id)

        days += 62
        card.assertScheduledIn(days)

        srs.answerCorrect(card.id)

        days += 155
        card.assertScheduledIn(days)

        srs.answerCorrect(card.id)
        card.assertNotScheduled() // auto suspend
    }

    @Test
    fun answeringCorrectIncreasesIntervalWithIntervalModifier() = runBlocking {
        val deck = createAndReturnDeck("testName")
        srs.editDeck(deck.id, deck.name, intervalModifier = 200)
        val card = createAndReturnCard(deck, "front", "back")

        var days = 0L

        srs.answerCorrect(card.id)

        days += 1
        card.assertScheduledIn(days)

        srs.answerCorrect(card.id)

        days += 4
        card.assertScheduledIn(days)

        srs.answerCorrect(card.id)

        days += 20
        card.assertScheduledIn(days)

        srs.answerCorrect(card.id)

        days += 100
        card.assertScheduledIn(days)

        srs.answerCorrect(card.id)

        card.assertNotScheduled() // auto suspend
    }

    @Test
    fun answeringWrongDecreasesInterval() = runBlocking {
        val deck = createAndReturnDeck("testName")
        val card = createAndReturnCard(deck, "front", "back")

        var days = 0L

        srs.answerCorrect(card.id)
        days += 1
        card.assertScheduledIn(days)

        srs.answerCorrect(card.id)
        days += 4
        card.assertScheduledIn(days)

        srs.answerCorrect(card.id)
        days += 10

        card.assertScheduledIn(days)

        srs.answerWrong(card.id)
        srs.answerCorrect(card.id)
        days += 7

        card.assertScheduledIn(days)
    }

    @Test
    fun markLeechAfterTooManyWrongAnswers() = runBlocking {
        val deck = createAndReturnDeck("testName")
        val card = createAndReturnCard(deck, "front", "back")

        repeat(4) {
            srs.answerWrong(card.id)
        }

        card.assertNotScheduled()
    }

    // TODO: Explicitly advance time in caller
    private suspend fun Card.assertScheduledIn(days: Long) {
        clock.instant = now.plus(days - 1, ChronoUnit.DAYS)
        val reviewsBeforeDay = srs.getCardsToReview(deckId).first()
        assertEquals(0, reviewsBeforeDay.size)

        clock.instant = now.plus(days, ChronoUnit.DAYS)
        val reviewsOnDay = srs.getCardsToReview(deckId).first()
        assertEquals(1, reviewsOnDay.size)

        val cardToReview = reviewsOnDay.first()
        assertEquals(id, cardToReview.id)
        assertEquals(front, cardToReview.front)
        assertEquals(back, cardToReview.back)
    }

    private suspend fun Card.assertNotScheduled() {
        clock.instant = now.plus(1024, ChronoUnit.DAYS)
        assertEquals(0, srs.getCardsToReview(deckId).first().size)
    }

    private suspend fun createAndReturnDeck(name: String): Deck {
        srs.createDeck(name)

        val decks = srs.getDecks().first()

        return decks.find { it.name == name }!!
    }

    private suspend fun createAndReturnCard(deck: Deck, front: String, back: String): Card {
        srs.createCard(deck.id, front, back)

        val cardToReview = srs.getCardsToReview(deck.id)
            .first()
            .find { it.front == front }!!

        return srs.getCardAndDeck(cardToReview.id)!!.first
    }
}
