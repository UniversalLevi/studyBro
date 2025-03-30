package com.example.studymate.data.source.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.studymate.data.model.DailyStudyTime
import com.example.studymate.data.model.StudySession
import com.example.studymate.data.model.WeeklySubjectTime
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface StudySessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySession): Long
    
    @Delete
    suspend fun deleteSession(session: StudySession)
    
    @Query("DELETE FROM study_sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: Long)
    
    @Query("SELECT * FROM study_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<StudySession>>
    
    @Query("SELECT * FROM study_sessions WHERE subjectId = :subjectId ORDER BY startTime DESC")
    fun getSessionsBySubject(subjectId: Long): Flow<List<StudySession>>
    
    @Query("SELECT * FROM study_sessions WHERE DATE(startTime) = DATE(:date) ORDER BY startTime DESC")
    fun getSessionsByDate(date: LocalDateTime): Flow<List<StudySession>>
    
    @Query("SELECT * FROM study_sessions WHERE DATE(startTime) BETWEEN DATE(:startDate) AND DATE(:endDate) ORDER BY startTime DESC")
    fun getSessionsBetweenDates(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<StudySession>>
    
    // Get total study time for a subject
    @Query("SELECT SUM(durationMinutes) FROM study_sessions WHERE subjectId = :subjectId")
    fun getTotalStudyTimeForSubject(subjectId: Long): Flow<Long?>
    
    // Get daily study time summaries
    @Query("""
        SELECT DATE(startTime) as date, SUM(durationMinutes) as totalMinutes 
        FROM study_sessions 
        GROUP BY DATE(startTime) 
        ORDER BY DATE(startTime) DESC
    """)
    fun getDailyStudyTimeSummary(): Flow<List<DailyStudyTime>>
    
    // Get weekly subject study time summaries
    @Query("""
        SELECT 
            s.id, 
            s.name, 
            s.color, 
            s.createdAt,
            SUM(ss.durationMinutes) as totalMinutes
        FROM subjects s
        LEFT JOIN study_sessions ss ON s.id = ss.subjectId
        WHERE ss.startTime BETWEEN :startDate AND :endDate
        GROUP BY s.id
        ORDER BY totalMinutes DESC
    """)
    fun getWeeklySubjectTimeSummary(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<WeeklySubjectTime>>
} 