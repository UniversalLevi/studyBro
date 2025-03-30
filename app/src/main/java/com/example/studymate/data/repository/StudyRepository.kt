package com.example.studymate.data.repository

import com.example.studymate.data.model.SessionType
import com.example.studymate.data.model.StudySession
import com.example.studymate.data.source.local.dao.StudySessionDao
import com.example.studymate.data.source.local.dao.SubjectDao
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Repository for handling study session related operations
 */
class StudyRepository(
    private val studySessionDao: StudySessionDao,
    private val subjectDao: SubjectDao
) {
    // Get all study sessions
    fun getAllSessions(): Flow<List<StudySession>> = studySessionDao.getAllSessions()
    
    // Get sessions for a specific subject
    fun getSessionsBySubject(subjectId: Long): Flow<List<StudySession>> = 
        studySessionDao.getSessionsBySubject(subjectId)
    
    // Get sessions for a specific date
    fun getSessionsByDate(date: LocalDateTime): Flow<List<StudySession>> =
        studySessionDao.getSessionsByDate(date)
    
    // Get sessions between dates
    fun getSessionsBetweenDates(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<StudySession>> =
        studySessionDao.getSessionsBetweenDates(startDate, endDate)
    
    // Get total study time for a subject
    fun getTotalStudyTimeForSubject(subjectId: Long): Flow<Long?> =
        studySessionDao.getTotalStudyTimeForSubject(subjectId)
    
    // Get daily study time summaries
    fun getDailyStudyTimeSummary() = studySessionDao.getDailyStudyTimeSummary()
    
    // Get weekly subject time summaries
    fun getWeeklySubjectTimeSummary(startDate: LocalDateTime, endDate: LocalDateTime) =
        studySessionDao.getWeeklySubjectTimeSummary(startDate, endDate)
    
    // Record a new study session
    suspend fun recordStudySession(
        subjectId: Long,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        sessionType: SessionType = SessionType.STUDY
    ): Long {
        val durationMinutes = java.time.Duration.between(startTime, endTime).toMinutes().toInt()
        
        val session = StudySession(
            subjectId = subjectId,
            startTime = startTime,
            endTime = endTime,
            durationMinutes = durationMinutes,
            sessionType = sessionType
        )
        
        return studySessionDao.insertSession(session)
    }
    
    // Delete a session
    suspend fun deleteSession(session: StudySession) {
        studySessionDao.deleteSession(session)
    }
    
    // Delete a session by ID
    suspend fun deleteSessionById(sessionId: Long) {
        studySessionDao.deleteSessionById(sessionId)
    }
}
 