package io.github.rsookram.srs.browser

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.rsookram.srs.Srs
import io.github.rsookram.srs.ui.TopLevelScreen
import io.github.rsookram.srs.ui.navigate
import javax.inject.Inject

@HiltViewModel
class BrowserViewModel @Inject constructor(srs: Srs) : ViewModel() {

    val pager = Pager(
        PagingConfig(
            pageSize = 30,
            enablePlaceholders = true,
        ),
        pagingSourceFactory = { srs.browseCards() },
    )
}

@Composable
fun BrowserScreen(navController: NavController, vm: BrowserViewModel = hiltViewModel()) {
    val cardItems = vm.pager.flow.collectAsLazyPagingItems()

    Browser(
        cardItems,
        onNavItemClick = { screen ->
            if (screen != TopLevelScreen.BROWSER) navController.navigate(screen)
        },
        onCardClick = { cardId ->
            navController.navigate("card/$cardId")
        }
    )
}
