package com.example.studymate.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.studymate.data.model.DailyStudyTime
import com.example.studymate.data.model.SessionType
import com.example.studymate.data.model.StudySession
import com.example.studymate.data.model.WeeklySubjectTime
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface StudySessionDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudySession(studySession: StudySession): Long
    
    @Query("SELECT * FROM study_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): StudySession?
    
    @Query("SELECT * FROM study_sessions WHERE subjectId = :subjectId ORDER BY startTime DESC")
    fun getSessionsBySubject(subjectId: Long): Flow<List<StudySession>>
    
    @Query("SELECT * FROM study_sessions WHERE startTime BETWEEN :startDate AND :endDate ORDER BY startTime DESC")
    fun getSessionsBetweenDates(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<StudySession>>
    
    @Query("SELECT * FROM study_sessions WHERE sessionType = :sessionType ORDER BY startTime DESC")
    fun getSessionsByType(sessionType: SessionType): Flow<List<StudySession>>
    
    @Query("SELECT SUM(durationMinutes) FROM study_sessions WHERE subjectId = :subjectId AND sessionType = :sessionType")
    fun getTotalDurationForSubject(subjectId: Long, sessionType: SessionType = SessionType.STUDY): Flow<Long?>
    
    @Query("""
        SELECT date(startTime) as date, SUM(durationMinutes) as totalMinutes 
        FROM study_sessions 
        WHERE startTime BETWEEN :startDate AND :endDate AND sessionType = :sessionType
        GROUP BY date(startTime)
        ORDER BY date(startTime) ASC
    """)
    fun getDailyStudyTimes(startDate: LocalDateTime, endDate: LocalDateTime, sessionType: SessionType = SessionType.STUDY): Flow<List<DailyStudyTime>>
    
    @Transaction
    @Query("""
        SELECT s.id, s.name, s.color, s.createdAt, SUM(ss.durationMinutes) as totalMinutes
        FROM subjects s 
        JOIN study_sessions ss ON s.id = ss.subjectId
        WHERE ss.startTime BETWEEN :startDate AND :endDate AND ss.sessionType = :sessionType
        GROUP BY s.id
        ORDER BY totalMinutes DESC
    """)
    fun getWeeklySubjectTimes(startDate: LocalDateTime, endDate: LocalDateTime, sessionType: SessionType = SessionType.STUDY): Flow<List<WeeklySubjectTime>>
    
    @Query("SELECT SUM(durationMinutes) FROM study_sessions WHERE startTime BETWEEN :startDate AND :endDate AND sessionType = :sessionType")
    fun getTotalStudyTimeBetweenDates(startDate: LocalDateTime, endDate: LocalDateTime, sessionType: SessionType = SessionType.STUDY): Flow<Long?>
} 