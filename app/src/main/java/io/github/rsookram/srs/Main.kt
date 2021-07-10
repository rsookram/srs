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
import io.github.rsookram.srs.stats.StatsScreen

@Composable
fun Main() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController)
        }

        composable("browser") {
            BrowserScreen(navController)
        }

        composable("stats") {
            StatsScreen(navController)
        }

        composable(
            "card/{id}",
            listOf(
                navArgument("id") {
                    type = NavType.LongType
                    nullable = true
                }
            ),
        ) { backStackEntry ->
            val cardId = backStackEntry.arguments?.getLong("id")
            CardScreen(navController, cardId)
        }

        composable(
            "review/{deckId}",
            listOf(navArgument("deckId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments!!.getLong("deckId")
            ReviewScreen(navController, deckId)
        }
    }
}
