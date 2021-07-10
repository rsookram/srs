package io.github.rsookram.srs.stats

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.rsookram.srs.DeckStats
import io.github.rsookram.srs.GlobalStats
import io.github.rsookram.srs.Srs
import io.github.rsookram.srs.ui.TopLevelScreen
import io.github.rsookram.srs.ui.navigate
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(srs: Srs) : ViewModel() {

    val stats: Flow<Pair<GlobalStats, List<DeckStats>>> = srs.stats()
}

@Composable
fun StatsScreen(navController: NavController, vm: StatsViewModel = hiltViewModel()) {
    val stats by vm.stats.collectAsState(initial = null)

    Stats(
        global = stats?.first,
        decks = stats?.second.orEmpty(),
        onNavItemClick = { screen ->
            if (screen != TopLevelScreen.STATS) navController.navigate(screen)
        },
    )
}
