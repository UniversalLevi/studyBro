package com.example.studymate

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.studymate.data.model.StudySession
import com.example.studymate.data.repository.SubjectRepository
import com.example.studymate.data.repository.TaskRepository
import com.example.studymate.data.source.RepositoryProvider
import com.example.studymate.ui.screens.tasks.TaskItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    // Repositories
    private val taskRepository: TaskRepository = RepositoryProvider.getTaskRepository(application)
    private val subjectRepository: SubjectRepository = RepositoryProvider.getSubjectRepository(application)
    
    // Tasks state
    private val _tasks = MutableStateFlow<List<TaskItem>>(emptyList())
    val tasks: StateFlow<List<TaskItem>> = _tasks.asStateFlow()
    
    // Subjects state
    private val _subjects = MutableStateFlow<List<String>>(emptyList())
    val subjects: StateFlow<List<String>> = _subjects.asStateFlow()
    
    // Study sessions state
    private val _studySessions = MutableStateFlow<List<StudySession>>(emptyList())
    val studySessions: StateFlow<List<StudySession>> = _studySessions.asStateFlow()
    
    // Loading states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        // Load all data when ViewModel is created
        loadTasks()
        loadSubjects()
    }
    
    // Load tasks from the database
    private fun loadTasks() {
        viewModelScope.launch {
            _isLoading.value = true
            taskRepository.getAllTaskItems()
                .catch { e: Throwable ->
                    // Handle error
                    _isLoading.value = false
                    e.printStackTrace()
                }
                .collect { taskItems ->
                    _tasks.value = taskItems
                    _isLoading.value = false
                }
        }
    }
    
    // Load subjects from the database
    private fun loadSubjects() {
        viewModelScope.launch {
            _isLoading.value = true
            subjectRepository.getSubjectNames()
                .catch { e: Throwable ->
                    // Handle error
                    _isLoading.value = false
                    e.printStackTrace()
                }
                .collect { subjectNames ->
                    _subjects.value = subjectNames
                    _isLoading.value = false
                }
        }
    }
    
    // Add a new task
    fun addTask(task: TaskItem) {
        viewModelScope.launch {
            taskRepository.insertTask(task)
            // No need to update _tasks manually as we're observing the database
        }
    }
    
    // Add a new subject
    fun addSubject(subjectName: String) {
        if (subjectName.isBlank()) return
        
        viewModelScope.launch {
            try {
                // Check if subject already exists to avoid duplicates
                if (!_subjects.value.contains(subjectName)) {
                    // Create a new subject in the database
                    subjectRepository.createSubject(subjectName)
                    // No need to update _subjects manually as we're observing the database
                }
            } catch (e: Exception) {
                // Handle the error (could log or show to user)
                e.printStackTrace()
            }
        }
    }
    
    // Remove a subject
    fun removeSubject(subjectName: String) {
        viewModelScope.launch {
            try {
                // Find and collect the subject from the database
                subjectRepository.getSubjectByName(subjectName)
                    .catch { e: Throwable ->
                        // Handle error
                        e.printStackTrace()
                    }
                    .collect { subject ->
                        // Delete the subject if found
                        if (subject != null) {
                            try {
                                subjectRepository.deleteSubject(subject)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
            } catch (e: Exception) {
                // Handle the error
                e.printStackTrace()
            }
        }
    }
    
    // Update task status (completed/pending)
    fun updateTaskStatus(taskId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            taskRepository.updateTaskStatus(taskId, isCompleted)
        }
    }
    
    // Delete a task
    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            taskRepository.deleteTaskById(taskId)
        }
    }
    
    // Reset all stats
    fun resetStats() {
        viewModelScope.launch {
            // Reset tasks to pending status
            _tasks.value.filter { it.isCompleted }.forEach { task ->
                updateTaskStatus(task.id, false)
            }
            
            // In a real app, you might want to clear study sessions too
            // This would require a studySessionRepository
        }
    }
    
    // Get pending tasks as a flow
    fun getPendingTasks(): Flow<List<TaskItem>> {
        return taskRepository.getPendingTaskItems()
    }
    
    // Get completed tasks as a flow
    fun getCompletedTasks(): Flow<List<TaskItem>> {
        return taskRepository.getCompletedTaskItems()
    }
    
    // Get a task by ID
    fun getTaskById(taskId: Long): Flow<TaskItem?> {
        return taskRepository.getTaskItemById(taskId)
    }
} 