package com.example.studymate.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studymate.data.model.Priority
import com.example.studymate.data.model.TaskStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(navigateToTask: (Long) -> Unit) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navigateToTask(-1) }, // -1 indicates new task
                modifier = Modifier.padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Task")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            WelcomeSection()
            Spacer(modifier = Modifier.height(24.dp))
            SummarySection()
            Spacer(modifier = Modifier.height(24.dp))
            TodayTasksSection(navigateToTask)
            Spacer(modifier = Modifier.height(24.dp))
            RecentStudySessions()
        }
    }
}

@Composable
fun WelcomeSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Welcome back!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Let's be productive today",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun SummarySection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Weekly Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(
                    title = "Study Time",
                    value = "8.5h",
                    icon = Icons.Filled.Timer
                )
                
                SummaryItem(
                    title = "Tasks Done",
                    value = "12",
                    icon = Icons.Filled.CheckCircle
                )
            }
        }
    }
}

@Composable
fun SummaryItem(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun TodayTasksSection(navigateToTask: (Long) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Today's Tasks",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Sample tasks
        TaskItem(
            id = 1L,
            name = "Math Assignment",
            subject = "Mathematics",
            subjectColor = "#4285F4",
            deadline = LocalDate.now(),
            priority = Priority.HIGH,
            status = TaskStatus.PENDING,
            navigateToTask = navigateToTask
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        TaskItem(
            id = 2L,
            name = "Read Chapter 5",
            subject = "History",
            subjectColor = "#EA4335",
            deadline = LocalDate.now(),
            priority = Priority.MEDIUM,
            status = TaskStatus.PENDING,
            navigateToTask = navigateToTask
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        TaskItem(
            id = 3L,
            name = "Prepare Presentation",
            subject = "Economics",
            subjectColor = "#34A853",
            deadline = LocalDate.now(),
            priority = Priority.LOW,
            status = TaskStatus.COMPLETED,
            navigateToTask = navigateToTask
        )
    }
}

@Composable
fun TaskItem(
    id: Long,
    name: String,
    subject: String,
    subjectColor: String,
    deadline: LocalDate,
    priority: Priority,
    status: TaskStatus,
    navigateToTask: (Long) -> Unit
) {
    val priorityColor = when (priority) {
        Priority.HIGH -> Color(0xFFEA4335)
        Priority.MEDIUM -> Color(0xFFFBBC05)
        Priority.LOW -> Color(0xFF34A853)
    }
    
    val completedAlpha = if (status == TaskStatus.COMPLETED) 0.6f else 1f
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navigateToTask(id) },
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
            // Subject color indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color(android.graphics.Color.parseColor(subjectColor)))
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = completedAlpha)
                )
                
                Text(
                    text = subject,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f * completedAlpha)
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(priorityColor.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = priority.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = priorityColor
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = deadline.format(DateTimeFormatter.ofPattern("MMM dd")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f * completedAlpha)
                )
            }
        }
    }
}

@Composable
fun RecentStudySessions() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Recent Study Sessions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Sample study sessions
        StudySessionItem(
            subject = "Mathematics",
            subjectColor = "#4285F4",
            duration = "1h 30m",
            date = "Today, 10:30 AM"
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        StudySessionItem(
            subject = "History",
            subjectColor = "#EA4335",
            duration = "45m",
            date = "Today, 08:15 AM"
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        StudySessionItem(
            subject = "Economics",
            subjectColor = "#34A853",
            duration = "2h 15m",
            date = "Yesterday, 4:20 PM"
        )
    }
}

@Composable
fun StudySessionItem(
    subject: String,
    subjectColor: String,
    duration: String,
    date: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            // Subject color indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(android.graphics.Color.parseColor(subjectColor))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Timer,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = subject,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Text(
                text = duration,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
} 