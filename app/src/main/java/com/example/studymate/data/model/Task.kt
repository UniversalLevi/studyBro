package com.example.studymate.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

/**
 * Priority levels for tasks
 */
enum class Priority {
    HIGH, MEDIUM, LOW
}

/**
 * Status of a task
 */
enum class TaskStatus {
    PENDING, COMPLETED, DUE
}

/**
 * Type of repetition for a task
 */
enum class RepeatType {
    NONE, DAILY, WEEKLY, CUSTOM
}

/**
 * Entity representing a task in the database
 */
@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("subjectId")
    ]
)
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val deadline: LocalDate,
    val priority: Priority = Priority.MEDIUM,
    val status: TaskStatus = TaskStatus.PENDING,
    val repeatType: RepeatType = RepeatType.NONE,
    val repeatDays: List<DayOfWeek> = emptyList(),
    val subjectId: Long? = null,
    val reminderTime: LocalTime? = null,
    val createdAt: LocalDate = LocalDate.now()
)

/**
 * Task with its related subject information
 */
data class TaskWithSubject(
    @Embedded val task: Task,
    @Relation(
        parentColumn = "subjectId",
        entityColumn = "id"
    )
    val subject: Subject?
) 