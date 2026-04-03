package com.lumina.tvaggregator.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lumina.tvaggregator.ui.screens.HomeScreen
import com.lumina.tvaggregator.viewmodel.HomeViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object Settings : Screen("settings")

    companion object {
        val startDestination = Home.route
    }
}

@Composable
fun TVAggregatorNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    homeViewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = Screen.startDestination,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            val platforms by homeViewModel.filteredPlatforms.collectAsState()
            val searchFilter by homeViewModel.searchFilter.collectAsState()
            val isLoading by homeViewModel.isLoading.collectAsState()
            val errorMessage by homeViewModel.errorMessage.collectAsState()

            HomeScreen(
                platforms = platforms,
                searchFilter = searchFilter,
                onSearchQueryChanged = { homeViewModel.updateSearchQuery(it) },
                onCategoryFilterChanged = { homeViewModel.updateCategoryFilter(it) },
                onInstalledFilterChanged = { homeViewModel.updateInstalledFilter(it) },
                onPlatformClick = { platform ->
                    homeViewModel.openPlatform(platform, context)
                }
            )

            // Handle error messages (could be improved with snackbar)
            errorMessage?.let { message ->
                // In a real app, you would show this in a snackbar or dialog
                // For now, we'll just clear it
                homeViewModel.clearError()
            }
        }

        composable(Screen.Search.route) {
            // Advanced search screen (could be added later)
            HomeScreen(
                platforms = emptyList(),
                searchFilter = homeViewModel.searchFilter.collectAsState().value,
                onSearchQueryChanged = { homeViewModel.updateSearchQuery(it) },
                onCategoryFilterChanged = { homeViewModel.updateCategoryFilter(it) },
                onInstalledFilterChanged = { homeViewModel.updateInstalledFilter(it) },
                onPlatformClick = { platform ->
                    homeViewModel.openPlatform(platform, context)
                }
            )
        }

        composable(Screen.Settings.route) {
            // Settings screen (could be added later for preferences)
            HomeScreen(
                platforms = emptyList(),
                searchFilter = homeViewModel.searchFilter.collectAsState().value,
                onSearchQueryChanged = { homeViewModel.updateSearchQuery(it) },
                onCategoryFilterChanged = { homeViewModel.updateCategoryFilter(it) },
                onInstalledFilterChanged = { homeViewModel.updateInstalledFilter(it) },
                onPlatformClick = { platform ->
                    homeViewModel.openPlatform(platform, context)
                }
            )
        }
    }
}