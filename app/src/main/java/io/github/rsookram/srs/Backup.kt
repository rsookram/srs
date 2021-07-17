package io.github.rsookram.srs

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.io.OutputStream

class Backup(
    private val databaseFile: File,
    private val ioDispatcher: CoroutineDispatcher,
) {

    enum class CreateResult {
        SUCCESS,
        TRANSACTION_IN_PROGRESS,
        FAILED,
    }

    suspend fun create(dst: OutputStream): CreateResult = withContext(ioDispatcher) {
        if (!databaseFile.exists()) {
            return@withContext CreateResult.FAILED
        }

        val journalFile = databaseFile.resolveSibling("$databaseFile-journal")
        if (journalFile.length() > 0) {
            return@withContext CreateResult.TRANSACTION_IN_PROGRESS
        }

        try {
            databaseFile.inputStream().use { src ->
                dst.use { d ->
                    src.copyTo(d)
                }
            }
            CreateResult.SUCCESS
        } catch (e: Exception) {
            CreateResult.FAILED
        }
    }
}
