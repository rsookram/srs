package io.github.rsookram.srs.home

import android.os.Process
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.rsookram.srs.ApplicationScope
import io.github.rsookram.srs.Backup
import io.github.rsookram.srs.DeckWithCount
import io.github.rsookram.srs.Srs
import io.github.rsookram.srs.ui.TopLevelScreen
import io.github.rsookram.srs.ui.navigate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val srs: Srs,
    private val backup: Backup,
    @ApplicationScope private val applicationScope: CoroutineScope,
) : ViewModel() {

    val decks: Flow<List<DeckWithCount>> = srs.getDecksWithCount()

    private val _exportResults = Channel<Backup.CreateResult>()
    val exportResults = _exportResults.receiveAsFlow()

    private val _importErrors = Channel<Backup.ImportError>()
    val importErrors = _importErrors.receiveAsFlow()

    fun onCreateDeckClick(deckName: DeckName) {
        applicationScope.launch {
            srs.createDeck(deckName)
        }
    }

    fun onDeckSaveClick(deckId: Long, name: DeckName, intervalModifier: IntervalModifier) {
        applicationScope.launch {
            srs.editDeck(deckId, name, intervalModifier)
        }
    }

    fun onDeckDeleteClick(deckId: Long) {
        applicationScope.launch {
            srs.deleteDeck(deckId)
        }
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

    val decks by vm.decks.collectAsState(initial = emptyList())

    val getOutputFile = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument()
    ) { uri ->
        if (uri != null) {
            val stream = context.contentResolver.openOutputStream(uri)
            if (stream != null) {
                vm.onExportLocationSelect(stream)
            }
        }
    }

    val getInputFile = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
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
            val message = when (result) {
                Backup.CreateResult.SUCCESS -> "Successfully exported"
                Backup.CreateResult.TRANSACTION_IN_PROGRESS -> "Failed to export. Try again later."
                Backup.CreateResult.FAILED -> "Failed to export"
            }

            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(vm.exportResults) {
        vm.importErrors.collect { result ->
            val message = when (result) {
                Backup.ImportError.TRANSACTION_IN_PROGRESS -> "Failed to import. Try again later."
                Backup.ImportError.FAILED -> "Failed to import"
            }

            snackbarHostState.showSnackbar(message)
        }
    }

    Home(
        snackbarHostState,
        decks,
        onSearchClick = { navController.navigate("browser") },
        onExportClick = { getOutputFile.launch("srs.db") },
        onImportClick = { getInputFile.launch(arrayOf("application/octet-stream")) },
        vm::onCreateDeckClick,
        onNavItemClick = { screen ->
            if (screen != TopLevelScreen.HOME) navController.navigate(screen)
        },
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
