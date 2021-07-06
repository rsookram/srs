package io.github.rsookram.srs.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.rsookram.srs.ApplicationScope
import io.github.rsookram.srs.DeckWithCount
import io.github.rsookram.srs.Srs
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
}

@Composable
fun HomeScreen(vm: HomeViewModel = viewModel()) {
    val decks by vm.decks.collectAsState(initial = emptyList())

    Home(
        decks,
        vm::onCreateDeckClick,
        onNavItemClick = { /*TODO*/ },
        onAddCardClick = { /*TODO*/ },
    )
}
