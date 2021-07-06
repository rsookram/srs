package io.github.rsookram.srs.ui

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import io.github.rsookram.srs.ui.theme.SrsTheme

enum class TopLevelScreen {
    HOME, BROWSER, STATS,
}

@Composable
fun BottomBar(selected: TopLevelScreen, onItemClick: (TopLevelScreen) -> Unit) {
    BottomNavigation {
        val screens = TopLevelScreen.values()

        screens.forEach { item ->
            BottomNavigationItem(
                icon = {
                    val icon = when (item) {
                        TopLevelScreen.HOME -> Icons.Default.Home
                        TopLevelScreen.BROWSER -> Icons.Default.Search
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
        TopLevelScreen.BROWSER -> "browser"
        TopLevelScreen.STATS -> "stats"
    }

    navigate(route, NavOptions.Builder().setLaunchSingleTop(true).build())
}

@Preview
@Composable
private fun BottomBarPreview() = SrsTheme {
    BottomBar(selected = TopLevelScreen.HOME, onItemClick = {})
}
