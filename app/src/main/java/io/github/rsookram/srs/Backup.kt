package io.github.rsookram.srs

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
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

    enum class ImportError {
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

    suspend fun restore(src: InputStream): ImportError? = withContext(ioDispatcher) {
        val journalFile = databaseFile.resolveSibling("$databaseFile-journal")
        if (journalFile.length() > 0) {
            return@withContext ImportError.TRANSACTION_IN_PROGRESS
        }

        val tmpFile = databaseFile.resolveSibling("$databaseFile.tmp")

        if (tmpFile.exists() && !tmpFile.delete()) {
            return@withContext ImportError.FAILED
        }

        try {
            tmpFile.createNewFile()

            tmpFile.outputStream().use { dst ->
                src.use { s ->
                    s.copyTo(dst)
                }
            }

            if (tmpFile.renameTo(databaseFile)) {
                null
            } else {
                ImportError.FAILED
            }
        } catch (e: Exception) {
            ImportError.FAILED
        }
    }
}
