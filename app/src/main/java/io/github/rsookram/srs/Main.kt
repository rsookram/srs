package io.github.rsookram.srs

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import io.github.rsookram.srs.browser.BrowserScreen
import io.github.rsookram.srs.card.CardScreen
import io.github.rsookram.srs.home.HomeScreen
import io.github.rsookram.srs.review.ReviewScreen

@Composable
fun Main() {
    val navController = rememberNavController()

    // Arguments are retrieved through SavedStateHandle
    NavHost(navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }

        composable("browser") { BrowserScreen(navController) }

        composable("card") { CardScreen(navController) }

        composable(
            "card/{id}",
            listOf(navArgument("id") { type = NavType.LongType }),
        ) { CardScreen(navController) }

        composable(
            "review/{deckId}",
            listOf(navArgument("deckId") { type = NavType.LongType }),
        ) { ReviewScreen(navController) }
    }
}
