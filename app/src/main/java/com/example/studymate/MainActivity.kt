package com.example.studymate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.studymate.ui.navigation.NavigationGraph
import com.example.studymate.ui.navigation.NavigationItem
import com.example.studymate.ui.navigation.Screen
import com.example.studymate.ui.screens.home.HomeScreen
import com.example.studymate.ui.screens.settings.SettingsScreen
import com.example.studymate.ui.screens.stats.StatisticsScreen
import com.example.studymate.ui.screens.tasks.TaskItem
import com.example.studymate.ui.screens.timer.TimerScreen
import com.example.studymate.ui.theme.StudyMateTheme
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.studymate.data.model.StudySession
import com.example.studymate.data.model.SessionType
import com.example.studymate.ui.screens.tasks.TaskPriority
import java.time.LocalDate
import java.time.LocalDateTime

class MainViewModel : ViewModel() {
    // Subjects
    private val _subjects = mutableListOf<String>()
    val subjects: List<String> get() = _subjects.toList()
    
    // Tasks
    private val _tasks = MutableStateFlow<List<TaskItem>>(emptyList())
    val tasks: StateFlow<List<TaskItem>> = _tasks.asStateFlow()
    
    // Study sessions
    private val _studySessions = MutableStateFlow<List<StudySession>>(emptyList())
    val studySessions: StateFlow<List<StudySession>> = _studySessions.asStateFlow()
    
    // Add a new subject
    fun addSubject(subject: String) {
        if (!_subjects.contains(subject)) {
            _subjects.add(subject)
        }
    }
    
    // Remove a subject
    fun removeSubject(subject: String) {
        _subjects.remove(subject)
    }
    
    // Add a new task
    fun addTask(task: TaskItem) {
        _tasks.value = _tasks.value + task
    }
    
    // Update a task status
    fun updateTaskStatus(taskId: Long, completed: Boolean) {
        _tasks.value = _tasks.value.map { task ->
            if (task.id == taskId) {
                task.copy(isCompleted = completed)
            } else {
                task
            }
        }
    }
    
    // Delete a task
    fun deleteTask(taskId: Long) {
        _tasks.value = _tasks.value.filter { it.id != taskId }
    }
    
    // Add a study session
    fun addStudySession(session: StudySession) {
        _studySessions.value = _studySessions.value + session
    }
    
    // Reset statistics
    fun resetStats() {
        _studySessions.value = emptyList()
        _tasks.value = _tasks.value.map { 
            it.copy(isCompleted = false) 
        }
    }
    
    // Initialize with sample data for testing
    init {
        // Add initial subjects
        _subjects.addAll(listOf("Mathematics", "History", "Physics", "Economics", "English"))
        
        // Add sample tasks
        val sampleTasks = listOf(
            TaskItem(
                id = 1,
                title = "Complete Math Assignment",
                subject = "Mathematics",
                subjectColor = androidx.compose.ui.graphics.Color(0xFF4285F4),
                deadline = LocalDate.now().plusDays(1),
                time = "14:30",
                priority = TaskPriority.HIGH,
                isCompleted = false
            ),
            TaskItem(
                id = 2,
                title = "Read History Chapter 5",
                subject = "History",
                subjectColor = androidx.compose.ui.graphics.Color(0xFFEA4335),
                deadline = LocalDate.now().plusDays(3),
                time = "16:00",
                priority = TaskPriority.MEDIUM,
                isCompleted = false
            ),
            TaskItem(
                id = 3,
                title = "Prepare Physics Lab Report",
                subject = "Physics",
                subjectColor = androidx.compose.ui.graphics.Color(0xFFFBBC05),
                deadline = LocalDate.now().plusDays(2),
                time = "10:15",
                priority = TaskPriority.HIGH,
                isCompleted = false
            ),
            TaskItem(
                id = 4,
                title = "Complete Literature Essay",
                subject = "English",
                subjectColor = androidx.compose.ui.graphics.Color(0xFF34A853),
                deadline = LocalDate.now().minusDays(1),
                time = "09:00",
                priority = TaskPriority.LOW,
                isCompleted = true
            )
        )
        _tasks.value = sampleTasks
        
        // Add some sample study sessions for the past week
        val today = LocalDateTime.now()
        val sampleSessions = listOf(
            StudySession(
                id = 1L,
                subjectId = 1L,
                startTime = today.minusDays(6).withHour(10),
                endTime = today.minusDays(6).withHour(11).withMinute(30),
                durationMinutes = 90,
                sessionType = SessionType.STUDY
            ),
            StudySession(
                id = 2L,
                subjectId = 2L,
                startTime = today.minusDays(5).withHour(14),
                endTime = today.minusDays(5).withHour(15),
                durationMinutes = 60,
                sessionType = SessionType.STUDY
            ),
            StudySession(
                id = 3L,
                subjectId = 1L,
                startTime = today.minusDays(3).withHour(9),
                endTime = today.minusDays(3).withHour(10),
                durationMinutes = 60,
                sessionType = SessionType.STUDY
            ),
            StudySession(
                id = 4L,
                subjectId = 3L,
                startTime = today.minusDays(1).withHour(16),
                endTime = today.minusDays(1).withHour(18),
                durationMinutes = 120,
                sessionType = SessionType.STUDY
            )
        )
        _studySessions.value = sampleSessions
    }
}

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            StudyMateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun MainScreen(mainViewModel: MainViewModel = viewModel()) {
    val navController = rememberNavController()
    val navItems = listOf(
        NavigationItem.Home,
        NavigationItem.Tasks,
        NavigationItem.Timer,
        NavigationItem.Statistics,
        NavigationItem.Settings
    )
    
    var bottomBarVisible by rememberSaveable { mutableStateOf(true) }
    
    // Track current route for hiding bottom bar on detail screens
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Hide bottom bar on detail screens
    bottomBarVisible = when {
        currentRoute == null -> true
        currentRoute.startsWith("taskDetail/") -> false
        currentRoute == "addTask" -> false
        else -> true
    }
    
    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = bottomBarVisible,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp,
                ) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp // Remove elevation which can cause shadow/dark areas
                    ) {
                        val currentDestination = navBackStackEntry?.destination
                        
                        navItems.forEach { item ->
                            NavigationBarItem(
                                icon = { Icon(item.icon, contentDescription = null) },
                                label = { Text(stringResource(item.labelResId)) },
                                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        },
        contentColor = MaterialTheme.colorScheme.onBackground,
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            NavigationGraph(
                navController = navController,
                mainViewModel = mainViewModel
            )
        }
    }
}