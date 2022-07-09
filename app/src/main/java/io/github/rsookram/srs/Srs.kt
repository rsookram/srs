package io.github.rsookram.srs

import androidx.paging.PagingSource
import com.squareup.sqldelight.android.paging3.QueryPagingSource
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.roundToLong
import kotlin.random.Random
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

private const val WRONG_ANSWERS_FOR_LEECH = 4
private const val WRONG_ANSWER_PENALTY = 0.7

/** See [Srs.startOfTomorrow] */
private const val START_HOUR_OF_DAY = 4

class Srs(
    private val db: Database,
    private val random: Random,
    private val clock: Clock,
    private val ioDispatcher: CoroutineDispatcher,
    private val getZoneId: (() -> ZoneId) = { ZoneId.systemDefault() },
) {

    fun getDeck(id: Long): Flow<Deck> = db.deckQueries.select(id).asFlow().mapToOne()

    fun getDecksWithCount(): Flow<List<DeckWithCount>> =
        db.deckQueries
            .deckWithCount(startOfTomorrow(clock.instant()).toEpochMilli())
            .asFlow()
            .mapToList()

    suspend fun createDeck(
        name: String,
    ) = withContext(ioDispatcher) { db.deckQueries.insert(name, clock.millis()) }

    suspend fun editDeck(
        id: Long,
        name: String,
        intervalModifier: Long,
    ) = withContext(ioDispatcher) { db.deckQueries.update(name, intervalModifier, id) }

    fun getDecks(): Flow<List<Deck>> = db.deckQueries.selectAll().asFlow().mapToList()

    suspend fun deleteDeck(id: Long) = withContext(ioDispatcher) { db.deckQueries.delete(id) }

    suspend fun getCard(id: Long): Card? =
        withContext(ioDispatcher) { db.cardQueries.select(id).executeAsOneOrNull() }

    fun browseCards(): PagingSource<Long, BrowserCard> {
        val cardQueries = db.cardQueries

        return QueryPagingSource(
            countQuery = cardQueries.countCards(),
            transacter = cardQueries,
            dispatcher = ioDispatcher,
            queryProvider = cardQueries::browserCard,
        )
    }

    fun searchCards(query: String): PagingSource<Long, BrowserCard> {
        val cardQueries = db.cardQueries

        return QueryPagingSource(
            countQuery = cardQueries.countSearchCards(query),
            transacter = cardQueries,
            dispatcher = ioDispatcher,
            queryProvider = { limit, offset ->
                cardQueries.browserSearchCard(query, limit, offset, ::BrowserCard)
            },
        )
    }

    suspend fun createCard(deckId: Long, front: String, back: String) =
        withContext(ioDispatcher) {
            db.transaction {
                db.cardQueries.insert(deckId, front, back, creationTimestamp = clock.millis())

                val id = db.cardQueries.getLastCreatedId().executeAsOne()
                db.scheduleQueries.insert(
                    id,
                    scheduledForTimestamp = clock.millis(),
                    intervalDays = 0,
                )
            }
        }

    suspend fun editCard(id: Long, deckId: Long, front: String, back: String) =
        withContext(ioDispatcher) { db.cardQueries.update(deckId, front, back, id) }

    suspend fun deleteCard(id: Long) = withContext(ioDispatcher) { db.cardQueries.delete(id) }

    fun getCardsToReview(deckId: Long): Flow<List<CardToReview>> =
        db.scheduleQueries
            .cardToReview(startOfTomorrow(clock.instant()).toEpochMilli(), deckId)
            .asFlow()
            .mapToList()

    suspend fun answerCorrect(cardId: Long) =
        withContext(ioDispatcher) {
            db.transaction {
                val intervalDays =
                    db.scheduleQueries.intervalDays(cardId).executeAsOneOrNull()?.intervalDays
                requireNotNull(intervalDays) { "Tried to answer suspended card with ID=$cardId" }

                db.answerQueries.insert(cardId, isCorrect = true, timestamp = clock.millis())

                when (intervalDays) {
                    // Newly added card answered correctly
                    0L -> db.scheduleQueries.scheduleCardIn(cardId, numDays = 1L)
                    // First review
                    1L -> db.scheduleQueries.scheduleCardIn(cardId, numDays = 4L)
                    else -> {
                        val (lastAnswer, intervalModifier) =
                            db.answerQueries.updateScheduleInfo(cardId).executeAsOne()

                        // Previous answer was correct
                        if (lastAnswer) {
                            val fuzz =
                                random.nextLong(
                                    (-intervalDays * fuzzFactor(intervalDays)).roundToLong(),
                                    (+intervalDays * fuzzFactor(intervalDays)).roundToLong() + 1
                                )

                            val numDays =
                                (intervalDays * 2.5 * (intervalModifier / 100.0)).toLong() + fuzz
                            if (numDays >= 365) {
                                // auto suspend since the card is known well enough
                                db.scheduleQueries.setTimestampAndInterval(
                                    scheduledForTimestamp = null,
                                    intervalDays = null,
                                    cardId,
                                )
                            } else {
                                db.scheduleQueries.scheduleCardIn(cardId, numDays)
                            }
                        } else {
                            // Last answer was wrong
                            val numDays =
                                (intervalDays.toDouble() * WRONG_ANSWER_PENALTY)
                                    .toLong()
                                    .coerceAtLeast(1)

                            db.scheduleQueries.scheduleCardIn(cardId, numDays)
                        }
                    }
                }
            }
        }

    /**
     * Returns a value in (0, 1) used to randomize the next interval for a card. This prevents
     * cards from getting grouped together based on when they were added.
     */
    private fun fuzzFactor(previousIntervalDays: Long): Double = when {
        previousIntervalDays < 7 -> 0.25
        previousIntervalDays < 30 -> 0.15
        else -> 0.05
    }

    private fun ScheduleQueries.scheduleCardIn(cardId: Long, numDays: Long) {
        setTimestampAndInterval(
            scheduledForTimestamp = clock.instant().plus(numDays, ChronoUnit.DAYS).toEpochMilli(),
            intervalDays = numDays,
            cardId,
        )
    }

    suspend fun answerWrong(cardId: Long) =
        withContext(ioDispatcher) {
            db.transaction {
                val now = clock.millis()
                db.answerQueries.insert(cardId, isCorrect = false, timestamp = now)

                db.scheduleQueries.setTimestamp(now, cardId)

                val numWrong = db.answerQueries.numWrong(cardId).executeAsOne()
                if (numWrong >= WRONG_ANSWERS_FOR_LEECH) {
                    db.scheduleQueries.markLeech(cardId)
                }
            }
        }

    fun stats(): Flow<Pair<GlobalStats, List<DeckStats>>> {
        val now = clock.instant()

        val tomorrowEnd = startOfTomorrow(now).plus(1, ChronoUnit.DAYS)

        val statsQuery =
            db.deckQueries.globalStats(reviewSpanEnd = tomorrowEnd.toEpochMilli())

        return statsQuery
            .asFlow()
            .mapToOne()
            .combine(
                db.deckQueries
                    .deckStats(
                        accuracySinceTimestamp = now.minus(30, ChronoUnit.DAYS).toEpochMilli()
                    )
                    .asFlow()
                    .mapToList(),
                transform = ::Pair,
            )
    }

    /**
     * Returns an [Instant] of the beginning of the next day relative to [now], where the first hour
     * of the day is given by [START_HOUR_OF_DAY].
     */
    private fun startOfTomorrow(now: Instant): Instant {
        val localNow = now.atZone(getZoneId())

        val localTomorrowStart =
            if (localNow.hour < START_HOUR_OF_DAY) {
                localNow.withHour(START_HOUR_OF_DAY)
            } else {
                localNow.plusDays(1).withHour(START_HOUR_OF_DAY)
            }

        return localTomorrowStart.toInstant()
    }
}
