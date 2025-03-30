package com.example.studymate.data.source.local

import androidx.room.TypeConverter
import com.example.studymate.data.model.Priority
import com.example.studymate.data.model.RepeatType
import com.example.studymate.data.model.SessionType
import com.example.studymate.data.model.TaskStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Room type converters for date/time and enum types
 */
class Converters {
    private val gson = Gson()
    
    // LocalDate converters
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.toString()
    }
    
    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? {
        return dateString?.let { LocalDate.parse(it) }
    }
    
    // LocalTime converters
    @TypeConverter
    fun fromLocalTime(time: LocalTime?): String? {
        return time?.format(DateTimeFormatter.ISO_LOCAL_TIME)
    }
    
    @TypeConverter
    fun toLocalTime(timeString: String?): LocalTime? {
        return timeString?.let { LocalTime.parse(it) }
    }
    
    // LocalDateTime converters
    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
        return dateTime?.toString()
    }
    
    @TypeConverter
    fun toLocalDateTime(dateTimeString: String?): LocalDateTime? {
        return dateTimeString?.let { LocalDateTime.parse(it) }
    }
    
    // Priority enum converters
    @TypeConverter
    fun fromPriority(priority: Priority): String {
        return priority.name
    }
    
    @TypeConverter
    fun toPriority(value: String): Priority {
        return Priority.valueOf(value)
    }
    
    // TaskStatus enum converters
    @TypeConverter
    fun fromTaskStatus(status: TaskStatus): String {
        return status.name
    }
    
    @TypeConverter
    fun toTaskStatus(value: String): TaskStatus {
        return TaskStatus.valueOf(value)
    }
    
    // RepeatType enum converters
    @TypeConverter
    fun fromRepeatType(repeatType: RepeatType): String {
        return repeatType.name
    }
    
    @TypeConverter
    fun toRepeatType(value: String): RepeatType {
        return RepeatType.valueOf(value)
    }
    
    // SessionType enum converters
    @TypeConverter
    fun fromSessionType(sessionType: SessionType): String {
        return sessionType.name
    }
    
    @TypeConverter
    fun toSessionType(value: String): SessionType {
        return SessionType.valueOf(value)
    }
    
    // List<DayOfWeek> converters
    @TypeConverter
    fun fromDayOfWeekList(days: List<DayOfWeek>): String {
        return gson.toJson(days.map { it.name })
    }
    
    @TypeConverter
    fun toDayOfWeekList(value: String): List<DayOfWeek> {
        val listType = object : TypeToken<List<String>>() {}.type
        val dayNames: List<String> = gson.fromJson(value, listType)
        return dayNames.map { DayOfWeek.valueOf(it) }
    }
} 