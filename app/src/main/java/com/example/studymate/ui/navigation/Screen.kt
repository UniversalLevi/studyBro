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
    object TaskDetail : Screen("task_detail")
    object AddTask : Screen("add_task")
    object AddSubject : Screen("add_subject")
} 