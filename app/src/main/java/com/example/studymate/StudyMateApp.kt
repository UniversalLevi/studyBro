package com.example.studymate

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.Configuration
import androidx.work.WorkManager
import com.example.studymate.data.repository.StudyRepository
import com.example.studymate.data.repository.TaskRepository
import com.example.studymate.data.source.local.StudyMateDatabase

class StudyMateApp : Application(), Configuration.Provider {
    
    // Database instance
    val database by lazy { StudyMateDatabase.getDatabase(this) }
    
    // Repositories
    val taskRepository by lazy { TaskRepository(database.taskDao(), database.subjectDao()) }
    val studyRepository by lazy { StudyRepository(database.studySessionDao(), database.subjectDao()) }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Timer notification channel
            val timerChannel = NotificationChannel(
                TIMER_CHANNEL_ID,
                "Study Timer",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for study and break timers"
            }
            
            // Task reminder notification channel
            val reminderChannel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                "Task Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for scheduled tasks"
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannels(listOf(timerChannel, reminderChannel))
        }
    }
    
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
    }
    
    companion object {
        const val TIMER_CHANNEL_ID = "study_timer_channel"
        const val REMINDER_CHANNEL_ID = "task_reminder_channel"
    }
} 