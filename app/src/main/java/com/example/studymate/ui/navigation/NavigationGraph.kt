package com.example.studymate.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.studymate.MainViewModel
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
fun NavigationGraph(
    navController: NavHostController,
    mainViewModel: MainViewModel
) {
    // Get subjects from the ViewModel
    val subjects = mainViewModel.subjects
    
    // Get tasks from ViewModel
    val tasks by mainViewModel.tasks.collectAsState()
    
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
                    mainViewModel.updateTaskStatus(taskId, completed)
                },
                onStartTimerClick = {
                    navController.navigate(NavigationItem.Timer.route)
                },
                subjects = subjects,
                onAddSubject = { mainViewModel.addSubject(it) },
                onRemoveSubject = { mainViewModel.removeSubject(it) }
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
                    mainViewModel.updateTaskStatus(taskId, completed)
                },
                onTaskDelete = { id ->
                    mainViewModel.deleteTask(id)
                }
            )
        }
        
        composable(NavigationItem.Timer.route) {
            TimerScreen(
                subjects = subjects.map { it },
                subjectIds = subjects.associateWith { 0L }
            )
        }
        
        composable(NavigationItem.Statistics.route) {
            StatisticsScreen(
                tasks = mainViewModel.tasks.collectAsState().value,
                studySessions = mainViewModel.studySessions.collectAsState().value,
                onResetStats = { mainViewModel.resetStats() }
            )
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
                    mainViewModel.updateTaskStatus(id, completed)
                },
                onTaskDelete = { id ->
                    mainViewModel.deleteTask(id)
                    navController.popBackStack()
                }
            )
        }
        
        // Add task screen
        composable("addTask") {
            AddTaskScreen(
                onBackClick = { navController.popBackStack() },
                onSaveTask = { newTask ->
                    mainViewModel.addTask(newTask)
                    navController.popBackStack()
                }
            )
        }
    }
} 