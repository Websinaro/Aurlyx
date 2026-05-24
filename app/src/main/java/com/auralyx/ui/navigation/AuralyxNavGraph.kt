package com.auralyx.ui.navigation
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.auralyx.ui.home.HomeScreen
import com.auralyx.ui.library.LibraryScreen
import com.auralyx.ui.player.MiniPlayerBar
import com.auralyx.ui.player.PlayerScreen
import com.auralyx.ui.search.SearchScreen
import com.auralyx.ui.settings.SettingsScreen

@Composable
fun AuralyxNavGraph() {
    val nav   = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val isPlayer = route == Routes.PLAYER

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            Column {
                if (!isPlayer && route != null) MiniPlayerBar(onTap = { nav.navigate(Routes.PLAYER) })
                if (route in NavDestination.bottomNavItems.map { it.route }) AuralyxBottomNav(nav, route)
            }
        }
    ) { padding ->
        NavHost(nav, NavDestination.Home.route, Modifier.padding(padding)) {
            composable(NavDestination.Home.route, enterTransition={fadeIn(tween(220))}, exitTransition={fadeOut(tween(180))}) {
                HomeScreen(onNavigateToPlayer={nav.navigate(Routes.PLAYER)}, onNavigateToSettings={nav.navigate(Routes.SETTINGS)})
            }
            composable(NavDestination.Library.route, enterTransition={fadeIn(tween(220))}, exitTransition={fadeOut(tween(180))}) {
                LibraryScreen(onNavigateToPlayer={nav.navigate(Routes.PLAYER)})
            }
            composable(NavDestination.Search.route, enterTransition={fadeIn(tween(220))}, exitTransition={fadeOut(tween(180))}) {
                SearchScreen(onNavigateToPlayer={nav.navigate(Routes.PLAYER)})
            }
            composable(Routes.PLAYER,
                enterTransition={ slideInVertically(tween(380,easing=FastOutSlowInEasing)){it}+fadeIn(tween(280)) },
                exitTransition={ slideOutVertically(tween(300)){it}+fadeOut(tween(250)) }
            ) { PlayerScreen(onBack={nav.popBackStack()}) }
            composable(Routes.SETTINGS,
                enterTransition={ slideInHorizontally(tween(280)){it}+fadeIn(tween(220)) },
                exitTransition={ slideOutHorizontally(tween(240)){it}+fadeOut(tween(200)) }
            ) { SettingsScreen(onBack={nav.popBackStack()}) }
        }
    }
}

@Composable
private fun AuralyxBottomNav(nav: NavHostController, currentRoute: String?) {
    NavigationBar(tonalElevation=0.dp) {
        NavDestination.bottomNavItems.forEach { dest ->
            val sel = currentRoute == dest.route
            NavigationBarItem(selected=sel, onClick={
                nav.navigate(dest.route) { popUpTo(nav.graph.findStartDestination().id){saveState=true}; launchSingleTop=true; restoreState=true }
            }, icon={ Icon(if(sel)dest.selectedIcon else dest.unselectedIcon, dest.label) }, label={ Text(dest.label) })
        }
    }
}
