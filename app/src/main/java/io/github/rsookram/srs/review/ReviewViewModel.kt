package io.github.rsookram.srs.review

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.rsookram.srs.ApplicationScope
import io.github.rsookram.srs.Srs
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val srs: Srs,
    @ApplicationScope private val applicationScope: CoroutineScope,
) : ViewModel() {

    // TODO: Finish implementing
    var deckId: Long? = null
        set(value) {
            field = value
        }

    var showAnswer by mutableStateOf(false)
        private set

    fun onShowAnswerClick() {
        showAnswer = true
    }

    fun onCorrectClick() {
        showAnswer = false
    }

    fun onWrongClick() {
        showAnswer = false
    }
}

@Composable
fun ReviewScreen(navController: NavController, deckId: Long, vm: ReviewViewModel = viewModel()) {
    vm.deckId = deckId

    Review(
        deckName = "",
        front = "",
        back = "",
        showAnswer = vm.showAnswer,
        onShowAnswerClick = vm::onShowAnswerClick,
        onCorrectClick = vm::onCorrectClick,
        onWrongClick = vm::onWrongClick,
        onUpClick = { navController.popBackStack() },
    )
}
