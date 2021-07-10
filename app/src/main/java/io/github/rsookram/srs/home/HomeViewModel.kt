package io.github.rsookram.srs.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.rsookram.srs.ApplicationScope
import io.github.rsookram.srs.DeckWithCount
import io.github.rsookram.srs.Srs
import io.github.rsookram.srs.ui.TopLevelScreen
import io.github.rsookram.srs.ui.navigate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val srs: Srs,
    @ApplicationScope private val applicationScope: CoroutineScope,
) : ViewModel() {

    val decks: Flow<List<DeckWithCount>> = srs.getDecksWithCount()

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
}

@Composable
fun HomeScreen(navController: NavController, vm: HomeViewModel = hiltViewModel()) {
    val decks by vm.decks.collectAsState(initial = emptyList())

    Home(
        decks,
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
    )
}
