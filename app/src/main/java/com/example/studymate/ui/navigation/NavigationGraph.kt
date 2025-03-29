package com.example.studymate.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.studymate.ui.screens.home.HomeScreen
import com.example.studymate.ui.screens.settings.SettingsScreen
import com.example.studymate.ui.screens.stats.StatisticsScreen
import com.example.studymate.ui.screens.tasks.AddTaskScreen
import com.example.studymate.ui.screens.tasks.TaskDetailScreen
import com.example.studymate.ui.screens.tasks.TaskItem
import com.example.studymate.ui.screens.tasks.TaskPriority
import com.example.studymate.ui.screens.tasks.TasksScreen
import com.example.studymate.ui.screens.timer.TimerScreen
import java.time.LocalDate

@Composable
fun NavigationGraph(navController: NavHostController) {
    // Sample tasks data
    var tasks by remember { 
        mutableStateOf(
            listOf(
                TaskItem(
                    id = 1,
                    title = "Complete Math Assignment",
                    subject = "Mathematics",
                    subjectColor = androidx.compose.ui.graphics.Color(0xFF4285F4),
                    deadline = LocalDate.now().plusDays(1),
                    priority = TaskPriority.HIGH,
                    isCompleted = false
                ),
                TaskItem(
                    id = 2,
                    title = "Read History Chapter 5",
                    subject = "History",
                    subjectColor = androidx.compose.ui.graphics.Color(0xFFEA4335),
                    deadline = LocalDate.now().plusDays(3),
                    priority = TaskPriority.MEDIUM,
                    isCompleted = false
                ),
                TaskItem(
                    id = 3,
                    title = "Prepare Physics Lab Report",
                    subject = "Physics",
                    subjectColor = androidx.compose.ui.graphics.Color(0xFFFBBC05),
                    deadline = LocalDate.now().plusDays(2),
                    priority = TaskPriority.HIGH,
                    isCompleted = false
                ),
                TaskItem(
                    id = 4,
                    title = "Complete Literature Essay",
                    subject = "English",
                    subjectColor = androidx.compose.ui.graphics.Color(0xFF34A853),
                    deadline = LocalDate.now().minusDays(1),
                    priority = TaskPriority.LOW,
                    isCompleted = true
                )
            )
        )
    }
    
    NavHost(
        navController = navController,
        startDestination = NavigationItem.Home.route
    ) {
        composable(NavigationItem.Home.route) {
            HomeScreen(
                tasks = tasks,
                onTaskClick = { taskId ->
                    navController.navigate("taskDetail/$taskId")
                },
                onAddTaskClick = {
                    navController.navigate("addTask")
                },
                onTaskStatusChange = { taskId, completed ->
                    tasks = tasks.map { task ->
                        if (task.id == taskId) {
                            task.copy(isCompleted = completed)
                        } else {
                            task
                        }
                    }
                },
                onStartTimerClick = {
                    navController.navigate(NavigationItem.Timer.route)
                }
            )
        }
        
        composable(NavigationItem.Tasks.route) {
            TasksScreen(
                tasks = tasks,
                navigateToTaskDetail = { taskId ->
                    navController.navigate("taskDetail/$taskId")
                },
                navigateToAddTask = {
                    navController.navigate("addTask")
                },
                onTaskStatusChange = { taskId, completed ->
                    tasks = tasks.map { task ->
                        if (task.id == taskId) {
                            task.copy(isCompleted = completed)
                        } else {
                            task
                        }
                    }
                },
                onTaskDelete = { id ->
                    tasks = tasks.filter { it.id != id }
                }
            )
        }
        
        composable(NavigationItem.Timer.route) {
            TimerScreen()
        }
        
        composable(NavigationItem.Statistics.route) {
            StatisticsScreen()
        }
        
        composable(NavigationItem.Settings.route) {
            SettingsScreen()
        }
        
        // Task detail screen with task ID argument
        composable(
            route = "taskDetail/{taskId}",
            arguments = listOf(
                navArgument("taskId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getLong("taskId") ?: 0L
            TaskDetailScreen(
                taskId = taskId,
                tasks = tasks,
                onBackClick = { navController.popBackStack() },
                onEditTask = { /* TODO: Add edit functionality */ },
                onTaskStatusChange = { id, completed ->
                    tasks = tasks.map { task ->
                        if (task.id == id) {
                            task.copy(isCompleted = completed)
                        } else {
                            task
                        }
                    }
                },
                onTaskDelete = { id ->
                    tasks = tasks.filter { it.id != id }
                }
            )
        }
        
        // Add task screen
        composable("addTask") {
            AddTaskScreen(
                onBackClick = { navController.popBackStack() },
                onSaveTask = { newTask ->
                    tasks = tasks + newTask
                }
            )
        }
    }
} 