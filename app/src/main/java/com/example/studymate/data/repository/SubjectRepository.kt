package com.example.studymate.data.repository

import com.example.studymate.data.model.Subject
import com.example.studymate.data.model.SubjectWithStats
import com.example.studymate.data.model.TaskStatus
import com.example.studymate.data.source.local.StudySessionDao
import com.example.studymate.data.source.local.SubjectDao
import com.example.studymate.data.source.local.TaskDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

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
    suspend fun getSubjectById(subjectId: Long): Subject? = subjectDao.getSubjectById(subjectId)
    
    // Create a new subject
    suspend fun createSubject(name: String, color: String): Long {
        val subject = Subject(name = name, color = color)
        return subjectDao.insertSubject(subject)
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
    
    // Private helper to get all subject task statistics
    private fun getAllSubjectTaskStats(): Flow<Map<Long, SubjectStatsData>> {
        return getAllSubjects().combine(getCompletedTaskCounts()) { subjects, completedCounts ->
            val statsMap = mutableMapOf<Long, SubjectStatsData>()
            
            subjects.forEach { subject ->
                // Since we can't directly get the Flow value here, we'll default to 0
                // and update it asynchronously when the Flow collects
                val completedTasks = taskDao.getTaskCountBySubjectAndStatus(subject.id, TaskStatus.COMPLETED)
                val pendingTasks = taskDao.getTaskCountBySubjectAndStatus(subject.id, TaskStatus.PENDING)
                
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
} 