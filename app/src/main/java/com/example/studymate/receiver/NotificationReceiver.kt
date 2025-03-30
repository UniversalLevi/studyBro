package com.example.studymate.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
        when (intent.action) {
            ACTION_TIMER_FINISHED -> handleTimerFinished(context, intent)
            ACTION_TASK_REMINDER -> handleTaskReminder(context, intent)
        }
    }
    
    private fun handleTimerFinished(context: Context, intent: Intent) {
        // Extract data from intent
        val sessionTypeOrdinal = intent.getIntExtra(EXTRA_SESSION_TYPE, SessionType.STUDY.ordinal)
        val sessionType = SessionType.values()[sessionTypeOrdinal]
        val subjectId = intent.getLongExtra(EXTRA_SUBJECT_ID, 0)
        
        // Get start and end times
        val startTimeStr = intent.getStringExtra(EXTRA_START_TIME) ?: return
        val endTimeStr = intent.getStringExtra(EXTRA_END_TIME) ?: return
        
        val startTime = LocalDateTime.parse(startTimeStr)
        val endTime = LocalDateTime.parse(endTimeStr)
        
        // Save the study session
        val studyMateApp = context.applicationContext as StudyMateApp
        val studyRepository = studyMateApp.studyRepository
        
        CoroutineScope(Dispatchers.IO).launch {
            studyRepository.recordStudySession(
                subjectId = subjectId,
                startTime = startTime,
                endTime = endTime,
                sessionType = sessionType
            )
        }
    }
    
    private fun handleTaskReminder(context: Context, intent: Intent) {
        // Extract task ID from intent
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, 0)
        
        // Get task info and create a notification
        val studyMateApp = context.applicationContext as StudyMateApp
        val taskRepository = studyMateApp.taskRepository
        
        CoroutineScope(Dispatchers.IO).launch {
            val task = taskRepository.getTaskWithSubject(taskId)
            task?.let {
                if (it.task.deadline.isBefore(LocalDate.now())) {
                    // Create and show notification for overdue task
                    val notification = NotificationCompat.Builder(context, "task_channel")
                        .setContentTitle("Task Overdue: " + it.task.name)
                        .setContentText("The deadline for this task has passed.")
                        .setSmallIcon(android.R.drawable.ic_dialog_alert)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .build()
                    
                    NotificationManagerCompat.from(context).notify(taskId.toInt(), notification)
                }
            }
        }
    }
    
    companion object {
        const val ACTION_TIMER_FINISHED = "com.example.studymate.action.TIMER_FINISHED"
        const val ACTION_TASK_REMINDER = "com.example.studymate.action.TASK_REMINDER"
        
        const val EXTRA_SESSION_TYPE = "com.example.studymate.extra.SESSION_TYPE"
        const val EXTRA_SUBJECT_ID = "com.example.studymate.extra.SUBJECT_ID"
        const val EXTRA_START_TIME = "com.example.studymate.extra.START_TIME"
        const val EXTRA_END_TIME = "com.example.studymate.extra.END_TIME"
        const val EXTRA_TASK_ID = "com.example.studymate.extra.TASK_ID"
    }
} 