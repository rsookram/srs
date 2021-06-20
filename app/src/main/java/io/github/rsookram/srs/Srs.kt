package io.github.rsookram.srs

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOneOrNull
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.Clock
import java.time.temporal.ChronoUnit

class Srs(
    private val db: Database,
    private val clock: Clock,
    private val ioDispatcher: CoroutineDispatcher,
) {

    fun getDecksWithCount(): Flow<List<DeckWithCount>> =
        db.deckQueries.deckWithCount(clock.instant().plus(1, ChronoUnit.DAYS).toEpochMilli())
            .asFlow()
            .mapToList()

    suspend fun createDeck(name: String) = withContext(ioDispatcher) {
        db.deckQueries.insert(name)
    }

    fun getCard(id: Long): Flow<Card?> =
        db.cardQueries.select(id)
            .asFlow()
            .mapToOneOrNull()

    suspend fun createCard(deckId: Long, front: String, back: String) = withContext(ioDispatcher) {
        db.transaction {
            db.cardQueries.insert(deckId, front, back)

            val id = db.cardQueries.getLastCreatedId().executeAsOne()
            db.scheduleQueries.insert(
                id,
                scheduledForTimestamp = clock.instant().toEpochMilli(),
                intervalDays = 0,
            )
        }
    }

    suspend fun editCard(id: Long, front: String, back: String) = withContext(ioDispatcher) {
        db.cardQueries.update(front, back, id)
    }
}
