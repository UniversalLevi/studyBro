package com.example.studymate.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.studymate.R

/**
 * Sealed class representing navigation items in the bottom navigation bar
 */
sealed class NavigationItem(
    val route: String,
    val icon: ImageVector,
    val labelResId: Int
) {
    object Home : NavigationItem(
        route = Screen.Home.route,
        icon = Icons.Filled.Home,
        labelResId = R.string.nav_home
    )
    
    object Tasks : NavigationItem(
        route = Screen.Tasks.route,
        icon = Icons.Filled.List,
        labelResId = R.string.nav_tasks
    )
    
    object Timer : NavigationItem(
        route = Screen.Timer.route,
        icon = Icons.Filled.Timer,
        labelResId = R.string.nav_timer
    )
    
    object Statistics : NavigationItem(
        route = Screen.Statistics.route,
        icon = Icons.Outlined.BarChart,
        labelResId = R.string.nav_stats
    )
    
    object Settings : NavigationItem(
        route = Screen.Settings.route,
        icon = Icons.Filled.Settings,
        labelResId = R.string.nav_settings
    )
} 