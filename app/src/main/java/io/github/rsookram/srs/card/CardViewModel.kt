package io.github.rsookram.srs.card

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
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
    @ApplicationScope private val applicationScope: CoroutineScope,
) : ViewModel() {

    var cardId: Long? = null
        set(value) {
            field = value

            enableDeletion = value != null

            viewModelScope.launch {
                if (value != null) {
                    val cardAndDeck = srs.getCardAndDeck(value)
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

    var front by mutableStateOf("")
        private set
    var back by mutableStateOf("")
        private set

    val decks: Flow<List<Deck>> = srs.getDecks()

    private var deck by mutableStateOf<Deck?>(null)
    val selectedDeckName: String by derivedStateOf { deck?.name.orEmpty() }

    var enableDeletion by mutableStateOf(false)

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
fun CardScreen(navController: NavController, cardId: Long?, vm: CardViewModel = hiltViewModel()) {
    // TODO: Get this through saved state handle
    LaunchedEffect(key1 = cardId) { vm.cardId = cardId }

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
