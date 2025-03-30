package com.example.studymate.ui.screens.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: Long,
    tasks: List<TaskItem>,
    onBackClick: () -> Unit,
    onEditTask: (Long) -> Unit,
    onTaskStatusChange: (Long, Boolean) -> Unit,
    onTaskDelete: (Long) -> Unit
) {
    val task = tasks.find { it.id == taskId }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEditTask(taskId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Task")
                    }
                    IconButton(onClick = { 
                        onTaskDelete(taskId)
                        onBackClick()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Task")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (task == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Task not found")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TaskHeader(task)
                TaskStatusToggle(
                    task = task,
                    onTaskStatusChange = { isCompleted -> 
                        onTaskStatusChange(taskId, isCompleted)
                    }
                )
                
                Divider()
                
                TaskDetailSection(title = "Subject", content = task.subject)
                TaskDetailSection(title = "Priority", content = task.priority.name)
                TaskDetailSection(
                    title = "Deadline", 
                    content = task.deadline?.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")) ?: "No deadline"
                )
                
                // Display time if available
                if (task.time != null) {
                    TaskDetailSection(
                        title = "Time",
                        content = task.time
                    )
                }
                
                Divider()
                
                // Additional sections can be added here (notes, description, etc.)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        onTaskDelete(taskId)
                        onBackClick()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete Task")
                }
            }
        }
    }
}

@Composable
fun TaskHeader(task: TaskItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(task.subjectColor)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Surface(
                color = when (task.priority) {
                    TaskPriority.HIGH -> MaterialTheme.colorScheme.errorContainer
                    TaskPriority.MEDIUM -> MaterialTheme.colorScheme.tertiaryContainer
                    TaskPriority.LOW -> MaterialTheme.colorScheme.primaryContainer
                },
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.align(Alignment.Start)
            ) {
                Text(
                    text = task.priority.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = when (task.priority) {
                        TaskPriority.HIGH -> MaterialTheme.colorScheme.onErrorContainer
                        TaskPriority.MEDIUM -> MaterialTheme.colorScheme.onTertiaryContainer
                        TaskPriority.LOW -> MaterialTheme.colorScheme.onPrimaryContainer
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun TaskStatusToggle(
    task: TaskItem,
    onTaskStatusChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = task.isCompleted,
            onCheckedChange = onTaskStatusChange
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = if (task.isCompleted) "Completed" else "Mark as completed",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun TaskDetailSection(title: String, content: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = content,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
} 