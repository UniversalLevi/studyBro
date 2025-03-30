package com.example.studymate.data.repository

import com.example.studymate.data.model.Priority
import com.example.studymate.data.model.RepeatType
import com.example.studymate.data.model.Subject
import com.example.studymate.data.model.Task
import com.example.studymate.data.model.TaskStatus
import com.example.studymate.data.model.TaskWithSubject
import com.example.studymate.data.source.local.dao.SubjectDao
import com.example.studymate.data.source.local.dao.TaskDao
import com.example.studymate.ui.screens.tasks.TaskItem
import com.example.studymate.ui.screens.tasks.TaskPriority
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import androidx.compose.ui.graphics.Color

/**
 * Repository for handling task-related data operations
 */
class TaskRepository(
    private val taskDao: TaskDao
) {
    
    // Get all tasks with their subjects
    fun getAllTasks(): Flow<List<TaskWithSubject>> = taskDao.getAllTasksWithSubject()
    
    // Get tasks by status (pending, completed, due)
    fun getTasksByStatus(status: TaskStatus): Flow<List<TaskWithSubject>> = 
        taskDao.getTasksByStatus(status.name)
    
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
        taskDao.updateTaskStatus(taskId, TaskStatus.COMPLETED.name)
    }
    
    // Mark a task as pending
    suspend fun markTaskAsPending(taskId: Long) {
        taskDao.updateTaskStatus(taskId, TaskStatus.PENDING.name)
    }
    
    // Mark a task as due
    suspend fun markTaskAsDue(taskId: Long) {
        taskDao.updateTaskStatus(taskId, TaskStatus.DUE.name)
    }
    
    // Get a single task with its subject by ID
    suspend fun getTaskWithSubject(taskId: Long): TaskWithSubject? {
        return taskDao.getTaskWithSubjectById(taskId)
    }
    
    // Get count of tasks by status
    fun getTaskCountByStatus(status: TaskStatus): Flow<Int> = 
        taskDao.getTaskCountByStatus(status.name)
    
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
    
    // Get all tasks as TaskItem objects for UI
    fun getAllTaskItems(): Flow<List<TaskItem>> {
        return taskDao.getTasksWithSubjects().map { taskWithSubjects ->
            taskWithSubjects.map { it.toTaskItem() }
        }
    }
    
    // Get all pending tasks as TaskItem objects for UI
    fun getPendingTaskItems(): Flow<List<TaskItem>> {
        return taskDao.getPendingTasks().map { tasks ->
            tasks.map { it.toTaskItem() }
        }
    }
    
    // Get all completed tasks as TaskItem objects for UI
    fun getCompletedTaskItems(): Flow<List<TaskItem>> {
        return taskDao.getCompletedTasks().map { tasks ->
            tasks.map { it.toTaskItem() }
        }
    }
    
    // Get a specific task as TaskItem for UI
    fun getTaskItemById(taskId: Long): Flow<TaskItem?> {
        return taskDao.getTaskWithSubject(taskId).map { it?.toTaskItem() }
    }
    
    // Insert a new task from a TaskItem
    suspend fun insertTask(taskItem: TaskItem) {
        val task = taskItem.toTask()
        taskDao.insertTask(task)
    }
    
    // Update task status (completed/pending)
    suspend fun updateTaskStatus(taskId: Long, isCompleted: Boolean) {
        val status = if (isCompleted) TaskStatus.COMPLETED else TaskStatus.PENDING
        taskDao.updateTaskStatus(taskId, status.name)
    }
    
    // Get tasks by subject
    fun getTasksBySubject(subjectId: Long): Flow<List<Task>> {
        return taskDao.getTasksBySubject(subjectId)
    }
    
    // Extension functions to convert between Task and TaskItem
    private fun Task.toTaskItem(): TaskItem {
        val subjectName = "General" // Default
        val subjectColor = Color(0xFF4285F4) // Default blue
        
        return TaskItem(
            id = this.id,
            title = this.name,
            subject = subjectName,
            subjectColor = subjectColor,
            deadline = this.deadline,
            time = this.reminderTime?.toString(),
            priority = this.priority.toTaskPriority(),
            isCompleted = this.status == TaskStatus.COMPLETED
        )
    }
    
    private fun TaskWithSubject.toTaskItem(): TaskItem {
        return TaskItem(
            id = task.id,
            title = task.name,
            subject = subject?.name ?: "General",
            subjectColor = subject?.color?.let { Color(android.graphics.Color.parseColor(it)) } 
                ?: Color(0xFF4285F4), // Default blue
            deadline = task.deadline,
            time = task.reminderTime?.toString(),
            priority = task.priority.toTaskPriority(),
            isCompleted = task.status == TaskStatus.COMPLETED
        )
    }
    
    private fun TaskItem.toTask(): Task {
        // We should attempt to find or create an appropriate subject
        // For now, we'll just go with null for simplicity
        val subjectId: Long? = null
        
        return Task(
            id = this.id,
            name = this.title,
            description = "", // Default empty description
            deadline = this.deadline ?: LocalDate.now(),
            priority = this.priority.toPriority(),
            status = if (this.isCompleted) TaskStatus.COMPLETED else TaskStatus.PENDING,
            reminderTime = this.time?.let { 
                try {
                    val parts = it.split(":")
                    if (parts.size >= 2) {
                        LocalTime.of(parts[0].toInt(), parts[1].toInt())
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
            },
            subjectId = subjectId,
            repeatType = RepeatType.NONE, // Default non-repeating
            repeatDays = emptyList()
        )
    }
    
    private fun Priority.toTaskPriority(): TaskPriority {
        return when (this) {
            Priority.HIGH -> TaskPriority.HIGH
            Priority.MEDIUM -> TaskPriority.MEDIUM
            Priority.LOW -> TaskPriority.LOW
        }
    }
    
    private fun TaskPriority.toPriority(): Priority {
        return when (this) {
            TaskPriority.HIGH -> Priority.HIGH
            TaskPriority.MEDIUM -> Priority.MEDIUM
            TaskPriority.LOW -> Priority.LOW
        }
    }
} 