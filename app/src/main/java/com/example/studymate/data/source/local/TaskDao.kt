package com.example.studymate.data.source.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.studymate.data.model.Task
import com.example.studymate.data.model.TaskStatus
import com.example.studymate.data.model.TaskWithSubject
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TaskDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long
    
    @Update
    suspend fun updateTask(task: Task)
    
    @Delete
    suspend fun deleteTask(task: Task)
    
    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Long)
    
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): Task?
    
    @Transaction
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskWithSubjectById(taskId: Long): TaskWithSubject?
    
    @Transaction
    @Query("SELECT * FROM tasks ORDER BY deadline ASC")
    fun getAllTasksWithSubject(): Flow<List<TaskWithSubject>>
    
    @Transaction
    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY deadline ASC")
    fun getTasksByStatus(status: TaskStatus): Flow<List<TaskWithSubject>>
    
    @Transaction
    @Query("SELECT * FROM tasks WHERE deadline = :date ORDER BY priority DESC")
    fun getTasksForDate(date: LocalDate): Flow<List<TaskWithSubject>>
    
    @Transaction
    @Query("SELECT * FROM tasks WHERE subjectId = :subjectId ORDER BY deadline ASC")
    fun getTasksForSubject(subjectId: Long): Flow<List<TaskWithSubject>>
    
    @Transaction
    @Query("SELECT * FROM tasks WHERE deadline < :date AND status = :status")
    fun getOverdueTasks(date: LocalDate, status: TaskStatus = TaskStatus.PENDING): Flow<List<TaskWithSubject>>
    
    @Query("UPDATE tasks SET status = :status WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: Long, status: TaskStatus)
    
    @Query("SELECT COUNT(*) FROM tasks WHERE status = :status")
    fun getTaskCountByStatus(status: TaskStatus): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM tasks WHERE subjectId = :subjectId AND status = :status")
    fun getTaskCountBySubjectAndStatus(subjectId: Long, status: TaskStatus): Flow<Int>
} 