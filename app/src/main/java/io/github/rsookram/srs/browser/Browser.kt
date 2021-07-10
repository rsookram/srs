package io.github.rsookram.srs.browser

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.items
import io.github.rsookram.srs.BrowserCard
import io.github.rsookram.srs.ui.BottomBar
import io.github.rsookram.srs.ui.TopLevelScreen

// TODO: Add search
@Composable
fun Browser(
    cardItems: LazyPagingItems<BrowserCard>,
    onNavItemClick: (TopLevelScreen) -> Unit,
    onCardClick: (cardId: Long) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Browser") })
        },
        bottomBar = {
            BottomBar(selected = TopLevelScreen.BROWSER, onItemClick = onNavItemClick)
        },
    ) {
        LazyColumn {
            items(cardItems) { item ->
                val modifier = Modifier.heightIn(min = 48.dp)
                if (item != null) {
                    // TODO: background colour for leech
                    Text(item.front, modifier.clickable { onCardClick(item.id) })
                } else {
                    Spacer(modifier)
                }
            }
        }
    }
}
