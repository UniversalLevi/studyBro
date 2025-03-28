package com.example.studymate.data.repository

import com.example.studymate.data.model.Priority
import com.example.studymate.data.model.RepeatType
import com.example.studymate.data.model.Subject
import com.example.studymate.data.model.Task
import com.example.studymate.data.model.TaskStatus
import com.example.studymate.data.model.TaskWithSubject
import com.example.studymate.data.source.local.SubjectDao
import com.example.studymate.data.source.local.TaskDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

/**
 * Repository for handling task-related data operations
 */
class TaskRepository(
    private val taskDao: TaskDao,
    private val subjectDao: SubjectDao
) {
    
    // Get all tasks with their subjects
    fun getAllTasks(): Flow<List<TaskWithSubject>> = taskDao.getAllTasksWithSubject()
    
    // Get tasks by status (pending, completed, due)
    fun getTasksByStatus(status: TaskStatus): Flow<List<TaskWithSubject>> = 
        taskDao.getTasksByStatus(status)
    
    // Get tasks for a specific date
    fun getTasksForDate(date: LocalDate): Flow<List<TaskWithSubject>> = 
        taskDao.getTasksForDate(date)
    
    // Get tasks for a specific subject
    fun getTasksForSubject(subjectId: Long): Flow<List<TaskWithSubject>> = 
        taskDao.getTasksForSubject(subjectId)
    
    // Get overdue tasks (past deadline and still pending)
    fun getOverdueTasks(): Flow<List<TaskWithSubject>> = 
        taskDao.getOverdueTasks(LocalDate.now())
    
    // Create a new task
    suspend fun createTask(
        name: String,
        description: String = "",
        deadline: LocalDate,
        priority: Priority = Priority.MEDIUM,
        repeatType: RepeatType = RepeatType.NONE,
        repeatDays: List<DayOfWeek> = emptyList(),
        subjectId: Long? = null,
        reminderTime: LocalTime? = null
    ): Long {
        val task = Task(
            name = name,
            description = description,
            deadline = deadline,
            priority = priority,
            status = TaskStatus.PENDING,
            repeatType = repeatType,
            repeatDays = repeatDays,
            subjectId = subjectId,
            reminderTime = reminderTime
        )
        return taskDao.insertTask(task)
    }
    
    // Update an existing task
    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }
    
    // Delete a task
    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }
    
    // Delete a task by ID
    suspend fun deleteTaskById(taskId: Long) {
        taskDao.deleteTaskById(taskId)
    }
    
    // Mark a task as completed
    suspend fun completeTask(taskId: Long) {
        taskDao.updateTaskStatus(taskId, TaskStatus.COMPLETED)
    }
    
    // Mark a task as pending
    suspend fun markTaskAsPending(taskId: Long) {
        taskDao.updateTaskStatus(taskId, TaskStatus.PENDING)
    }
    
    // Mark a task as due
    suspend fun markTaskAsDue(taskId: Long) {
        taskDao.updateTaskStatus(taskId, TaskStatus.DUE)
    }
    
    // Get a single task with its subject by ID
    suspend fun getTaskWithSubject(taskId: Long): TaskWithSubject? {
        return taskDao.getTaskWithSubjectById(taskId)
    }
    
    // Get count of tasks by status
    fun getTaskCountByStatus(status: TaskStatus): Flow<Int> = 
        taskDao.getTaskCountByStatus(status)
    
    // Handle recurring tasks - create next occurrence after completion
    suspend fun handleRecurringTask(task: Task) {
        if (task.repeatType == RepeatType.NONE) return
        
        val newDeadline = when (task.repeatType) {
            RepeatType.DAILY -> task.deadline.plusDays(1)
            RepeatType.WEEKLY -> task.deadline.plusWeeks(1)
            RepeatType.CUSTOM -> {
                // Find the next day from the repeatDays that comes after the current deadline
                val currentDayOfWeek = task.deadline.dayOfWeek
                val nextDays = task.repeatDays.filter { it > currentDayOfWeek }.sorted()
                
                if (nextDays.isNotEmpty()) {
                    // Get the next day in the same week
                    val daysToAdd = nextDays.first().value - currentDayOfWeek.value
                    task.deadline.plusDays(daysToAdd.toLong())
                } else {
                    // Wrap around to the next week
                    val firstDayNextWeek = task.repeatDays.minOrNull() ?: return
                    val daysToAdd = 7 - currentDayOfWeek.value + firstDayNextWeek.value
                    task.deadline.plusDays(daysToAdd.toLong())
                }
            }
            else -> return
        }
        
        // Create a new task with the updated deadline
        val newTask = task.copy(
            id = 0,
            deadline = newDeadline,
            status = TaskStatus.PENDING,
            createdAt = LocalDate.now()
        )
        
        taskDao.insertTask(newTask)
    }
} 