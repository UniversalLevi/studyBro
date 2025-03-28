package com.example.studymate.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.studymate.StudyMateApp
import com.example.studymate.data.model.TaskStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Broadcast receiver to handle device reboot and reschedule tasks
 */
class BootCompletedReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            rescheduleTaskReminders(context)
        }
    }
    
    private fun rescheduleTaskReminders(context: Context) {
        val studyMateApp = context.applicationContext as StudyMateApp
        val taskRepository = studyMateApp.taskRepository
        
        CoroutineScope(Dispatchers.IO).launch {
            // Get all pending tasks with reminders
            val tasks = taskRepository.getTasksByStatus(TaskStatus.PENDING).first()
            
            // Current date
            val today = LocalDate.now()
            
            // Filter tasks with reminders that are due today or in the future
            val tasksToReschedule = tasks.filter { 
                it.task.reminderTime != null && 
                (it.task.deadline.isEqual(today) || it.task.deadline.isAfter(today))
            }
            
            // Reschedule each task reminder
            tasksToReschedule.forEach { taskWithSubject ->
                // In a real implementation, you would use AlarmManager to schedule the reminders
                // This is just a placeholder
            }
        }
    }
} 