package com.example.studymate.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Entity representing a subject in the database
 */
@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val color: String = "#4285F4", // Default blue color
    val createdAt: LocalDate = LocalDate.now()
)

/**
 * Subject with stats about study time and tasks
 */
data class SubjectWithStats(
    val subject: Subject,
    val totalStudyTimeMinutes: Long = 0,
    val completedTasks: Int = 0,
    val pendingTasks: Int = 0
) 