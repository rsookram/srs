package io.github.rsookram.srs

import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.ZoneOffset
import kotlin.random.Random

class SrsTest {

    private val inMemorySqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).apply {
        Database.Schema.create(this)
    }

    private val srs = Srs(
        Database(inMemorySqlDriver),
        Random.Default,
        Clock.systemUTC(),
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
        assertEquals(1, decks.size)

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

    private suspend fun createAndReturnDeck(name: String): Deck {
        srs.createDeck("testName")

        val decks = srs.getDecks().first()

        return decks.find { it.name == name }!!
    }
}
