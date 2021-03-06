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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.items
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.navigationBarsWithImePadding
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.systemBarsPadding
import io.github.rsookram.srs.BrowserCard
import io.github.rsookram.srs.R
import io.github.rsookram.srs.ui.theme.SrsTheme

@Composable
fun Browser(
    cards: LazyPagingItems<BrowserCard>,
    query: String,
    onQueryChange: (String) -> Unit,
    onCardClick: (cardId: Long) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Scaffold(
        topBar = {
            SearchField(
                Modifier.systemBarsPadding(bottom = false).focusRequester(focusRequester),
                query,
                onQueryChange
            )
        },
        bottomBar = { Spacer(Modifier.navigationBarsWithImePadding().fillMaxWidth()) },
    ) { contentPadding ->
        Content(
            contentPadding =
                rememberInsetsPaddingValues(
                    insets = LocalWindowInsets.current.navigationBars,
                    applyBottom = false,
                    additionalTop = contentPadding.calculateTopPadding(),
                    additionalBottom = contentPadding.calculateBottomPadding(),
                ),
            cards,
            onCardClick,
        )
    }
}

@Composable
private fun SearchField(
    modifier: Modifier = Modifier,
    query: String,
    onQueryChange: (String) -> Unit,
) {
    Box(modifier) {
        Surface(Modifier.padding(8.dp), RoundedCornerShape(4.dp), elevation = 4.dp) {
            TextField(
                query,
                onQueryChange,
                Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null, Modifier.padding(8.dp))
                },
                placeholder = { Text(stringResource(R.string.search_for_card)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                // The Surface below handles the shape and the background colour
                shape = RectangleShape,
                colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent),
            )
        }
    }
}

@Preview
@Composable
private fun SearchFieldPreview() = SrsTheme { SearchField(query = "", onQueryChange = {}) }

@Composable
private fun Content(
    contentPadding: PaddingValues,
    cards: LazyPagingItems<BrowserCard>,
    onCardClick: (cardId: Long) -> Unit,
) {
    LazyColumn(contentPadding = contentPadding) {
        items(cards) { item ->
            val modifier = Modifier.heightIn(min = 48.dp)
            if (item != null) {
                Card(
                    modifier,
                    item.front,
                    item.isLeech,
                    onClick = { onCardClick(item.id) },
                )
            } else {
                Spacer(modifier)
            }
        }
    }
}

@Composable
private fun Card(modifier: Modifier, label: String, isLeech: Boolean, onClick: () -> Unit) {
    Text(
        label,
        Modifier.clickable { onClick() }
            .background(
                if (isLeech) {
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

@Preview
@Composable
private fun CardPreview() = SrsTheme {
    Card(
        Modifier.heightIn(min = 48.dp),
        label = "card is not leech",
        isLeech = false,
        onClick = {},
    )
}

@Preview
@Composable
private fun LeechCardPreview() = SrsTheme {
    Card(
        Modifier.heightIn(min = 48.dp),
        label = "card is leech",
        isLeech = true,
        onClick = {},
    )
}
