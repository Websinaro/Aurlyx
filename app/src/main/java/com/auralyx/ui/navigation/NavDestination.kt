package com.auralyx.ui.navigation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
sealed class NavDestination(val route:String, val label:String, val selectedIcon:ImageVector, val unselectedIcon:ImageVector) {
    object Home    : NavDestination("home",    "Home",    Icons.Filled.Home,         Icons.Outlined.Home)
    object Library : NavDestination("library", "Library", Icons.Filled.LibraryMusic,  Icons.Outlined.LibraryMusic)
    object Search  : NavDestination("search",  "Search",  Icons.Filled.Search,        Icons.Outlined.Search)
    companion object { val bottomNavItems = listOf(Home, Library, Search) }
}
object Routes {
    const val PLAYER   = "player"
    const val SETTINGS = "settings"
}
