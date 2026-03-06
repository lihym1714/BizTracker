package com.biztracker

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.biztracker.feature.dashboard.DashboardScreen
import com.biztracker.feature.entries.EntriesScreen
import com.biztracker.feature.entry.EntryInputScreen
import com.biztracker.feature.settings.SettingsScreen
import com.biztracker.feature.stats.StatsScreen

enum class TopLevelDestination(
    val route: String,
    val labelRes: Int,
) {
    Dashboard(route = "dashboard", labelRes = R.string.tab_dashboard),
    Entries(route = "entries", labelRes = R.string.tab_entries),
    Stats(route = "stats", labelRes = R.string.tab_stats),
    Settings(route = "settings", labelRes = R.string.tab_settings),
}

val topLevelDestinations = TopLevelDestination.entries

object EntryInputRoute {
    const val argumentName = "type"
    const val routePattern = "entry_input?type={type}"

    fun create(type: String): String = "entry_input?type=${type.lowercase()}"
}

fun NavHostController.navigateToTopLevel(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun BizTrackerNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = TopLevelDestination.Dashboard.route,
        modifier = modifier,
    ) {
        composable(route = TopLevelDestination.Dashboard.route) {
            DashboardScreen(
                onAddIncome = {
                    navController.navigate(EntryInputRoute.create(type = "income"))
                },
                onAddExpense = {
                    navController.navigate(EntryInputRoute.create(type = "expense"))
                },
            )
        }

        composable(route = TopLevelDestination.Entries.route) {
            EntriesScreen(
                onNavigateToEntryInput = { type ->
                    navController.navigate(EntryInputRoute.create(type = type))
                },
            )
        }

        composable(
            route = EntryInputRoute.routePattern,
            arguments = listOf(
                navArgument(EntryInputRoute.argumentName) {
                    type = NavType.StringType
                    defaultValue = "expense"
                },
            ),
        ) { backStackEntry ->
            val initialType = backStackEntry.arguments
                ?.getString(EntryInputRoute.argumentName)
                ?: "expense"

            EntryInputScreen(
                initialType = initialType,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        composable(route = TopLevelDestination.Stats.route) {
            StatsScreen()
        }

        composable(route = TopLevelDestination.Settings.route) {
            SettingsScreen()
        }
    }
}
