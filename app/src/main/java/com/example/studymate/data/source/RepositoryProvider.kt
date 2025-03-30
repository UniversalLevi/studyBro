package com.example.studymate.data.source

import android.content.Context
import com.example.studymate.StudyMateApp
import com.example.studymate.data.repository.SubjectRepository
import com.example.studymate.data.repository.TaskRepository

/**
 * Singleton provider class for repositories
 */
object RepositoryProvider {
    /**
     * Get the TaskRepository instance
     */
    fun getTaskRepository(context: Context): TaskRepository {
        return (context.applicationContext as StudyMateApp).taskRepository
    }
    
    /**
     * Get the SubjectRepository instance
     */
    fun getSubjectRepository(context: Context): SubjectRepository {
        return (context.applicationContext as StudyMateApp).subjectRepository
    }
} 