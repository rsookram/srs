package io.github.rsookram.srs.review

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.rsookram.srs.ApplicationScope
import io.github.rsookram.srs.CardToReview
import io.github.rsookram.srs.Srs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class) // for flatMapLatest
@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val srs: Srs,
    @ApplicationScope private val applicationScope: CoroutineScope,
) : ViewModel() {

    private val deckId = MutableStateFlow<Long?>(null)

    private val cardsToReview: StateFlow<List<CardToReview>?> =
        deckId.filterNotNull()
            .flatMapLatest(srs::getCardsToReview)
            .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)

    val finishedReview: Flow<Boolean> = cardsToReview.map { it?.isEmpty() ?: false }

    var deckName: Flow<String> =
        deckId.filterNotNull()
            .flatMapLatest(srs::getDeck)
            .map { it.name }

    private val card: StateFlow<CardToReview?> =
        cardsToReview.map { it?.firstOrNull() }
            .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)

    val currentCardId: Long?
        get() = card.value?.id

    val front = card.map { it?.front.orEmpty() }
    val back = card.map { it?.back.orEmpty() }

    var showAnswer by mutableStateOf(false)
        private set

    fun setDeckId(deckId: Long) {
        this.deckId.value = deckId
    }

    fun onShowAnswerClick() {
        showAnswer = true
    }

    fun onCorrectClick() {
        val card = card.value ?: return
        applicationScope.launch {
            srs.answerCorrect(card.id)
        }

        showAnswer = false
    }

    fun onWrongClick() {
        val card = card.value ?: return
        applicationScope.launch {
            srs.answerWrong(card.id)
        }

        showAnswer = false
    }
}

@Composable
fun ReviewScreen(
    navController: NavController,
    deckId: Long,
    vm: ReviewViewModel = hiltViewModel(),
) {
    LaunchedEffect(key1 = deckId) {
        vm.setDeckId(deckId)
    }

    val isFinished by vm.finishedReview.collectAsState(initial = false)
    if (isFinished) {
        SideEffect {
            navController.popBackStack()
        }
        return
    }

    Review(
        deckName = vm.deckName.collectAsState(initial = "").value,
        front = vm.front.collectAsState(initial = "").value,
        back = vm.back.collectAsState(initial = "").value,
        showAnswer = vm.showAnswer,
        onShowAnswerClick = vm::onShowAnswerClick,
        onCorrectClick = vm::onCorrectClick,
        onWrongClick = vm::onWrongClick,
        onUpClick = { navController.popBackStack() },
        onEditCardClick = {
            val id = vm.currentCardId
            if (id != null) {
                navController.navigate("card/$id")
            }
        }
    )
}
