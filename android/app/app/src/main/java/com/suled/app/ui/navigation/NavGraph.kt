package com.suled.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.suled.app.ui.screens.GamesListScreen
import com.suled.app.ui.screens.PairSelectionScreen

object Routes {
    const val PAIR_SELECTION = "pair_selection"
    const val GAMES_LIST = "games_list/{pairId}/{pairName}"
    
    fun gamesListRoute(pairId: String, pairName: String) = "games_list/$pairId/$pairName"
}

@Composable
fun TournamentNavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Routes.PAIR_SELECTION
    ) {
        composable(Routes.PAIR_SELECTION) {
            PairSelectionScreen(
                onPairSelected = { pairId, pairName ->
                    navController.navigate(Routes.gamesListRoute(pairId, pairName))
                }
            )
        }
        
        composable(
            route = Routes.GAMES_LIST,
            arguments = listOf(
                navArgument("pairId") { type = NavType.StringType },
                navArgument("pairName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val pairId = backStackEntry.arguments?.getString("pairId") ?: return@composable
            val pairName = backStackEntry.arguments?.getString("pairName") ?: return@composable
            
            GamesListScreen(
                pairId = pairId,
                pairName = pairName,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
