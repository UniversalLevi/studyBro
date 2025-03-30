package com.example.studymate.data.source.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.studymate.data.model.Subject
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject): Long
    
    @Update
    suspend fun updateSubject(subject: Subject)
    
    @Delete
    suspend fun deleteSubject(subject: Subject)
    
    @Query("DELETE FROM subjects WHERE id = :subjectId")
    suspend fun deleteSubjectById(subjectId: Long)
    
    @Query("SELECT * FROM subjects ORDER BY name ASC")
    fun getAllSubjects(): Flow<List<Subject>>
    
    @Query("SELECT * FROM subjects WHERE id = :subjectId")
    fun getSubjectById(subjectId: Long): Flow<Subject?>
    
    @Query("SELECT * FROM subjects WHERE name = :name LIMIT 1")
    fun getSubjectByName(name: String): Flow<Subject?>
    
    @Query("SELECT EXISTS(SELECT 1 FROM subjects WHERE name = :name LIMIT 1)")
    suspend fun subjectExists(name: String): Boolean
} 