package com.example.studymate.ui.screens.timer

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studymate.R
import com.example.studymate.StudyMateApp
import com.example.studymate.data.model.SessionType
import com.example.studymate.receiver.NotificationReceiver
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class TimerViewModel : ViewModel() {
    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()
    
    private var timerJob: Job? = null
    private var isBreakMode = false
    private var selectedSubjectId: Long = 0
    private var startTime: LocalDateTime? = null
    
    fun setDuration(minutes: Int) {
        _timerState.value = TimerState(
            totalTimeMillis = TimeUnit.MINUTES.toMillis(minutes.toLong()),
            remainingTimeMillis = TimeUnit.MINUTES.toMillis(minutes.toLong())
        )
    }
    
    fun setSelectedSubject(subjectId: Long) {
        selectedSubjectId = subjectId
    }
    
    fun startTimer() {
        if (timerJob != null) return
        
        _timerState.value = _timerState.value.copy(isRunning = true)
        
        // Record start time when timer begins
        startTime = LocalDateTime.now()
        
        timerJob = viewModelScope.launch {
            val startTimeMillis = System.currentTimeMillis()
            val totalTime = _timerState.value.remainingTimeMillis
            
            while (_timerState.value.remainingTimeMillis > 0 && _timerState.value.isRunning) {
                delay(100) // Update every 100ms for smoother progress with animation
                
                val elapsedTime = System.currentTimeMillis() - startTimeMillis
                val remainingTime = (totalTime - elapsedTime).coerceAtLeast(0)
                
                _timerState.value = _timerState.value.copy(
                    remainingTimeMillis = remainingTime,
                    progress = (1 - remainingTime.toFloat() / totalTime).coerceIn(0f, 1f)
                )
                
                // If timer completed
                if (remainingTime <= 0) {
                    _timerState.value = _timerState.value.copy(
                        isRunning = false,
                        isCompleted = true,
                        progress = 1f
                    )
                    break
                }
            }
            
            if (_timerState.value.isCompleted) {
                onTimerComplete()
            }
            
            timerJob = null
        }
    }
    
    fun pauseTimer() {
        timerJob?.cancel()
        timerJob = null
        _timerState.value = _timerState.value.copy(isRunning = false)
    }
    
    fun resetTimer() {
        pauseTimer()
        _timerState.value = _timerState.value.copy(
            remainingTimeMillis = _timerState.value.totalTimeMillis,
            progress = 0f,
            isCompleted = false
        )
        // Reset start time when timer is reset
        startTime = null
    }
    
    fun toggleMode() {
        isBreakMode = !isBreakMode
        val defaultDuration = if (isBreakMode) 5 else 25
        setDuration(defaultDuration)
    }
    
    fun isInBreakMode(): Boolean = isBreakMode
    
    private fun onTimerComplete() {
        viewModelScope.launch {
            // Record end time when timer completes
            val endTime = LocalDateTime.now()
            
            // Update state to completed
            _timerState.value = _timerState.value.copy(
                isRunning = false,
                isCompleted = true,
                remainingTimeMillis = 0,
                progress = 1f
            )
            
            // Auto switch mode after completion
            toggleMode()
        }
    }
    
    fun showTimerCompleteNotification(context: Context) {
        try {
            // Create notification channel
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channelId = "timer_channel"
                val channel = NotificationChannel(
                    channelId,
                    "Timer Notifications",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for completed timers"
                    enableVibration(true)
                    
                    // Add sound
                    val audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                    setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), audioAttributes)
                }
                
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
            
            // Save session data to database
            startTime?.let { start ->
                val app = context.applicationContext as? StudyMateApp
                if (app != null) {
                    val sessionType = if (isBreakMode) SessionType.BREAK else SessionType.STUDY
                    val endTime = LocalDateTime.now()
                    val durationMinutes = (TimeUnit.MILLISECONDS.toMinutes(_timerState.value.totalTimeMillis)).toInt()
                    
                    viewModelScope.launch {
                        try {
                            app.studyRepository.recordStudySession(
                                subjectId = selectedSubjectId,
                                startTime = start,
                                endTime = endTime,
                                sessionType = sessionType
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            
            // Build notification with better UI elements
            val title = if (isBreakMode) "Break Time Completed!" else "Study Session Complete!"
            val message = if (isBreakMode) 
                "Time to get back to studying!" 
                else "Great job! Take a well-deserved break."
                
            val notification = NotificationCompat.Builder(context, "timer_channel")
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // Use a system icon as fallback
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()
            
            // Check for notification permission before showing
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    NotificationManagerCompat.from(context).notify(1, notification)
                }
            } else {
                NotificationManagerCompat.from(context).notify(1, notification)
            }
            
            // Vibrate device
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(500)
            }
        } catch (e: Exception) {
            // Log the error but don't crash
            e.printStackTrace()
        }
    }
}

data class TimerState(
    val totalTimeMillis: Long = TimeUnit.MINUTES.toMillis(25), // Default 25 minutes
    val remainingTimeMillis: Long = totalTimeMillis,
    val progress: Float = 0f,
    val isRunning: Boolean = false,
    val isCompleted: Boolean = false
) {
    val remainingMinutes: Int
        get() = TimeUnit.MILLISECONDS.toMinutes(remainingTimeMillis).toInt()
    
    val remainingSeconds: Int
        get() = (TimeUnit.MILLISECONDS.toSeconds(remainingTimeMillis) % 60).toInt()
} 