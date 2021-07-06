package io.github.rsookram.srs

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.rsookram.srs.home.HomeScreen
import io.github.rsookram.srs.stats.StatsScreen

@Composable
fun Main() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController)
        }

        composable("stats") {
            StatsScreen(navController)
        }
    }
}
