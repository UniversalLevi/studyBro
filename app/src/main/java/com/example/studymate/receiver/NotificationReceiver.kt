package com.example.studymate.receiver

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.studymate.MainActivity
import com.example.studymate.StudyMateApp
import com.example.studymate.data.model.SessionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalDate

/**
 * Broadcast receiver to handle notifications like timer completion
 */
class NotificationReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        // Ensure notification channels are created
        createNotificationChannels(context)
        
        when (intent.action) {
            ACTION_TIMER_FINISHED -> handleTimerFinished(context, intent)
            ACTION_TASK_REMINDER -> handleTaskReminder(context, intent)
        }
    }
    
    private fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Timer channel
            val timerChannelId = "timer_channel"
            var channel = NotificationChannel(
                timerChannelId,
                "Timer Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for timer events"
                enableVibration(true)
                
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), audioAttributes)
            }
            notificationManager.createNotificationChannel(channel)
            
            // Tasks channel
            val taskChannelId = "task_channel"
            channel = NotificationChannel(
                taskChannelId,
                "Task Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for task reminders and deadlines"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun handleTimerFinished(context: Context, intent: Intent) {
        // Extract data from intent
        val sessionTypeOrdinal = intent.getIntExtra(EXTRA_SESSION_TYPE, SessionType.STUDY.ordinal)
        val sessionType = SessionType.values()[sessionTypeOrdinal]
        val subjectId = intent.getLongExtra(EXTRA_SUBJECT_ID, 0)
        val durationMinutes = intent.getIntExtra(EXTRA_DURATION_MINUTES, 0)
        
        // Get start and end times
        val startTimeStr = intent.getStringExtra(EXTRA_START_TIME) ?: return
        val endTimeStr = intent.getStringExtra(EXTRA_END_TIME) ?: return
        
        val startTime = LocalDateTime.parse(startTimeStr)
        val endTime = LocalDateTime.parse(endTimeStr)
        
        // Save the study session
        val studyMateApp = context.applicationContext as StudyMateApp
        val studyRepository = studyMateApp.studyRepository
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                studyRepository.recordStudySession(
                    subjectId = subjectId,
                    startTime = startTime,
                    endTime = endTime,
                    sessionType = sessionType
                )
                
                // After saving, show notification
                val title = if (sessionType == SessionType.BREAK) "Break Complete!" else "Study Session Complete!"
                val text = if (sessionType == SessionType.BREAK) 
                    "Time to get back to studying!" 
                else "Great job! You studied for $durationMinutes minutes."
                
                // Create intent for opening the app
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                
                val pendingIntent = PendingIntent.getActivity(
                    context, 0, intent, 
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                
                val notification = NotificationCompat.Builder(context, "timer_channel")
                    .setContentTitle(title)
                    .setContentText(text)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build()
                
                showNotification(context, 1001, notification)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun handleTaskReminder(context: Context, intent: Intent) {
        // Extract task ID from intent
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, 0)
        
        // Get task info and create a notification
        val studyMateApp = context.applicationContext as StudyMateApp
        val taskRepository = studyMateApp.taskRepository
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val task = taskRepository.getTaskWithSubject(taskId)
                task?.let {
                    if (it.task.deadline.isBefore(LocalDate.now())) {
                        // Create intent for opening the app
                        val appIntent = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        
                        val pendingIntent = PendingIntent.getActivity(
                            context, 0, appIntent, 
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                        )
                        
                        // Create and show notification for overdue task
                        val notification = NotificationCompat.Builder(context, "task_channel")
                            .setContentTitle("Task Overdue: " + it.task.name)
                            .setContentText("The deadline for this task has passed.")
                            .setSmallIcon(android.R.drawable.ic_dialog_alert)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)
                            .build()
                        
                        showNotification(context, taskId.toInt(), notification)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun showNotification(context: Context, notificationId: Int, notification: android.app.Notification) {
        // Check for permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return // Can't show notification without permission
            }
        }
        
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
    
    companion object {
        const val ACTION_TIMER_FINISHED = "com.example.studymate.action.TIMER_FINISHED"
        const val ACTION_TASK_REMINDER = "com.example.studymate.action.TASK_REMINDER"
        
        const val EXTRA_SESSION_TYPE = "com.example.studymate.extra.SESSION_TYPE"
        const val EXTRA_SUBJECT_ID = "com.example.studymate.extra.SUBJECT_ID"
        const val EXTRA_START_TIME = "com.example.studymate.extra.START_TIME"
        const val EXTRA_END_TIME = "com.example.studymate.extra.END_TIME"
        const val EXTRA_DURATION_MINUTES = "com.example.studymate.extra.DURATION_MINUTES"
        const val EXTRA_TASK_ID = "com.example.studymate.extra.TASK_ID"
    }
} 