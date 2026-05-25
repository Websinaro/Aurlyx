package com.auralyx.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.*
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.auralyx.ui.converter.ConverterScreen
import com.auralyx.ui.home.HomeScreen
import com.auralyx.ui.library.LibraryScreen
import com.auralyx.ui.player.MiniPlayerBar
import com.auralyx.ui.player.PlayerScreen
import com.auralyx.ui.search.SearchScreen
import com.auralyx.ui.settings.SettingsScreen

@Composable
fun AuralyxNavGraph() {
    val nav    = rememberNavController()
    val entry  by nav.currentBackStackEntryAsState()
    val route  = entry?.destination?.route
    val isPlayer = route == Routes.PLAYER

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        containerColor      = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column {
                // Mini-player (hidden on full player)
                AnimatedVisibility(
                    visible = !isPlayer && route != null,
                    enter   = slideInVertically { it } + fadeIn(tween(280)),
                    exit    = slideOutVertically { it } + fadeOut(tween(200))
                ) {
                    MiniPlayerBar(onTap = { nav.navigate(Routes.PLAYER) })
                }
                // Bottom navigation
                val showBottomNav = route in NavDestination.bottomNavItems.map { it.route }
                AnimatedVisibility(
                    visible = showBottomNav,
                    enter   = slideInVertically { it } + fadeIn(tween(220)),
                    exit    = slideOutVertically { it } + fadeOut(tween(180))
                ) {
                    PremiumBottomBar(nav, route)
                }
            }
        }
    ) { padding ->
        NavHost(
            navController    = nav,
            startDestination = NavDestination.Home.route,
            modifier         = Modifier.padding(padding),
            enterTransition  = { fadeIn(tween(280)) + scaleIn(tween(300), 0.96f) },
            exitTransition   = { fadeOut(tween(220)) + scaleOut(tween(240), 0.96f) },
            popEnterTransition  = { fadeIn(tween(260)) + scaleIn(tween(280), 0.96f) },
            popExitTransition   = { fadeOut(tween(200)) + scaleOut(tween(220), 0.96f) }
        ) {
            composable(NavDestination.Home.route) {
                HomeScreen(
                    onNavigateToPlayer   = { nav.navigate(Routes.PLAYER) },
                    onNavigateToSettings = { nav.navigate(Routes.SETTINGS) }
                )
            }
            composable(NavDestination.Library.route) {
                LibraryScreen(onNavigateToPlayer = { nav.navigate(Routes.PLAYER) })
            }
            composable(NavDestination.Search.route) {
                SearchScreen(onNavigateToPlayer = { nav.navigate(Routes.PLAYER) })
            }
            composable(
                Routes.PLAYER,
                enterTransition = {
                    slideInVertically(tween(420, easing = FastOutSlowInEasing)) { it / 2 } +
                    fadeIn(tween(300))
                },
                exitTransition = {
                    slideOutVertically(tween(350, easing = FastOutSlowInEasing)) { it / 2 } +
                    fadeOut(tween(260))
                }
            ) {
                PlayerScreen(onBack = { nav.popBackStack() })
            }
            composable(
                Routes.SETTINGS,
                enterTransition = { slideInHorizontally(tween(320, easing = FastOutSlowInEasing)) { it } + fadeIn(tween(260)) },
                exitTransition  = { slideOutHorizontally(tween(280)) { it } + fadeOut(tween(220)) }
            ) {
                SettingsScreen(
                    onBack = { nav.popBackStack() },
                    onNavigateToConverter = { nav.navigate(Routes.CONVERTER) }
                )
            }
            composable(
                Routes.CONVERTER,
                enterTransition = { slideInHorizontally(tween(320, easing = FastOutSlowInEasing)) { it } + fadeIn(tween(260)) },
                exitTransition  = { slideOutHorizontally(tween(280)) { it } + fadeOut(tween(220)) }
            ) {
                ConverterScreen(onBack = { nav.popBackStack() })
            }
        }
    }
}

@Composable
private fun PremiumBottomBar(nav: NavHostController, currentRoute: String?) {
    NavigationBar(
        containerColor   = MaterialTheme.colorScheme.surfaceContainer.copy(0.95f),
        tonalElevation   = 0.dp,
        windowInsets     = NavigationBarDefaults.windowInsets
    ) {
        NavDestination.bottomNavItems.forEach { dest ->
            val selected = currentRoute == dest.route
            NavigationBarItem(
                selected  = selected,
                onClick   = {
                    nav.navigate(dest.route) {
                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState    = true
                    }
                },
                icon = {
                    AnimatedContent(selected, label = "icon${dest.label}", transitionSpec = {
                        scaleIn(tween(200)) + fadeIn(tween(200)) togetherWith
                        scaleOut(tween(160)) + fadeOut(tween(160))
                    }) { sel ->
                        Icon(if (sel) dest.selectedIcon else dest.unselectedIcon, dest.label)
                    }
                },
                label = { Text(dest.label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(0.18f),
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
