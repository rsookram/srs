package io.github.rsookram.srs.browser

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.rsookram.srs.BrowserCard
import io.github.rsookram.srs.Srs
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

@HiltViewModel
class BrowserViewModel @Inject constructor(srs: Srs) : ViewModel() {

    private val _queries = MutableStateFlow("")
    val queries: Flow<String> = _queries

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val pagers: Flow<PagingData<BrowserCard>> =
        _queries
            .debounce(300)
            .map { query ->
                Pager(
                    PagingConfig(
                        pageSize = 30,
                        enablePlaceholders = true,
                    ),
                    pagingSourceFactory = {
                        if (query.isNotEmpty()) srs.searchCards(query) else srs.browseCards()
                    },
                )
            }
            .flatMapLatest { it.flow }

    fun onQueryChange(q: String) {
        _queries.value = q
    }
}

@Composable
fun BrowserScreen(navController: NavController, vm: BrowserViewModel = hiltViewModel()) {
    val cardItems = vm.pagers.collectAsLazyPagingItems()

    Browser(
        cardItems,
        vm.queries.collectAsState("").value,
        vm::onQueryChange,
        onCardClick = { cardId -> navController.navigate("card/$cardId") }
    )
}
