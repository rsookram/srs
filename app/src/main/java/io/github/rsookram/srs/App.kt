package io.github.rsookram.srs

import android.app.Application
import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import java.time.Clock
import kotlin.random.Random

@HiltAndroidApp
class App : Application()

@InstallIn(SingletonComponent::class)
@Module
class AppModule {

    @Provides
    fun provideSrs(@ApplicationContext context: Context) =
        Srs(
            Database(AndroidSqliteDriver(Database.Schema, context, name = "srs.db")),
            random = Random.Default,
            clock = Clock.systemUTC(),
            ioDispatcher = Dispatchers.IO,
        )
}
