package com.example.studymate.data.repository

import com.example.studymate.data.model.DailyStudyTime
import com.example.studymate.data.model.SessionType
import com.example.studymate.data.model.StudySession
import com.example.studymate.data.model.WeeklySubjectTime
import com.example.studymate.data.source.local.StudySessionDao
import com.example.studymate.data.source.local.SubjectDao
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters

/**
 * Repository for handling study session related data operations
 */
class StudyRepository(
    private val studySessionDao: StudySessionDao,
    private val subjectDao: SubjectDao
) {
    
    // Record a new study session
    suspend fun recordStudySession(
        subjectId: Long,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        sessionType: SessionType
    ): Long {
        val durationMinutes = java.time.Duration.between(startTime, endTime).toMinutes().toInt()
        val studySession = StudySession(
            subjectId = subjectId,
            startTime = startTime,
            endTime = endTime,
            durationMinutes = durationMinutes,
            sessionType = sessionType
        )
        return studySessionDao.insertStudySession(studySession)
    }
    
    // Get all sessions for a subject
    fun getSessionsBySubject(subjectId: Long): Flow<List<StudySession>> =
        studySessionDao.getSessionsBySubject(subjectId)
    
    // Get sessions between dates
    fun getSessionsBetweenDates(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<StudySession>> =
        studySessionDao.getSessionsBetweenDates(startDate, endDate)
    
    // Get daily study times for a date range
    fun getDailyStudyTimes(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        sessionType: SessionType = SessionType.STUDY
    ): Flow<List<DailyStudyTime>> =
        studySessionDao.getDailyStudyTimes(startDate, endDate, sessionType)
    
    // Get weekly subject times
    fun getWeeklySubjectTimes(
        startDate: LocalDateTime = getStartOfCurrentWeek(),
        endDate: LocalDateTime = getEndOfCurrentWeek(),
        sessionType: SessionType = SessionType.STUDY
    ): Flow<List<WeeklySubjectTime>> =
        studySessionDao.getWeeklySubjectTimes(startDate, endDate, sessionType)
    
    // Get total study time between dates
    fun getTotalStudyTime(
        startDate: LocalDateTime = getStartOfCurrentWeek(),
        endDate: LocalDateTime = getEndOfCurrentWeek(),
        sessionType: SessionType = SessionType.STUDY
    ): Flow<Long?> =
        studySessionDao.getTotalStudyTimeBetweenDates(startDate, endDate, sessionType)
    
    // Get total study time for a subject
    fun getTotalStudyTimeForSubject(
        subjectId: Long,
        sessionType: SessionType = SessionType.STUDY
    ): Flow<Long?> =
        studySessionDao.getTotalDurationForSubject(subjectId, sessionType)
    
    // Helper function to get start of current week (Monday)
    private fun getStartOfCurrentWeek(): LocalDateTime {
        val today = LocalDate.now()
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return LocalDateTime.of(monday, LocalTime.MIN)
    }
    
    // Helper function to get end of current week (Sunday)
    private fun getEndOfCurrentWeek(): LocalDateTime {
        val today = LocalDate.now()
        val sunday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        return LocalDateTime.of(sunday, LocalTime.MAX)
    }
    
    // Helper function to get start of current month
    fun getStartOfCurrentMonth(): LocalDateTime {
        val today = LocalDate.now()
        val firstDayOfMonth = today.withDayOfMonth(1)
        return LocalDateTime.of(firstDayOfMonth, LocalTime.MIN)
    }
    
    // Helper function to get end of current month
    fun getEndOfCurrentMonth(): LocalDateTime {
        val today = LocalDate.now()
        val lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth())
        return LocalDateTime.of(lastDayOfMonth, LocalTime.MAX)
    }
}
 