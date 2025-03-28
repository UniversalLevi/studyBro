package com.example.studymate.util

import androidx.room.TypeConverter
import com.example.studymate.data.model.Priority
import com.example.studymate.data.model.RepeatType
import com.example.studymate.data.model.SessionType
import com.example.studymate.data.model.TaskStatus
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Type converters for Room database to handle non-primitive types
 */
class Converters {
    
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME
    
    // LocalDate converters
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.format(dateFormatter)
    }
    
    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? {
        return dateString?.let { LocalDate.parse(it, dateFormatter) }
    }
    
    // LocalDateTime converters
    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
        return dateTime?.format(dateTimeFormatter)
    }
    
    @TypeConverter
    fun toLocalDateTime(dateTimeString: String?): LocalDateTime? {
        return dateTimeString?.let { LocalDateTime.parse(it, dateTimeFormatter) }
    }
    
    // LocalTime converters
    @TypeConverter
    fun fromLocalTime(time: LocalTime?): String? {
        return time?.format(timeFormatter)
    }
    
    @TypeConverter
    fun toLocalTime(timeString: String?): LocalTime? {
        return timeString?.let { LocalTime.parse(it, timeFormatter) }
    }
    
    // Priority enum converters
    @TypeConverter
    fun fromPriority(priority: Priority): String {
        return priority.name
    }
    
    @TypeConverter
    fun toPriority(priorityString: String): Priority {
        return Priority.valueOf(priorityString)
    }
    
    // TaskStatus enum converters
    @TypeConverter
    fun fromTaskStatus(status: TaskStatus): String {
        return status.name
    }
    
    @TypeConverter
    fun toTaskStatus(statusString: String): TaskStatus {
        return TaskStatus.valueOf(statusString)
    }
    
    // RepeatType enum converters
    @TypeConverter
    fun fromRepeatType(repeatType: RepeatType): String {
        return repeatType.name
    }
    
    @TypeConverter
    fun toRepeatType(repeatTypeString: String): RepeatType {
        return RepeatType.valueOf(repeatTypeString)
    }
    
    // SessionType enum converters
    @TypeConverter
    fun fromSessionType(sessionType: SessionType): String {
        return sessionType.name
    }
    
    @TypeConverter
    fun toSessionType(sessionTypeString: String): SessionType {
        return SessionType.valueOf(sessionTypeString)
    }
    
    // List<DayOfWeek> converters
    @TypeConverter
    fun fromDayOfWeekList(days: List<DayOfWeek>?): String? {
        return days?.joinToString(",") { it.name }
    }
    
    @TypeConverter
    fun toDayOfWeekList(daysString: String?): List<DayOfWeek> {
        return daysString?.split(",")?.map { DayOfWeek.valueOf(it) } ?: emptyList()
    }
} 