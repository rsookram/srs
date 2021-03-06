package io.github.rsookram.srs.home

import android.os.Process
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.rsookram.srs.ApplicationScope
import io.github.rsookram.srs.Backup
import io.github.rsookram.srs.DeckStats
import io.github.rsookram.srs.DeckWithCount
import io.github.rsookram.srs.GlobalStats
import io.github.rsookram.srs.R
import io.github.rsookram.srs.Srs
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel
@Inject
constructor(
    private val srs: Srs,
    private val backup: Backup,
    @ApplicationScope private val applicationScope: CoroutineScope,
) : ViewModel() {

    // Manually refresh the data when the app is foregrounded. The data needs to be refreshed
    // periodically because it depends on the current time. This refresh is driven by the lifecycle
    // (rather than the repository layer) so that it refreshes at a time relevant to the user.
    private val refreshCount = MutableStateFlow(0)

    val decks: Flow<List<DeckWithCount>> = refreshCount.flatMapLatest { srs.getDecksWithCount() }

    val stats: Flow<Pair<GlobalStats, List<DeckStats>>> = refreshCount.flatMapLatest { srs.stats() }

    private val _exportResults = Channel<Backup.CreateResult>()
    val exportResults = _exportResults.receiveAsFlow()

    private val _importErrors = Channel<Backup.ImportError>()
    val importErrors = _importErrors.receiveAsFlow()

    fun onForegrounded() {
        refreshCount.update { it + 1 }
    }

    fun onCreateDeckClick(deckName: DeckName) {
        applicationScope.launch { srs.createDeck(deckName) }
    }

    fun onDeckSaveClick(deckId: Long, name: DeckName, intervalModifier: IntervalModifier) {
        applicationScope.launch { srs.editDeck(deckId, name, intervalModifier) }
    }

    fun onDeckDeleteClick(deckId: Long) {
        applicationScope.launch { srs.deleteDeck(deckId) }
    }

    fun onExportLocationSelect(stream: OutputStream) {
        applicationScope.launch {
            val result = backup.create(stream)
            _exportResults.send(result)
        }
    }

    fun onImportLocationSelect(stream: InputStream) {
        applicationScope.launch {
            val error = backup.restore(stream)
            if (error == null) {
                // TODO: inject for testing
                Process.killProcess(Process.myPid())
                return@launch
            }

            _importErrors.send(error)
        }
    }
}

@Composable
fun HomeScreen(navController: NavController, vm: HomeViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val decks by vm.decks.collectAsState(initial = emptyList())

    val stats by vm.stats.collectAsState(initial = null)

    // TODO: try repeatOnLifecycle
    DisposableEffect(lifecycleOwner.lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                vm.onForegrounded()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val getOutputFile =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { uri ->
            if (uri != null) {
                val stream = context.contentResolver.openOutputStream(uri)
                if (stream != null) {
                    vm.onExportLocationSelect(stream)
                }
            }
        }

    val getInputFile =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                val stream = context.contentResolver.openInputStream(uri)
                if (stream != null) {
                    vm.onImportLocationSelect(stream)
                }
            }
        }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(vm.exportResults) {
        vm.exportResults.collect { result ->
            val message =
                when (result) {
                    Backup.CreateResult.SUCCESS -> R.string.successfully_exported_app_data
                    Backup.CreateResult.TRANSACTION_IN_PROGRESS ->
                        R.string.failed_to_export_app_data_try_again
                    Backup.CreateResult.FAILED -> R.string.failed_to_export_app_data
                }

            snackbarHostState.showSnackbar(context.getString(message))
        }
    }

    LaunchedEffect(vm.exportResults) {
        vm.importErrors.collect { result ->
            val message =
                when (result) {
                    Backup.ImportError.TRANSACTION_IN_PROGRESS ->
                        R.string.failed_to_import_app_data_try_again
                    Backup.ImportError.FAILED -> R.string.failed_to_import_app_data
                }

            snackbarHostState.showSnackbar(context.getString(message))
        }
    }

    Home(
        snackbarHostState,
        decks,
        globalStats = stats?.first,
        deckStats = stats?.second.orEmpty(),
        onSearchClick = { navController.navigate("browser") },
        onExportClick = { getOutputFile.launch("srs.db") },
        onImportClick = { getInputFile.launch(arrayOf("application/octet-stream")) },
        vm::onCreateDeckClick,
        // Adding a card requires a deck to add the card to
        showAddCard = decks.isNotEmpty(),
        onAddCardClick = { navController.navigate("card") },
        onDeckClick = { deck ->
            if (deck.scheduledCardCount > 0) {
                navController.navigate("review/${deck.id}")
            }
        },
        onDeckSaveClick = vm::onDeckSaveClick,
        onDeckDeleteClick = vm::onDeckDeleteClick,
    )
}
