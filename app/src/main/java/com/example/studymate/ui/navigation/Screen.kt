package com.example.studymate.ui.navigation

/**
 * Sealed class representing the different screens in the application
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Tasks : Screen("tasks")
    object Timer : Screen("timer")
    object Statistics : Screen("statistics")
    object Settings : Screen("settings")
    object Calendar : Screen("calendar")
    object Profile : Screen("profile")
    object Schedule : Screen("schedule")
    
    // These routes are used for destinations without bottom navigation
    object TaskDetail : Screen("taskDetail")  // Used as base for "taskDetail/{id}"
    object AddTask : Screen("addTask")
} 