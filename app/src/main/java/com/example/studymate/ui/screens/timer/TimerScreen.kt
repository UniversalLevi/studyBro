package com.example.studymate.ui.screens.timer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.studymate.ui.components.SimpleProgressRing
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    viewModel: TimerViewModel = viewModel(),
    subjects: List<String> = emptyList(),
    onSubjectSelected: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Get timer state from ViewModel
    val timerState by viewModel.timerState.collectAsState()
    val isBreakTime = viewModel.isInBreakMode()
    
    // For setting up notification permissions
    val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
    
    var shouldRequestPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && timerState.isCompleted) {
            viewModel.showTimerCompleteNotification(context)
        }
    }
    
    // Check for timer completion and show notification
    LaunchedEffect(timerState.isCompleted) {
        if (timerState.isCompleted) {
            // Give UI time to update before showing notification
            delay(300)
            if (hasNotificationPermission) {
                try {
                    viewModel.showTimerCompleteNotification(context)
                } catch (e: Exception) {
                    // Prevent app crash if notification fails
                    e.printStackTrace()
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                shouldRequestPermission = true
            }
        }
    }
    
    // Request notification permission if needed
    LaunchedEffect(shouldRequestPermission) {
        if (shouldRequestPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            shouldRequestPermission = false
        }
    }
    
    // Monitor lifecycle to keep timer running
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Resume UI updates, but don't restart the timer
            }
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    var selectedSubject by remember { mutableStateOf(if (subjects.isNotEmpty()) subjects.first() else "") }
    var showSubjectDropdown by remember { mutableStateOf(false) }
    
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
                                viewModel.toggleMode()
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
                                viewModel.toggleMode()
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
        
        // Subject selection (only during study mode)
        if (!isBreakTime && subjects.isNotEmpty()) {
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
                            enabled = !timerState.isRunning
                        ) {
                            Text("Change")
                        }
                    }
                    
                    DropdownMenu(
                        expanded = showSubjectDropdown && !timerState.isRunning,
                        onDismissRequest = { showSubjectDropdown = false }
                    ) {
                        subjects.forEach { subject ->
                            DropdownMenuItem(
                                text = { Text(subject) },
                                onClick = {
                                    selectedSubject = subject
                                    onSubjectSelected(subject)
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
            // Timer background with animated progress
            val animatedProgress by animateFloatAsState(
                targetValue = timerState.progress,
                label = "TimerProgress"
            )
            
            CircularProgressIndicator(
                progress = animatedProgress,
                modifier = Modifier.size(280.dp),
                strokeWidth = 16.dp,
                color = if (isBreakTime) Color(0xFF34A853) else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = String.format(
                        "%02d:%02d",
                        timerState.remainingMinutes,
                        timerState.remainingSeconds
                    ),
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
                    viewModel.resetTimer()
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
                onClick = { 
                    if (timerState.isRunning) {
                        viewModel.pauseTimer()
                    } else {
                        viewModel.startTimer()
                    }
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isBreakTime) Color(0xFF34A853) else MaterialTheme.colorScheme.primary)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = if (timerState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (timerState.isRunning) "Pause Timer" else "Start Timer",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            // Empty spacer for symmetry
            Box(
                modifier = Modifier.size(64.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Time adjustment slider (only when not running)
        if (!timerState.isRunning) {
            // Calculate minutes from milliseconds
            val durationMinutes = TimeUnit.MILLISECONDS.toMinutes(timerState.totalTimeMillis).toInt()
            var sliderPosition by remember { mutableStateOf(durationMinutes.toFloat()) }
            
            // Update slider position when duration changes
            LaunchedEffect(durationMinutes) {
                sliderPosition = durationMinutes.toFloat()
            }
            
            Text(
                text = "Adjust Time: ${sliderPosition.toInt()} minutes",
                style = MaterialTheme.typography.titleMedium
            )
            
            Slider(
                value = sliderPosition,
                onValueChange = { sliderPosition = it },
                valueRange = if (isBreakTime) 1f..15f else 1f..60f,
                steps = if (isBreakTime) 14 else 59,
                colors = SliderDefaults.colors(
                    thumbColor = if (isBreakTime) Color(0xFF34A853) else MaterialTheme.colorScheme.primary,
                    activeTrackColor = if (isBreakTime) Color(0xFF34A853) else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(horizontal = 16.dp),
                onValueChangeFinished = {
                    viewModel.setDuration(sliderPosition.toInt())
                }
            )
        }
    }
}

fun formatTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
} 