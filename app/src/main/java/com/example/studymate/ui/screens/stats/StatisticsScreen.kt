package com.example.studymate.ui.screens.stats

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studymate.ui.components.SimpleBarChart
import com.example.studymate.ui.components.SimpleProgressRing
import com.example.studymate.data.model.StudySession
import com.example.studymate.ui.screens.tasks.TaskItem
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    tasks: List<TaskItem>,
    studySessions: List<StudySession>,
    onResetStats: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Study Time", "Tasks")
    var selectedPeriod by remember { mutableStateOf("This Week") }
    var showPeriodDropdown by remember { mutableStateOf(false) }
    
    // Filter data based on selected period
    val filteredData = remember(selectedPeriod, tasks, studySessions) {
        filterDataByPeriod(selectedPeriod, tasks, studySessions)
    }
    
    // Calculate dynamic stats from filtered data
    val totalStudyTime = filteredData.sessions.sumOf { it.durationMinutes.toLong() }.toInt()
    val averageDailyStudyTime = if (filteredData.sessions.isNotEmpty()) {
        val days = filteredData.dateRange.days.coerceAtLeast(1)
        totalStudyTime / days
    } else 0
    
    val completedTasks = filteredData.tasks.count { it.isCompleted }
    val totalTasks = filteredData.tasks.size
    val completionRate = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics") },
                actions = {
                    IconButton(onClick = onResetStats) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Statistics",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Tab selection with animated transitions
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }
            
            // Period selection
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Statistics Overview",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Box {
                    OutlinedButton(
                        onClick = { showPeriodDropdown = true }
                    ) {
                        Text(selectedPeriod)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Period")
                    }
                    
                    DropdownMenu(
                        expanded = showPeriodDropdown,
                        onDismissRequest = { showPeriodDropdown = false }
                    ) {
                        listOf("Today", "This Week", "This Month", "All Time").forEach { period ->
                            DropdownMenuItem(
                                text = { Text(period) },
                                onClick = {
                                    selectedPeriod = period
                                    showPeriodDropdown = false
                                }
                            )
                        }
                    }
                }
            }
            
            // Animated content switching
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                when (selectedTabIndex) {
                    0 -> SimpleStudyTimeStats(
                        totalStudyTime = totalStudyTime,
                        averageDailyStudyTime = averageDailyStudyTime,
                        studySessions = filteredData.sessions,
                        dateRange = filteredData.dateRange
                    )
                    1 -> SimpleTasksStats(
                        completedTasks = completedTasks,
                        totalTasks = totalTasks,
                        completionRate = completionRate,
                        tasks = filteredData.tasks
                    )
                }
            }
        }
    }
}

// Data filtering and processing
data class DateRange(val start: LocalDate, val end: LocalDate) {
    val days: Int get() = ChronoUnit.DAYS.between(start, end).toInt() + 1
}

private data class FilteredData(
    val tasks: List<TaskItem>,
    val sessions: List<StudySession>,
    val dateRange: DateRange
)

private fun filterDataByPeriod(
    period: String,
    tasks: List<TaskItem>,
    studySessions: List<StudySession>
): FilteredData {
    val today = LocalDate.now()
    
    val (startDate, endDate) = when (period) {
        "Today" -> Pair(today, today)
        "This Week" -> {
            val start = today.minusDays(today.dayOfWeek.value - 1L)
            Pair(start, today)
        }
        "This Month" -> {
            val start = today.withDayOfMonth(1)
            Pair(start, today)
        }
        else -> Pair(LocalDate.of(2000, 1, 1), today) // All Time
    }
    
    val dateRange = DateRange(startDate, endDate)
    
    // Filter tasks by deadline date
    val filteredTasks = tasks.filter { task ->
        task.deadline?.let { deadline ->
            (deadline.isEqual(startDate) || deadline.isAfter(startDate)) &&
            (deadline.isEqual(endDate) || deadline.isBefore(endDate))
        } ?: false
    }
    
    // Filter study sessions by date
    val filteredSessions = studySessions.filter { session ->
        val sessionDate = session.startTime.toLocalDate()
        (sessionDate.isEqual(startDate) || sessionDate.isAfter(startDate)) &&
        (sessionDate.isEqual(endDate) || sessionDate.isBefore(endDate))
    }
    
    return FilteredData(
        tasks = filteredTasks,
        sessions = filteredSessions,
        dateRange = dateRange
    )
}

// Calculate daily study times for bar chart
private fun calculateDailyStudyTimes(
    sessions: List<StudySession>,
    dateRange: DateRange
): List<Pair<LocalDate, Float>> {
    // Group sessions by date and sum durations
    val sessionsByDate = sessions.groupBy { it.startTime.toLocalDate() }
        .mapValues { (_, sessions) -> sessions.sumOf { it.durationMinutes } / 60f } // Convert to hours
    
    // Create a list for all dates in range, with 0 hours for dates with no sessions
    return (0 until dateRange.days).map { dayOffset ->
        val date = dateRange.start.plusDays(dayOffset.toLong())
        Pair(date, sessionsByDate[date] ?: 0f)
    }
}

@Composable
fun SimpleStudyTimeStats(
    totalStudyTime: Int,
    averageDailyStudyTime: Int,
    studySessions: List<StudySession>,
    dateRange: DateRange
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Summary cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SummaryCard(
                title = "Total Study Time",
                value = "${totalStudyTime / 60}h ${totalStudyTime % 60}m",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            
            SummaryCard(
                title = "Average Daily",
                value = "${averageDailyStudyTime / 60}h ${averageDailyStudyTime % 60}m",
                color = Color(0xFF34A853),
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Weekly chart with actual data
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Daily Study Hours",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Calculate daily study hours for chart
                val dailyStudyHours = calculateDailyStudyTimes(studySessions, dateRange)
                val maxValue = dailyStudyHours.maxOfOrNull { it.second }?.coerceAtLeast(1f) ?: 1f
                
                // Simplified bar chart with actual data
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    dailyStudyHours.takeLast(7).forEach { (date, hours) ->
                        SimpleBarChart(
                            value = hours,
                            maxValue = maxValue,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // Day labels with actual date info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    dailyStudyHours.takeLast(7).forEach { (date, _) ->
                        Text(
                            text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Subject breakdown with actual data
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Study Time by Subject",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (studySessions.isEmpty()) {
                    Text(
                        text = "No study sessions recorded yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                } else {
                    // In a real app, you would query subject names and times
                    // Mocked for now
                    val subjectColors = listOf(
                        Color(0xFF4285F4), Color(0xFFEA4335),
                        Color(0xFFFBBC05), Color(0xFF34A853),
                        Color(0xFF9C27B0)
                    )
                    
                    // Just displaying placeholder until real data is available
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SubjectLegendItem("Mathematics", "${totalStudyTime / 5}m", subjectColors[0])
                        SubjectLegendItem("History", "${totalStudyTime / 4}m", subjectColors[1])
                        SubjectLegendItem("Physics", "${totalStudyTime / 3}m", subjectColors[2])
                    }
                }
            }
        }
    }
}

@Composable
fun SimpleTasksStats(
    completedTasks: Int,
    totalTasks: Int,
    completionRate: Float,
    tasks: List<TaskItem>
) {
    // Calculate task statistics by priority
    val tasksByPriority = tasks.groupBy { it.priority.toString() }
    val highPriorityStats = calculatePriorityStats(tasksByPriority["HIGH"] ?: emptyList())
    val mediumPriorityStats = calculatePriorityStats(tasksByPriority["MEDIUM"] ?: emptyList())
    val lowPriorityStats = calculatePriorityStats(tasksByPriority["LOW"] ?: emptyList())
    
    // Animated completion rate
    val animatedCompletionRate by animateFloatAsState(
        targetValue = completionRate,
        label = "completion_rate_animation"
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Summary cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SummaryCard(
                title = "Tasks Completed",
                value = "$completedTasks/$totalTasks",
                color = Color(0xFF4285F4),
                modifier = Modifier.weight(1f)
            )
            
            SummaryCard(
                title = "Completion Rate",
                value = "${(completionRate * 100).toInt()}%",
                color = Color(0xFF34A853),
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Task completion chart
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Task Completion",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (totalTasks == 0) {
                    Text(
                        text = "No tasks added yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            SimpleProgressRing(
                                progress = animatedCompletionRate,
                                color = Color(0xFF4285F4)
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = "${(completionRate * 100).toInt()}%",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4285F4)
                            )
                            
                            Text(
                                text = "Overall",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Tasks by priority with real data
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Tasks by Priority",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (totalTasks == 0) {
                    Text(
                        text = "No tasks added yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PriorityBar(
                            priority = "High",
                            completed = highPriorityStats.completed,
                            total = highPriorityStats.total,
                            color = Color(0xFFEA4335)
                        )
                        
                        PriorityBar(
                            priority = "Medium",
                            completed = mediumPriorityStats.completed,
                            total = mediumPriorityStats.total,
                            color = Color(0xFFFBBC05)
                        )
                        
                        PriorityBar(
                            priority = "Low",
                            completed = lowPriorityStats.completed,
                            total = lowPriorityStats.total,
                            color = Color(0xFF34A853)
                        )
                    }
                }
            }
        }
    }
}

private data class PriorityStats(val completed: Int, val total: Int)

private fun calculatePriorityStats(tasks: List<TaskItem>): PriorityStats {
    val completed = tasks.count { it.isCompleted }
    return PriorityStats(completed, tasks.size)
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun SubjectLegendItem(
    subject: String,
    time: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        
        Text(
            text = subject,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        )
        
        Text(
            text = time,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun PriorityBar(
    priority: String,
    completed: Int,
    total: Int, // Used for displaying completion ratio and calculating progress
    color: Color
) {
    val progress = if (total > 0) completed.toFloat() / total.toFloat() else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress_animation")
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = priority,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "$completed/$total",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
} 