package com.lumina.tvaggregator.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lumina.tvaggregator.data.ContentNavigation
import com.lumina.tvaggregator.data.model.Content
import com.lumina.tvaggregator.ui.screens.ContentDetailScreen
import com.lumina.tvaggregator.ui.screens.HomeScreen
import com.lumina.tvaggregator.ui.screens.KidsScreen
import com.lumina.tvaggregator.ui.screens.PlatformBrowseScreen
import com.lumina.tvaggregator.ui.screens.SearchScreen
import com.lumina.tvaggregator.viewmodel.HomeViewModel
import com.lumina.tvaggregator.viewmodel.KidsViewModel
import com.lumina.tvaggregator.viewmodel.PlatformBrowseViewModel
import com.lumina.tvaggregator.viewmodel.SearchViewModel

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

data class NavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
    val emoji: String = ""
)

val mainNavItems = listOf(
    NavItem(Screen.Home, "Accueil", Icons.Default.Home),
    NavItem(Screen.Search, "Recherche", Icons.Default.Search),
    NavItem(Screen.PlatformBrowse, "Plateformes", Icons.Default.Tv),
    NavItem(Screen.Kids, "Mode Enfant", Icons.Default.Star, "🧸")
)

// Routes principales qui affichent la sidebar
private val mainRoutes = setOf(
    Screen.Home.route,
    Screen.Search.route,
    Screen.PlatformBrowse.route,
    Screen.Kids.route,
    Screen.Settings.route
)

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
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    val showSidebar = currentRoute in mainRoutes

    Row(modifier = modifier.fillMaxSize()) {
        // Sidebar de navigation (visible sur les écrans principaux)
        if (showSidebar) {
            TVNavigationSidebar(
                currentRoute = currentRoute,
                onNavigate = { screen ->
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            // Éviter l'empilement des destinations principales
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }

        // Contenu principal
        NavHost(
            navController = navController,
            startDestination = Screen.startDestination,
            modifier = Modifier.weight(1f)
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
                            homeViewModel.openContent(selectedContent, context)
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
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                            launchSingleTop = true
                        }
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
                // Route Settings redirige vers PlatformBrowse
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
}

@Composable
private fun TVNavigationSidebar(
    currentRoute: String?,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val kidsColor = Color(0xFFFF6B35)
    val kidsSelected = currentRoute == Screen.Kids.route

    NavigationRail(
        modifier = modifier
            .fillMaxHeight()
            .background(
                if (kidsSelected)
                    Color(0xFF1A0A2E)
                else
                    MaterialTheme.colorScheme.surface
            ),
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        mainNavItems.forEach { item ->
            val selected = currentRoute == item.screen.route
            val isKids = item.screen == Screen.Kids

            NavigationRailItem(
                selected = selected,
                onClick = { onNavigate(item.screen) },
                icon = {
                    if (item.emoji.isNotEmpty()) {
                        Text(
                            text = item.emoji,
                            fontSize = 20.sp
                        )
                    } else {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label
                        )
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp,
                        color = when {
                            selected && isKids -> kidsColor
                            selected -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = if (isKids) kidsColor else MaterialTheme.colorScheme.primary,
                    selectedIndicatorColor = if (isKids)
                        kidsColor.copy(alpha = 0.2f)
                    else
                        MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}
