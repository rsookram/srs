package io.github.rsookram.srs

import androidx.paging.PagingSource
import com.squareup.sqldelight.android.paging3.QueryPagingSource
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.roundToLong
import kotlin.random.Random

private const val WRONG_ANSWERS_FOR_LEECH = 4
private const val WRONG_ANSWER_PENALTY = 0.7
private const val FUZZ_FACTOR = 0.05

/** See [Srs.startOfTomorrow] */
private const val START_HOUR_OF_DAY = 4

class Srs(
    private val db: Database,
    private val random: Random,
    private val clock: Clock,
    private val ioDispatcher: CoroutineDispatcher,
    private val getZoneId: (() -> ZoneId) = { ZoneId.systemDefault() },
) {

    fun getDeck(id: Long): Flow<Deck> =
        db.deckQueries.select(id)
            .asFlow()
            .mapToOne()

    fun getDecksWithCount(): Flow<List<DeckWithCount>> =
        db.deckQueries.deckWithCount(startOfTomorrow(clock.instant()).toEpochMilli())
            .asFlow()
            .mapToList()

    suspend fun createDeck(name: String) = withContext(ioDispatcher) {
        db.deckQueries.insert(name)
    }

    suspend fun editDeck(
        id: Long,
        name: String,
        intervalModifier: Long,
    ) = withContext(ioDispatcher) {
        db.deckQueries.update(name, intervalModifier, id)
    }

    fun getDecks(): Flow<List<Deck>> =
        db.deckQueries.selectAll()
            .asFlow()
            .mapToList()

    suspend fun deleteDeck(id: Long) = withContext(ioDispatcher) {
        db.deckQueries.delete(id)
    }

    suspend fun getCardAndDeck(id: Long): Pair<Card, Deck>? = withContext(ioDispatcher) {
        val card = db.cardQueries.select(id).executeAsOneOrNull()

        if (card != null) {
            card to db.deckQueries.select(card.deckId).executeAsOne()
        } else {
            null
        }
    }

    fun browseCards(): PagingSource<Long, BrowserCard> {
        val cardQueries = db.cardQueries

        return QueryPagingSource(
            countQuery = cardQueries.countCards(),
            transacter = cardQueries,
            dispatcher = ioDispatcher,
            queryProvider = cardQueries::browserCard,
        )
    }

    suspend fun createCard(deckId: Long, front: String, back: String) = withContext(ioDispatcher) {
        db.transaction {
            db.cardQueries.insert(deckId, front, back)

            val id = db.cardQueries.getLastCreatedId().executeAsOne()
            db.scheduleQueries.insert(
                id,
                scheduledForTimestamp = clock.millis(),
                intervalDays = 0,
            )
        }
    }

    suspend fun editCard(id: Long, deckId: Long, front: String, back: String) = withContext(ioDispatcher) {
        db.cardQueries.update(deckId, front, back, id)
    }

    suspend fun deleteCard(id: Long) = withContext(ioDispatcher) {
        db.cardQueries.delete(id)
    }

    fun getCardsToReview(deckId: Long): Flow<List<CardToReview>> =
        db.scheduleQueries.cardToReview(startOfTomorrow(clock.instant()).toEpochMilli(), deckId)
            .asFlow()
            .mapToList()

    suspend fun answerCorrect(cardId: Long) = withContext(ioDispatcher) {
        db.transaction {
            val (lastAnswer, intervalDays, intervalModifier) = db.answerQueries
                .updateScheduleInfo(cardId)
                .executeAsOne()

            requireNotNull(intervalDays) { "Tried to answer suspended card with ID=$cardId" }

            db.answerQueries.insert(cardId, isCorrect = true, timestamp = clock.millis())

            when {
                // Newly added card answered correctly
                intervalDays == 0L -> db.scheduleQueries.increaseIntervalTo(cardId, numDays = 1L)
                // First review
                intervalDays == 1L -> db.scheduleQueries.increaseIntervalTo(cardId, numDays = 4L)
                // Previous answer was correct
                lastAnswer -> {
                    val fuzz = random.nextLong(
                        (-intervalDays * FUZZ_FACTOR).roundToLong(),
                        (+intervalDays * FUZZ_FACTOR).roundToLong()
                    )

                    val numDays = (intervalDays * 2.5 * (intervalModifier / 100.0)).toLong() + fuzz
                    if (numDays >= 365) {
                        // auto suspend since the card is known well enough
                        db.scheduleQueries.setTimestampAndInterval(
                            scheduledForTimestamp = null,
                            intervalDays = null,
                            cardId,
                        )
                    } else {
                        db.scheduleQueries.increaseIntervalTo(cardId, numDays)
                    }
                }
                // Last answer was wrong
                else -> {
                    val numDays = (intervalDays.toDouble() * WRONG_ANSWER_PENALTY)
                        .toLong()
                        .coerceAtLeast(1)

                    db.scheduleQueries.increaseIntervalTo(cardId, numDays)
                }
            }
        }
    }

    private fun ScheduleQueries.increaseIntervalTo(cardId: Long, numDays: Long) {
        setTimestampAndInterval(
            scheduledForTimestamp = clock.instant().plus(numDays, ChronoUnit.DAYS).toEpochMilli(),
            intervalDays = numDays,
            cardId,
        )
    }

    suspend fun answerWrong(cardId: Long) = withContext(ioDispatcher) {
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

        val tomorrowStart = startOfTomorrow(now)
        val tomorrowEnd = tomorrowStart.plus(1, ChronoUnit.DAYS)

        val statsQuery = db.deckQueries.globalStats(
            reviewSpanStart = tomorrowStart.toEpochMilli(),
            reviewSpanEnd = tomorrowEnd.toEpochMilli()
        )

        return statsQuery
            .asFlow()
            .mapToOne()
            .combine(
                db.deckQueries.deckStats(
                    accuracySinceTimestamp = now.minus(30, ChronoUnit.DAYS).toEpochMilli()
                ).asFlow().mapToList(),
                transform = ::Pair,
            )
    }

    /**
     * Returns an [Instant] of the beginning of the next day relative to [now], where the first
     * hour of the day is given by [START_HOUR_OF_DAY].
     */
    private fun startOfTomorrow(now: Instant): Instant {
        val localNow = now.atZone(getZoneId())

        val localTomorrowStart = if (localNow.hour < START_HOUR_OF_DAY) {
            localNow.withHour(START_HOUR_OF_DAY)
        } else {
            localNow.plusDays(1).withHour(START_HOUR_OF_DAY)
        }

        return localTomorrowStart.toInstant()
    }
}
