package io.github.rsookram.srs.card

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.rsookram.srs.ApplicationScope
import io.github.rsookram.srs.Card
import io.github.rsookram.srs.Deck
import io.github.rsookram.srs.Srs
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

private const val KEY_DECK_ID = "deck_id"

/** ViewModel for [Card]. */
@HiltViewModel
class CardViewModel
@Inject
constructor(
    private val srs: Srs,
    private val savedStateHandle: SavedStateHandle,
    @ApplicationScope private val applicationScope: CoroutineScope,
) : ViewModel() {

    /** The ID of the card being edited. null when creating a card. */
    val cardId: Long? = savedStateHandle.get<Long>("id")

    var front by mutableStateOf("")
        private set
    var back by mutableStateOf("")
        private set

    val decks: Flow<List<Deck>> = srs.getDecks()

    private var deck by mutableStateOf<Deck?>(null)
    val selectedDeckName: String
        get() = deck?.name.orEmpty()

    var enableDeletion by mutableStateOf(false)

    private val _upNavigations = Channel<Unit>()
    val upNavigations = _upNavigations.receiveAsFlow()

    private val _frontFocuses = Channel<Unit>()
    val frontFocuses = _frontFocuses.receiveAsFlow()

    init {
        enableDeletion = cardId != null

        viewModelScope.launch {
            var card: Card? = null
            if (cardId != null) {
                card = srs.getCard(cardId)
                if (card != null) {
                    front = card.front
                    back = card.back
                }
            } else {
                _frontFocuses.send(Unit)
            }

            val savedDeckId = savedStateHandle.get<Long>(KEY_DECK_ID)
            when {
                savedDeckId != null -> {
                    setDeckAndId(srs.getDeck(savedDeckId).first())
                }
                card != null -> {
                    setDeckAndId(srs.getDeck(card.deckId).first())
                }
                else -> {
                    // Default the new card to being in the first deck returned
                    val decks = srs.getDecks().first()
                    setDeckAndId(decks.first())
                }
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
        setDeckAndId(deck)
    }

    private fun setDeckAndId(deck: Deck) {
        this.deck = deck
        savedStateHandle.set(KEY_DECK_ID, deck.id)
    }

    fun onUpClick() {
        viewModelScope.launch { _upNavigations.send(Unit) }
    }

    fun isConfirmEnabled(): Boolean = front.isNotEmpty()

    fun onConfirmClick() {
        val deck = deck ?: return

        val currentFront = front
        val currentBack = back

        applicationScope.launch {
            val id = cardId
            if (id == null) {
                srs.createCard(deck.id, currentFront, currentBack)
                _frontFocuses.send(Unit)
            } else {
                srs.editCard(id, deck.id, currentFront, currentBack)
            }
        }

        if (cardId != null) {
            viewModelScope.launch { _upNavigations.send(Unit) }
        } else {
            // Allow multiple cards to be added without leaving the screen
            front = ""
            back = ""
        }
    }

    fun onDeleteCardClick() {
        val id = cardId ?: return

        applicationScope.launch { srs.deleteCard(id) }

        viewModelScope.launch { _upNavigations.send(Unit) }
    }
}

/** Composable to bind [CardViewModel] to [Card]. */
@Composable
fun CardScreen(navController: NavController, vm: CardViewModel = hiltViewModel()) {
    LaunchedEffect(vm.upNavigations) { vm.upNavigations.collect { navController.popBackStack() } }

    val focusRequester = remember { FocusRequester() }
    val cardState =
        CardState(
            front = vm.front,
            onFrontChange = vm::onFrontChange,
            back = vm.back,
            onBackChange = vm::onBackChange,
            selectedDeckName = vm.selectedDeckName,
            onDeckClick = vm::onDeckClick,
            frontFocusRequester = focusRequester,
        )

    LaunchedEffect(vm.frontFocuses) {
        vm.frontFocuses.collect { cardState.frontFocusRequester.requestFocus() }
    }

    Card(
        cardState,
        decks = vm.decks.collectAsState(emptyList()).value,
        onUpClick = vm::onUpClick,
        onConfirmClick = if (vm.isConfirmEnabled()) vm::onConfirmClick else null,
        enableDeletion = vm.enableDeletion,
        onDeleteCardClick = vm::onDeleteCardClick,
    )
}
