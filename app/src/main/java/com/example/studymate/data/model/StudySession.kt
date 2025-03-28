package com.example.studymate.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Types of timer sessions
 */
enum class SessionType {
    STUDY, BREAK
}

/**
 * Entity representing a study session in the database
 */
@Entity(
    tableName = "study_sessions",
    foreignKeys = [
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("subjectId")
    ]
)
data class StudySession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subjectId: Long,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val durationMinutes: Int,
    val sessionType: SessionType = SessionType.STUDY
)

/**
 * Daily study time summary
 */
data class DailyStudyTime(
    val date: LocalDateTime,
    val totalMinutes: Long
)

/**
 * Weekly study time summary by subject
 */
data class WeeklySubjectTime(
    val id: Long,
    val name: String,
    val color: String,
    val createdAt: LocalDate,
    val totalMinutes: Long
) 