package io.github.rsookram.srs.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.google.accompanist.insets.ui.BottomNavigation
import io.github.rsookram.srs.ui.theme.SrsTheme

enum class TopLevelScreen {
    HOME, STATS,
}

@Composable
fun BottomBar(
    contentPadding: PaddingValues = PaddingValues(0.dp),
    selected: TopLevelScreen,
    onItemClick: (TopLevelScreen) -> Unit,
) {
    BottomNavigation(contentPadding = contentPadding) {
        val screens = TopLevelScreen.values()

        screens.forEach { item ->
            BottomNavigationItem(
                icon = {
                    val icon = when (item) {
                        TopLevelScreen.HOME -> Icons.Default.Home
                        TopLevelScreen.STATS -> Icons.Default.Star
                    }

                    Icon(icon, contentDescription = null)
                },
                label = { Text(item.name) },
                selected = item == selected,
                onClick = { onItemClick(item) },
            )
        }
    }
}

fun NavController.navigate(screen: TopLevelScreen) {
    val route = when (screen) {
        TopLevelScreen.HOME -> "home"
        TopLevelScreen.STATS -> "stats"
    }

    navigate(route, NavOptions.Builder().setLaunchSingleTop(true).build())
}

@Preview
@Composable
private fun BottomBarPreview() = SrsTheme {
    BottomBar(selected = TopLevelScreen.HOME, onItemClick = {})
}
