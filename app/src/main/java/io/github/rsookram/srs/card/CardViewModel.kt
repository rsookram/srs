package io.github.rsookram.srs.card

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.rsookram.srs.ApplicationScope
import io.github.rsookram.srs.Deck
import io.github.rsookram.srs.Srs
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class CardViewModel
@Inject
constructor(
    private val srs: Srs,
    savedStateHandle: SavedStateHandle,
    @ApplicationScope private val applicationScope: CoroutineScope,
) : ViewModel() {

    val cardId: Long? = savedStateHandle.get<Long>("id")

    var front by mutableStateOf("")
        private set
    var back by mutableStateOf("")
        private set

    val decks: Flow<List<Deck>> = srs.getDecks()

    private var deck by mutableStateOf<Deck?>(null)
    val selectedDeckName: String by derivedStateOf { deck?.name.orEmpty() }

    var enableDeletion by mutableStateOf(false)

    init {
        enableDeletion = cardId != null

        viewModelScope.launch {
            if (cardId != null) {
                val cardAndDeck = srs.getCardAndDeck(cardId)
                if (cardAndDeck != null) {
                    val (card, deck) = cardAndDeck

                    front = card.front
                    back = card.back

                    this@CardViewModel.deck = deck
                }
            } else {
                // Default the new card to being in the first deck returned
                val decks = srs.getDecks().first()
                deck = decks.first()
            }
        }
    }

    fun onFrontChange(s: String) {
        front = s
    }

    fun onBackChange(s: String) {
        back = s
    }

    fun onDeckClick(deck: Deck) {
        this.deck = deck
    }

    fun onConfirmClick() {
        val deck = deck ?: return

        applicationScope.launch {
            val id = cardId
            if (id == null) {
                srs.createCard(deck.id, front, back)
            } else {
                srs.editCard(id, deck.id, front, back)
            }
        }
    }

    fun onDeleteCardClick() {
        val id = cardId ?: return

        applicationScope.launch { srs.deleteCard(id) }
    }
}

@Composable
fun CardScreen(navController: NavController, vm: CardViewModel = hiltViewModel()) {
    Card(
        front = vm.front,
        onFrontChange = vm::onFrontChange,
        back = vm.back,
        onBackChange = vm::onBackChange,
        selectedDeckName = vm.selectedDeckName,
        onDeckClick = vm::onDeckClick,
        decks = vm.decks.collectAsState(emptyList()).value,
        onUpClick = { navController.popBackStack() },
        onConfirmClick = {
            // TODO: Add support for adding multiple cards without leaving screen
            vm.onConfirmClick()
            navController.popBackStack()
        },
        enableDeletion = vm.enableDeletion,
        onDeleteCardClick = {
            vm.onDeleteCardClick()
            navController.popBackStack()
        }
    )
}
