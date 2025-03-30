package com.example.studymate.ui.screens.timer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class TimerViewModel : ViewModel() {
    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()
    
    private var timerJob: Job? = null
    
    fun setDuration(minutes: Int) {
        _timerState.value = TimerState(
            totalTimeMillis = TimeUnit.MINUTES.toMillis(minutes.toLong()),
            remainingTimeMillis = TimeUnit.MINUTES.toMillis(minutes.toLong())
        )
    }
    
    fun startTimer() {
        if (timerJob != null) return
        
        _timerState.value = _timerState.value.copy(isRunning = true)
        
        timerJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            val totalTime = _timerState.value.remainingTimeMillis
            
            while (_timerState.value.remainingTimeMillis > 0 && _timerState.value.isRunning) {
                delay(100) // Update every 100ms for smoother progress
                
                val elapsedTime = System.currentTimeMillis() - startTime
                val remainingTime = (totalTime - elapsedTime).coerceAtLeast(0)
                
                _timerState.value = _timerState.value.copy(
                    remainingTimeMillis = remainingTime,
                    progress = (1 - remainingTime.toFloat() / totalTime).coerceIn(0f, 1f)
                )
                
                // If timer completed
                if (remainingTime <= 0) {
                    _timerState.value = _timerState.value.copy(
                        isRunning = false,
                        isCompleted = true
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
    }
    
    private fun onTimerComplete() {
        // Will be called by the UI to show notifications and vibrate
    }
    
    fun showTimerCompleteNotification(context: Context) {
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
        
        // Build notification
        val notification = NotificationCompat.Builder(context, "timer_channel")
            .setContentTitle("Study Timer Complete!")
            .setContentText("Great job! Your study session is complete.")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        
        // Show notification
        NotificationManagerCompat.from(context).notify(1, notification)
        
        // Vibrate device
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
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