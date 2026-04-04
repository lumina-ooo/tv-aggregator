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
import com.lumina.tvaggregator.ui.screens.KidsScreen
import com.lumina.tvaggregator.viewmodel.HomeViewModel
import com.lumina.tvaggregator.viewmodel.SearchViewModel
import com.lumina.tvaggregator.viewmodel.PlatformBrowseViewModel
import com.lumina.tvaggregator.viewmodel.KidsViewModel
import com.lumina.tvaggregator.data.model.Content
import com.lumina.tvaggregator.data.ContentNavigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object PlatformBrowse : Screen("platform_browse")
    object ContentDetail : Screen("content_detail/{contentId}")
    object Kids : Screen("kids")
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
    platformBrowseViewModel: PlatformBrowseViewModel = viewModel(),
    kidsViewModel: KidsViewModel = viewModel()
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
                },
                onKidsClick = {
                    navController.navigate(Screen.Kids.route)
                },
                onPlatformsClick = {
                    navController.navigate(Screen.PlatformBrowse.route)
                },
                onSearchClick = {
                    navController.navigate(Screen.Search.route)
                }
            )

            errorMessage?.let {
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

            errorMessage?.let {
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

            errorMessage?.let {
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
                        if (offer.webUrl != null) {
                            val uri = android.net.Uri.parse(offer.webUrl)
                            // Map technicalName to Android package for native app launch
                            val appPackage = when (offer.packageName) {
                                "rtbf" -> "be.rtbf.auvio"
                                "rtlplay" -> "be.rtl.rtlplay"
                                "tf1" -> "fr.tf1.mytf1"
                                "arte" -> "tv.arte.plus7"
                                "amazonprimevideo", "amazon" -> "com.amazon.avod.thirdpartyclient"
                                "amazonfrancetv", "amazontfoumax" -> "com.amazon.avod.thirdpartyclient"
                                "netflix" -> "com.netflix.mediaclient"
                                "play" -> "com.google.android.videos"
                                "disneyplus" -> "com.disney.disneyplus"
                                "max" -> "com.hbo.hbonow"
                                "itunes" -> "com.apple.atve.androidtv.appletv"
                                "wuaki" -> "tv.wuaki.apptv"
                                "crunchyroll" -> "com.crunchyroll.crunchyroid"
                                "molotovtv" -> "tv.molotov.app"
                                "sixplay" -> "fr.m6.m6replay"
                                "plutotv" -> "tv.pluto.android"
                                else -> null
                            }
                            var opened = false
                            // Try opening deep link in native app
                            if (appPackage != null) {
                                try {
                                    val appIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                                    appIntent.setPackage(appPackage)
                                    appIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(appIntent)
                                    opened = true
                                } catch (_: Exception) {}
                            }
                            // Fallback: open URL (system may still resolve to app)
                            if (!opened) {
                                try {
                                    val webIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                                    webIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(webIntent)
                                } catch (_: Exception) {}
                            }
                        }
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }

        composable(Screen.Kids.route) {
            val kidsContent by kidsViewModel.kidsContent.collectAsState()
            val isLoading by kidsViewModel.isLoading.collectAsState()
            val errorMessage by kidsViewModel.errorMessage.collectAsState()

            KidsScreen(
                kidsContent = kidsContent,
                isLoading = isLoading,
                onContentClick = { content ->
                    ContentNavigation.setSelectedContent(content)
                    navController.navigate(Screen.createContentDetailRoute(content.id))
                },
                onBackClick = {
                    navController.popBackStack()
                },
                onRefresh = {
                    kidsViewModel.loadKidsContent()
                }
            )

            errorMessage?.let {
                kidsViewModel.clearError()
            }
        }

        composable(Screen.Settings.route) {
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

            errorMessage?.let {
                platformBrowseViewModel.clearError()
            }
        }
    }
}
