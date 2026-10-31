package com.idlix.tv.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.idlix.tv.ui.components.NavItem
import com.idlix.tv.ui.components.TvNavDrawer
import com.idlix.tv.ui.screens.detail.DetailScreen
import com.idlix.tv.ui.screens.history.ContinueWatchingScreen
import com.idlix.tv.ui.screens.home.HomeScreen
import com.idlix.tv.ui.screens.movies.MoviesScreen
import com.idlix.tv.ui.screens.player.PlayerScreen
import com.idlix.tv.ui.screens.search.SearchScreen
import com.idlix.tv.ui.screens.series.SeriesScreen
import com.idlix.tv.ui.screens.settings.SettingsScreen
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    val selectedNavItem = when {
        currentRoute.startsWith("search") -> NavItem.SEARCH
        currentRoute.startsWith("continue_watching") -> NavItem.CONTINUE_WATCHING
        currentRoute.startsWith("movies") -> NavItem.MOVIES
        currentRoute.startsWith("series") -> NavItem.SERIES
        else -> NavItem.HOME
    }

    val isFullScreenRoute = currentRoute.startsWith("player") || currentRoute.startsWith("detail")

    if (isFullScreenRoute) {
        // Render without side drawer
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = modifier
        ) {
            setupNavRoutes(navController)
        }
    } else {
        // Render inside Collapsible TvNavDrawer
        TvNavDrawer(
            selectedItem = selectedNavItem,
            onItemSelected = { item ->
                when (item) {
                    NavItem.HOME -> navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                        launchSingleTop = true
                    }
                    NavItem.SEARCH -> navController.navigate(Screen.Search.route) {
                        popUpTo(Screen.Home.route)
                        launchSingleTop = true
                        restoreState = true
                    }
                    NavItem.CONTINUE_WATCHING -> navController.navigate(Screen.ContinueWatching.route) {
                        popUpTo(Screen.Home.route)
                        launchSingleTop = true
                        restoreState = true
                    }
                    NavItem.MOVIES -> navController.navigate(Screen.Movies.route) {
                        popUpTo(Screen.Home.route)
                        launchSingleTop = true
                        restoreState = true
                    }
                    NavItem.SERIES -> navController.navigate(Screen.Series.route) {
                        popUpTo(Screen.Home.route)
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            onOpenSettings = {
                navController.navigate(Screen.Settings.route) {
                    launchSingleTop = true
                }
            },
            modifier = modifier
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route
            ) {
                setupNavRoutes(navController)
            }
        }
    }
}

private fun androidx.navigation.NavGraphBuilder.setupNavRoutes(
    navController: androidx.navigation.NavHostController
) {
    composable(Screen.Home.route) {
        HomeScreen(
            onNavigateToDetail = { type, slug ->
                navController.navigate(Screen.Detail.createRoute(type, slug))
            },
            onNavigateToPlayer = { type, id, title, poster, backdrop, slug ->
                navController.navigate(Screen.Player.createRoute(type, id, title, poster, backdrop, slug))
            }
        )
    }

    composable(Screen.ContinueWatching.route) {
        ContinueWatchingScreen(
            onNavigateToPlayer = { type, id, title, poster, backdrop, slug, season, episode ->
                navController.navigate(Screen.Player.createRoute(type, id, title, poster, backdrop, slug, season, episode))
            },
            onNavigateToHome = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                }
            }
        )
    }

    composable(Screen.Movies.route) {
        MoviesScreen(
            onNavigateToDetail = { type, slug ->
                navController.navigate(Screen.Detail.createRoute(type, slug))
            },
            onNavigateToPlayer = { type, id, title, poster, backdrop, slug ->
                navController.navigate(Screen.Player.createRoute(type, id, title, poster, backdrop, slug))
            }
        )
    }

    composable(Screen.Series.route) {
        SeriesScreen(
            onNavigateToDetail = { type, slug ->
                navController.navigate(Screen.Detail.createRoute(type, slug))
            },
            onNavigateToPlayer = { type, id, title, poster, backdrop, slug ->
                navController.navigate(Screen.Player.createRoute(type, id, title, poster, backdrop, slug))
            }
        )
    }

    composable(Screen.Search.route) {
        SearchScreen(
            onNavigateToDetail = { type, slug ->
                navController.navigate(Screen.Detail.createRoute(type, slug))
            }
        )
    }

    composable(Screen.Settings.route) {
        SettingsScreen()
    }

    composable(
        route = Screen.Detail.route,
        arguments = listOf(
            navArgument("contentType") { type = NavType.StringType },
            navArgument("slug") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val contentType = backStackEntry.arguments?.getString("contentType") ?: "movie"
        val slug = backStackEntry.arguments?.getString("slug") ?: ""

        DetailScreen(
            contentType = contentType,
            slug = slug,
            onNavigateToPlayer = { type, id, title, poster, backdrop, slugParam, season, episode ->
                navController.navigate(Screen.Player.createRoute(type, id, title, poster, backdrop, slugParam, season, episode))
            }
        )
    }

    composable(
        route = Screen.Player.route,
        arguments = listOf(
            navArgument("contentType") { type = NavType.StringType },
            navArgument("contentId") { type = NavType.StringType },
            navArgument("title") { type = NavType.StringType },
            navArgument("poster") {
                type = NavType.StringType
                nullable = true
                defaultValue = ""
            },
            navArgument("backdrop") {
                type = NavType.StringType
                nullable = true
                defaultValue = ""
            },
            navArgument("slug") {
                type = NavType.StringType
                nullable = true
                defaultValue = ""
            },
            navArgument("season") {
                type = NavType.StringType
                nullable = true
                defaultValue = ""
            },
            navArgument("episode") {
                type = NavType.StringType
                nullable = true
                defaultValue = ""
            }
        )
    ) { backStackEntry ->
        val contentType = backStackEntry.arguments?.getString("contentType") ?: "movie"
        val contentId = backStackEntry.arguments?.getString("contentId") ?: ""
        val rawTitle = backStackEntry.arguments?.getString("title") ?: ""
        val title = try {
            URLDecoder.decode(rawTitle, StandardCharsets.UTF_8.toString())
        } catch (e: Exception) {
            rawTitle
        }
        val rawPoster = backStackEntry.arguments?.getString("poster") ?: ""
        val poster = try {
            URLDecoder.decode(rawPoster, StandardCharsets.UTF_8.toString()).takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            rawPoster.takeIf { it.isNotBlank() }
        }
        val rawBackdrop = backStackEntry.arguments?.getString("backdrop") ?: ""
        val backdrop = try {
            URLDecoder.decode(rawBackdrop, StandardCharsets.UTF_8.toString()).takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            rawBackdrop.takeIf { it.isNotBlank() }
        }
        val rawSlug = backStackEntry.arguments?.getString("slug") ?: ""
        val slug = try {
            URLDecoder.decode(rawSlug, StandardCharsets.UTF_8.toString()).takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            rawSlug.takeIf { it.isNotBlank() }
        }
        val season = backStackEntry.arguments?.getString("season")?.toIntOrNull()
        val episode = backStackEntry.arguments?.getString("episode")?.toIntOrNull()

        PlayerScreen(
            contentType = contentType,
            contentId = contentId,
            title = title,
            posterUrl = poster,
            backdropUrl = backdrop,
            slug = slug,
            seasonNumber = season,
            episodeNumber = episode,
            onBack = { navController.popBackStack() }
        )
    }
}
