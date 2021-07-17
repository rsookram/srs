package io.github.rsookram.srs

import android.app.Application
import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import com.squareup.sqldelight.android.AndroidSqliteDriver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.time.Clock
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.random.Random

private const val DATABASE_NAME = "srs.db"

@HiltAndroidApp
class App : Application()

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope

@InstallIn(SingletonComponent::class)
@Module
class AppModule {

    @Singleton
    @Provides
    fun provideSrs(@ApplicationContext context: Context) =
        Srs(
            Database(
                AndroidSqliteDriver(
                    Database.Schema,
                    context,
                    name = DATABASE_NAME,
                    callback = object : AndroidSqliteDriver.Callback(Database.Schema) {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            // Disabled to simplify the backup / restore process by only needing to
                            // handle a single file.
                            db.disableWriteAheadLogging()

                            db.execSQL("PRAGMA foreign_keys=ON;")
                        }
                    }
                )
            ),
            random = Random.Default,
            clock = Clock.systemUTC(),
            ioDispatcher = Dispatchers.IO,
        )

    @Singleton
    @Provides
    fun provideBackup(@ApplicationContext context: Context) =
        Backup(context.getDatabasePath(DATABASE_NAME), Dispatchers.IO)

    @Singleton
    @ApplicationScope
    @Provides
    fun providesCoroutineScope() = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}
