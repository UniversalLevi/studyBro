package com.example.studymate.ui.screens.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class TaskItem(
    val id: Long,
    val title: String,
    val subject: String,
    val subjectColor: Color,
    val deadline: LocalDate?,
    val time: String? = null,
    val priority: TaskPriority,
    val isCompleted: Boolean
)

enum class TaskPriority {
    HIGH, MEDIUM, LOW
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    tasks: List<TaskItem>,
    navigateToTaskDetail: (Long) -> Unit, 
    navigateToAddTask: () -> Unit,
    onTaskStatusChange: (Long, Boolean) -> Unit,
    onTaskDelete: (Long) -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("All", "Pending", "Completed")
    
    // Filter tasks based on tab selection
    val today = LocalDate.now()
    
    // Update tasks for each tab after any status change
    val allTasks = tasks
    val pendingTasks = tasks.filter { !it.isCompleted && (it.deadline == null || it.deadline >= today) }
    val completedTasks = tasks.filter { it.isCompleted }
    
    // Choose which task list to display based on selected tab
    val displayedTasks = when (selectedTabIndex) {
        0 -> allTasks
        1 -> pendingTasks
        2 -> completedTasks
        else -> allTasks
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tasks") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = navigateToAddTask,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }
            
            TaskList(
                tasks = displayedTasks,
                onTaskStatusChange = { taskId, completed ->
                    // Call the actual status change function
                    onTaskStatusChange(taskId, completed)
                },
                onTaskClick = navigateToTaskDetail,
                onTaskDelete = onTaskDelete
            )
        }
    }
}

@Composable
fun TaskList(
    tasks: List<TaskItem>,
    onTaskStatusChange: (Long, Boolean) -> Unit,
    onTaskClick: (Long) -> Unit,
    onTaskDelete: (Long) -> Unit
) {
    if (tasks.isEmpty()) {
        EmptyTasksMessage()
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tasks) { task ->
                TaskListItem(
                    task = task,
                    onStatusChange = { isCompleted -> onTaskStatusChange(task.id, isCompleted) },
                    onTaskClick = { onTaskClick(task.id) },
                    onDeleteClick = { onTaskDelete(task.id) }
                )
            }
        }
    }
}

@Composable
fun EmptyTasksMessage() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No Tasks Found",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Tap the + button to add a new task",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

@Composable
fun TaskListItem(
    task: TaskItem,
    onStatusChange: (Boolean) -> Unit,
    onTaskClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isCompleted = task.isCompleted
    val textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
    val textAlpha = if (isCompleted) 0.6f else 1.0f
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTaskClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onStatusChange(it) }
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Subject color indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(task.subjectColor)
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    textDecoration = textDecoration,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = textAlpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = task.subject,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f * textAlpha),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Surface(
                        color = when (task.priority) {
                            TaskPriority.HIGH -> Color(0xFFEA4335)
                            TaskPriority.MEDIUM -> Color(0xFFFBBC05)
                            TaskPriority.LOW -> Color(0xFF34A853)
                        }.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = task.priority.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = when (task.priority) {
                                TaskPriority.HIGH -> Color(0xFFEA4335)
                                TaskPriority.MEDIUM -> Color(0xFFFBBC05)
                                TaskPriority.LOW -> Color(0xFF34A853)
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Add time display if available
                    if (task.time != null) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f * textAlpha)
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = task.time,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f * textAlpha)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    
                    Text(
                        text = formatDeadline(task.deadline),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f * textAlpha)
                    )
                }
                
                // Show undo or delete button based on task status
                if (isCompleted) {
                    IconButton(
                        onClick = { onStatusChange(false) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Undo,
                            contentDescription = "Undo Task Completion",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Task",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatDeadline(date: LocalDate?): String {
    val today = LocalDate.now()
    return when {
        date == null -> "No Deadline"
        date.isEqual(today) -> "Today"
        date.isEqual(today.plusDays(1)) -> "Tomorrow"
        date.isBefore(today.plusDays(7)) -> date.format(DateTimeFormatter.ofPattern("E"))
        else -> date.format(DateTimeFormatter.ofPattern("MMM d"))
    }
} 