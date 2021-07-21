package io.github.rsookram.srs.browser

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.items
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.rememberInsetsPaddingValues
import io.github.rsookram.srs.BrowserCard

@Composable
fun Browser(
    cardItems: LazyPagingItems<BrowserCard>,
    query: String,
    onQueryChange: (String) -> Unit,
    onCardClick: (cardId: Long) -> Unit,
) {
    Scaffold(
        topBar = {
            SearchField(
                contentPadding = rememberInsetsPaddingValues(
                    insets = LocalWindowInsets.current.systemBars,
                    applyBottom = false,
                ),
                query,
                onQueryChange
            )
        },
        bottomBar = {
            Spacer(
                Modifier
                    .navigationBarsHeight()
                    .fillMaxWidth()
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
private fun SearchField(
    contentPadding: PaddingValues,
    query: String,
    onQueryChange: (String) -> Unit,
) {
    Box(modifier = Modifier.padding(contentPadding)) {
        Surface(Modifier.padding(8.dp), RoundedCornerShape(4.dp), elevation = 4.dp) {
            TextField(
                query,
                onQueryChange,
                Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = null,
                        Modifier.padding(8.dp)
                    )
                },
                placeholder = { Text("Search") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                // The Surface below handles the shape and the background colour
                shape = RectangleShape,
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent
                ),
            )
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
