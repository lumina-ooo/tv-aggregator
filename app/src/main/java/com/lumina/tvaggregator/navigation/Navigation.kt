package com.lumina.tvaggregator.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.lumina.tvaggregator.ui.screens.SearchScreen
import com.lumina.tvaggregator.ui.screens.ContentDetailScreen
import com.lumina.tvaggregator.ui.screens.PlatformBrowseScreen
import com.lumina.tvaggregator.viewmodel.HomeViewModel
import com.lumina.tvaggregator.viewmodel.SearchViewModel
import com.lumina.tvaggregator.viewmodel.PlatformBrowseViewModel
import com.lumina.tvaggregator.data.model.Content
import com.lumina.tvaggregator.data.ContentNavigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object PlatformBrowse : Screen("platform_browse")
    object ContentDetail : Screen("content_detail/{contentId}")
    object Settings : Screen("settings")

    companion object {
        val startDestination = Home.route

        fun createContentDetailRoute(contentId: String): String {
            return "content_detail/$contentId"
        }
    }
}

@Composable
fun TVAggregatorNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    homeViewModel: HomeViewModel = viewModel(),
    searchViewModel: SearchViewModel = viewModel(),
    platformBrowseViewModel: PlatformBrowseViewModel = viewModel()
) {
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = Screen.startDestination,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            val contentByGenre by homeViewModel.contentByGenre.collectAsState()
            val isLoading by homeViewModel.isLoading.collectAsState()
            val errorMessage by homeViewModel.errorMessage.collectAsState()

            HomeScreen(
                contentByGenre = contentByGenre,
                isLoading = isLoading,
                onContentClick = { content ->
                    ContentNavigation.setSelectedContent(content)
                    navController.navigate(Screen.createContentDetailRoute(content.id))
                },
                onRefresh = {
                    homeViewModel.refreshContent()
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
            val searchQuery by searchViewModel.searchQuery.collectAsState()
            val searchResults by searchViewModel.searchResults.collectAsState()
            val isLoading by searchViewModel.isLoading.collectAsState()
            val errorMessage by searchViewModel.errorMessage.collectAsState()

            SearchScreen(
                searchQuery = searchQuery,
                searchResults = searchResults,
                isLoading = isLoading,
                onSearchQueryChanged = { query ->
                    searchViewModel.updateSearchQuery(query)
                },
                onContentClick = { content ->
                    ContentNavigation.setSelectedContent(content)
                    navController.navigate(Screen.createContentDetailRoute(content.id))
                },
                onClearSearch = {
                    searchViewModel.clearSearch()
                }
            )

            // Handle error messages
            errorMessage?.let { message ->
                // In a real app, you would show this in a snackbar or dialog
                // For now, we'll just clear it
                searchViewModel.clearError()
            }
        }

        composable(Screen.PlatformBrowse.route) {
            val platforms by platformBrowseViewModel.platforms.collectAsState()
            val selectedPlatform by platformBrowseViewModel.selectedPlatform.collectAsState()
            val platformContent by platformBrowseViewModel.platformContent.collectAsState()
            val isLoadingContent by platformBrowseViewModel.isLoadingContent.collectAsState()
            val errorMessage by platformBrowseViewModel.errorMessage.collectAsState()

            PlatformBrowseScreen(
                platforms = platforms,
                selectedPlatform = selectedPlatform,
                platformContent = platformContent,
                isLoadingContent = isLoadingContent,
                onPlatformSelect = { platform ->
                    platformBrowseViewModel.selectPlatform(platform)
                },
                onContentClick = { content ->
                    ContentNavigation.setSelectedContent(content)
                    navController.navigate(Screen.createContentDetailRoute(content.id))
                },
                onBackToHome = {
                    platformBrowseViewModel.clearSelectedPlatform()
                }
            )

            // Handle error messages
            errorMessage?.let { message ->
                // In a real app, you would show this in a snackbar or dialog
                // For now, we'll just clear it
                platformBrowseViewModel.clearError()
            }
        }

        composable(Screen.ContentDetail.route) {
            val selectedContent = ContentNavigation.getSelectedContent()

            if (selectedContent != null) {
                ContentDetailScreen(
                    content = selectedContent,
                    onBackClick = {
                        ContentNavigation.clearSelectedContent()
                        navController.popBackStack()
                    },
                    onOfferClick = { offer ->
                        homeViewModel.openContent(selectedContent, context)
                    }
                )
            } else {
                // Handle case where content is not found - navigate back
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }

        composable(Screen.Settings.route) {
            // Use PlatformBrowse as the Settings screen for now
            val platforms by platformBrowseViewModel.platforms.collectAsState()
            val selectedPlatform by platformBrowseViewModel.selectedPlatform.collectAsState()
            val platformContent by platformBrowseViewModel.platformContent.collectAsState()
            val isLoadingContent by platformBrowseViewModel.isLoadingContent.collectAsState()
            val errorMessage by platformBrowseViewModel.errorMessage.collectAsState()

            PlatformBrowseScreen(
                platforms = platforms,
                selectedPlatform = selectedPlatform,
                platformContent = platformContent,
                isLoadingContent = isLoadingContent,
                onPlatformSelect = { platform ->
                    platformBrowseViewModel.selectPlatform(platform)
                },
                onContentClick = { content ->
                    ContentNavigation.setSelectedContent(content)
                    navController.navigate(Screen.createContentDetailRoute(content.id))
                },
                onBackToHome = {
                    platformBrowseViewModel.clearSelectedPlatform()
                }
            )

            // Handle error messages
            errorMessage?.let { message ->
                // In a real app, you would show this in a snackbar or dialog
                // For now, we'll just clear it
                platformBrowseViewModel.clearError()
            }
        }
    }
}