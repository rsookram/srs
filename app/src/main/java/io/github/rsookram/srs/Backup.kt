package io.github.rsookram.srs

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.OutputStream

interface Backup {

    enum class CreateResult {
        SUCCESS,
        TRANSACTION_IN_PROGRESS,
        FAILED,
    }

    suspend fun create(dst: OutputStream): CreateResult
}

class AndroidBackup(
    private val context: Context,
    private val databaseName: String,
    private val ioDispatcher: CoroutineDispatcher,
) : Backup {

    override suspend fun create(dst: OutputStream): Backup.CreateResult =
        withContext(ioDispatcher) {
            val dbFile = context.getDatabasePath(databaseName)
            if (!dbFile.exists()) {
                return@withContext Backup.CreateResult.FAILED
            }

            val journalFile = dbFile.resolveSibling("$dbFile-journal")
            if (journalFile.length() > 0) {
                return@withContext Backup.CreateResult.TRANSACTION_IN_PROGRESS
            }

            try {
                dbFile.inputStream().use { src ->
                    dst.use { d ->
                        src.copyTo(d)
                    }
                }
                Backup.CreateResult.SUCCESS
            } catch (e: Exception) {
                e.printStackTrace()
                Backup.CreateResult.FAILED
            }
        }
}
