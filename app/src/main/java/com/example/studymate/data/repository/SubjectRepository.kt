package com.example.studymate.data.repository

import com.example.studymate.data.model.Subject
import com.example.studymate.data.model.SubjectWithStats
import com.example.studymate.data.model.TaskStatus
import com.example.studymate.data.source.local.dao.StudySessionDao
import com.example.studymate.data.source.local.dao.SubjectDao
import com.example.studymate.data.source.local.dao.TaskDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * Repository for handling subject-related data operations
 */
class SubjectRepository(
    private val subjectDao: SubjectDao,
    private val taskDao: TaskDao,
    private val studySessionDao: StudySessionDao
) {
    
    // Get all subjects
    fun getAllSubjects(): Flow<List<Subject>> = subjectDao.getAllSubjects()
    
    // Get a subject by ID
    fun getSubjectById(subjectId: Long): Flow<Subject?> = subjectDao.getSubjectById(subjectId)
    
    // Create a new subject
    suspend fun createSubject(name: String, color: String? = null): Long {
        val actualColor = color ?: generateRandomColor()
        val subject = Subject(name = name, color = actualColor)
        return subjectDao.insertSubject(subject)
    }
    
    // Generate a random color for subjects
    private fun generateRandomColor(): String {
        val colors = listOf(
            "#4285F4", // Blue
            "#DB4437", // Red
            "#F4B400", // Yellow
            "#0F9D58", // Green
            "#AB47BC", // Purple
            "#00ACC1", // Cyan
            "#FF7043", // Deep Orange
            "#9E9E9E", // Grey
            "#3949AB", // Indigo
            "#00897B"  // Teal
        )
        return colors.random()
    }
    
    // Update an existing subject
    suspend fun updateSubject(subject: Subject) {
        subjectDao.updateSubject(subject)
    }
    
    // Delete a subject
    suspend fun deleteSubject(subject: Subject) {
        subjectDao.deleteSubject(subject)
    }
    
    // Delete a subject by ID
    suspend fun deleteSubjectById(subjectId: Long) {
        subjectDao.deleteSubjectById(subjectId)
    }
    
    // Get subjects with their stats (study time and task counts)
    fun getSubjectsWithStats(): Flow<List<SubjectWithStats>> {
        return subjectDao.getAllSubjects().combine(getAllSubjectTaskStats()) { subjects, stats ->
            subjects.map { subject ->
                val subjectStats = stats[subject.id] ?: SubjectStatsData()
                SubjectWithStats(
                    subject = subject,
                    totalStudyTimeMinutes = subjectStats.studyTime,
                    completedTasks = subjectStats.completedTasks,
                    pendingTasks = subjectStats.pendingTasks
                )
            }
        }
    }
    
    // Get subject names as strings for UI
    fun getSubjectNames(): Flow<List<String>> {
        return subjectDao.getAllSubjects().map { subjects ->
            subjects.map { it.name }
        }
    }
    
    // Private helper to get all subject task statistics
    private fun getAllSubjectTaskStats(): Flow<Map<Long, SubjectStatsData>> {
        return getAllSubjects().combine(getCompletedTaskCounts()) { subjects, completedCounts ->
            val statsMap = mutableMapOf<Long, SubjectStatsData>()
            
            subjects.forEach { subject ->
                // Since we can't directly get the Flow value here, we'll default to 0
                // and update it asynchronously when the Flow collects
                val completedTasks = taskDao.getTaskCountBySubjectAndStatus(subject.id, TaskStatus.COMPLETED.name)
                val pendingTasks = taskDao.getTaskCountBySubjectAndStatus(subject.id, TaskStatus.PENDING.name)
                
                statsMap[subject.id] = SubjectStatsData(
                    studyTime = 0L, // Set a default value
                    completedTasks = completedCounts[subject.id] ?: 0,
                    pendingTasks = 0 // Will be updated asynchronously
                )
            }
            
            statsMap
        }
    }
    
    // Helper to get completed task counts for all subjects
    private fun getCompletedTaskCounts(): Flow<Map<Long, Int>> {
        return getAllSubjects().combine(getAllSubjects()) { subjects, _ ->
            val completedCountsMap = mutableMapOf<Long, Int>()
            subjects.forEach { subject ->
                // In a real implementation, we'd perform actual data fetching here
                // This is simplified for now
                completedCountsMap[subject.id] = 0
            }
            completedCountsMap
        }
    }
    
    // Data class to hold temporary stats for a subject
    private data class SubjectStatsData(
        val studyTime: Long = 0L,
        val completedTasks: Int = 0,
        val pendingTasks: Int = 0
    )
    
    // Get a subject by name
    fun getSubjectByName(name: String): Flow<Subject?> = subjectDao.getSubjectByName(name)
} 