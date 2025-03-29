package com.example.studymate.ui.screens.timer

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studymate.ui.components.SimpleProgressRing
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TimerScreen() {
    var timerDuration by remember { mutableIntStateOf(25 * 60) } // 25 minutes in seconds
    var remainingTime by remember { mutableIntStateOf(25 * 60) }
    var isRunning by remember { mutableStateOf(false) }
    var isBreakTime by remember { mutableStateOf(false) }
    var selectedSubject by remember { mutableStateOf("Mathematics") }
    var showSubjectDropdown by remember { mutableStateOf(false) }
    
    val progress = 1f - (remainingTime.toFloat() / timerDuration.toFloat())
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "TimerProgress"
    )
    
    // Timer logic
    LaunchedEffect(isRunning, remainingTime, isBreakTime) {
        if (isRunning && remainingTime > 0) {
            delay(1000L)
            remainingTime -= 1
        } else if (isRunning && remainingTime <= 0) {
            isRunning = false
            // Switch between study and break
            isBreakTime = !isBreakTime
            timerDuration = if (isBreakTime) 5 * 60 else 25 * 60 // 5 min break, 25 min study
            remainingTime = timerDuration
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mode selection
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Row(
                modifier = Modifier
                    .padding(4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (!isBreakTime) MaterialTheme.colorScheme.primary else Color.Transparent,
                    modifier = Modifier.weight(1f)
                ) {
                    TextButton(
                        onClick = {
                            if (isBreakTime) {
                                isBreakTime = false
                                timerDuration = 25 * 60
                                remainingTime = timerDuration
                                isRunning = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Study",
                            color = if (!isBreakTime) MaterialTheme.colorScheme.onPrimary 
                                else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isBreakTime) MaterialTheme.colorScheme.primary else Color.Transparent,
                    modifier = Modifier.weight(1f)
                ) {
                    TextButton(
                        onClick = {
                            if (!isBreakTime) {
                                isBreakTime = true
                                timerDuration = 5 * 60
                                remainingTime = timerDuration
                                isRunning = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Break",
                            color = if (isBreakTime) MaterialTheme.colorScheme.onPrimary 
                                else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Subject selection
        if (!isBreakTime) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Book,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        
                        Text(
                            text = "Subject: $selectedSubject",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp)
                        )
                        
                        Button(
                            onClick = { showSubjectDropdown = true },
                            enabled = !isRunning
                        ) {
                            Text("Change")
                        }
                    }
                    
                    DropdownMenu(
                        expanded = showSubjectDropdown && !isRunning,
                        onDismissRequest = { showSubjectDropdown = false }
                    ) {
                        listOf("Mathematics", "History", "Physics", "Economics", "English").forEach { subject ->
                            DropdownMenuItem(
                                text = { Text(subject) },
                                onClick = {
                                    selectedSubject = subject
                                    showSubjectDropdown = false
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        // Timer display
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(280.dp)
                .padding(16.dp)
        ) {
            // Timer background
            SimpleProgressRing(
                progress = animatedProgress,
                color = if (isBreakTime) Color(0xFF34A853) else MaterialTheme.colorScheme.primary,
                strokeWidth = 24f
            )
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = formatTime(remainingTime),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Text(
                    text = if (isBreakTime) "Break Time" else "Study Time",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Controls
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Reset button
            IconButton(
                onClick = {
                    isRunning = false
                    remainingTime = timerDuration
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Timer",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            // Play/Pause button
            IconButton(
                onClick = { isRunning = !isRunning },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isBreakTime) Color(0xFF34A853) else MaterialTheme.colorScheme.primary)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "Pause Timer" else "Start Timer",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            // Spacer for symmetry
            Box(
                modifier = Modifier.size(64.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Time adjustment slider
        if (!isRunning) {
            Text(
                text = "Adjust Time: ${timerDuration / 60} minutes",
                style = MaterialTheme.typography.titleMedium
            )
            
            Slider(
                value = timerDuration.toFloat(),
                onValueChange = {
                    timerDuration = it.toInt()
                    remainingTime = timerDuration
                },
                valueRange = if (isBreakTime) 1f * 60f..15f * 60f else 1f * 60f..60f * 60f,
                steps = if (isBreakTime) 14 else 59,
                colors = SliderDefaults.colors(
                    thumbColor = if (isBreakTime) Color(0xFF34A853) else MaterialTheme.colorScheme.primary,
                    activeTrackColor = if (isBreakTime) Color(0xFF34A853) else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

fun formatTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
} 