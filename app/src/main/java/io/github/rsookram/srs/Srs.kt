package io.github.rsookram.srs

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.temporal.ChronoUnit

class Srs(private val db: Database, private val ioDispatcher: CoroutineDispatcher) {

    suspend fun createDeck(name: String) = withContext(ioDispatcher) {
        db.deckQueries.insert(name)
    }

    fun getDecksWithCount(now: Instant): Flow<List<DeckWithCount>> =
        db.deckQueries.deckWithCount(now.plus(1, ChronoUnit.DAYS).toEpochMilli())
            .asFlow()
            .mapToList()
}
