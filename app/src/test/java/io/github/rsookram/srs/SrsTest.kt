package io.github.rsookram.srs

import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import kotlin.random.Random

class SrsTest {

    private val inMemorySqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).apply {
        Database.Schema.create(this)
    }

    private val db = Database(inMemorySqlDriver)

    private val srs = Srs(
        db,
        Random.Default,
        Clock.systemUTC(),
        Dispatchers.Unconfined,
    )

    @Test
    fun emptyDb(): Unit = runBlocking {
        assertEquals(emptyList<Deck>(), srs.getDecks().first())
        assertEquals(emptyList<DeckWithCount>(), srs.getDecksWithCount().first())

        assertEquals(null, srs.getCard(id = 1).first())
        assertEquals(emptyList<CardToReview>(), srs.getCardsToReview().first())

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
}
