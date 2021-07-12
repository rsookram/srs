package io.github.rsookram.srs.browser

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.items
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.TopAppBar
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
            TopAppBar(
                title = { Text("Browser") },
                contentPadding = rememberInsetsPaddingValues(
                    insets = LocalWindowInsets.current.systemBars,
                    applyBottom = false,
                ),
            )
        },
        bottomBar = {
            BottomBar(
                contentPadding = rememberInsetsPaddingValues(
                    insets = LocalWindowInsets.current.navigationBars
                ),
                selected = TopLevelScreen.BROWSER,
                onItemClick = onNavItemClick,
            )
        },
    ) { contentPadding ->
        LazyColumn(
            contentPadding = rememberInsetsPaddingValues(
                insets = LocalWindowInsets.current.navigationBars,
                applyBottom = false,
                additionalTop = contentPadding.calculateTopPadding(),
                additionalBottom = contentPadding.calculateBottomPadding(),
            )
        ) {
            items(cardItems) { item ->
                val modifier = Modifier.heightIn(min = 48.dp)
                if (item != null) {
                    Card(modifier, item, onCardClick)
                } else {
                    Spacer(modifier)
                }
            }
        }
    }
}

@Composable
private fun Card(
    modifier: Modifier,
    card: BrowserCard,
    onClick: (cardId: Long) -> Unit
) {
    Text(
        card.front,
        Modifier
            .clickable { onClick(card.id) }
            .background(
                if (card.isLeech) {
                    MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                } else {
                    Color.Transparent
                }
            )
            .fillMaxWidth()
            .then(modifier)
            .padding(16.dp)
    )
}
