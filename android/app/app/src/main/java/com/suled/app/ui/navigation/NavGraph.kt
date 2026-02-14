package com.suled.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.suled.app.ui.screens.GamesListScreen
import com.suled.app.ui.screens.PairSelectionScreen
import com.suled.app.ui.screens.TournamentListScreen

object Routes {
    const val TOURNAMENT_LIST = "tournament_list"
    const val PAIR_SELECTION = "pair_selection/{tournamentId}"
    const val GAMES_LIST = "games_list/{tournamentId}/{pairId}/{pairName}"
    
    fun pairSelectionRoute(tournamentId: String) = "pair_selection/$tournamentId"
    fun gamesListRoute(tournamentId: String, pairId: String, pairName: String) = "games_list/$tournamentId/$pairId/$pairName"
}

@Composable
fun TournamentNavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Routes.TOURNAMENT_LIST
    ) {
        composable(Routes.TOURNAMENT_LIST) {
            TournamentListScreen(
                onTournamentSelected = { tournamentId ->
                    navController.navigate(Routes.pairSelectionRoute(tournamentId))
                }
            )
        }
        
        composable(
            route = Routes.PAIR_SELECTION,
            arguments = listOf(
                navArgument("tournamentId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val tournamentId = backStackEntry.arguments?.getString("tournamentId") ?: ""
            PairSelectionScreen(
                tournamentId = tournamentId,
                onPairSelected = { pairId, pairName ->
                    navController.navigate(Routes.gamesListRoute(tournamentId, pairId, pairName))
                }
            )
        }
        
        composable(
            route = Routes.GAMES_LIST,
            arguments = listOf(
                navArgument("tournamentId") { type = NavType.StringType },
                navArgument("pairId") { type = NavType.StringType },
                navArgument("pairName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val tournamentId = backStackEntry.arguments?.getString("tournamentId") ?: return@composable
            val pairId = backStackEntry.arguments?.getString("pairId") ?: return@composable
            val pairName = backStackEntry.arguments?.getString("pairName") ?: return@composable
            
            GamesListScreen(
                tournamentId = tournamentId,
                pairId = pairId,
                pairName = pairName,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
