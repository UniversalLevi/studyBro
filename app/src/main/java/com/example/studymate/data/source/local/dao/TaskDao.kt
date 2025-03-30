package com.example.studymate.data.source.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.studymate.data.model.Task
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
    
    @Query("SELECT * FROM tasks ORDER BY deadline ASC")
    fun getAllTasks(): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun getTaskById(taskId: Long): Flow<Task?>
    
    @Query("SELECT * FROM tasks WHERE status = 'PENDING' ORDER BY deadline ASC")
    fun getPendingTasks(): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE status = 'COMPLETED' ORDER BY deadline DESC")
    fun getCompletedTasks(): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE deadline = :date")
    fun getTasksByDate(date: LocalDate): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE subjectId = :subjectId")
    fun getTasksBySubject(subjectId: Long): Flow<List<Task>>
    
    @Transaction
    @Query("SELECT * FROM tasks")
    fun getTasksWithSubjects(): Flow<List<TaskWithSubject>>
    
    @Transaction
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun getTaskWithSubject(taskId: Long): Flow<TaskWithSubject?>
    
    @Query("UPDATE tasks SET status = :status WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: Long, status: String)
    
    // Additional methods required by the repository
    
    @Query("SELECT COUNT(*) FROM tasks WHERE status = :status")
    fun getTaskCountByStatus(status: String): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM tasks WHERE subjectId = :subjectId AND status = :status")
    fun getTaskCountBySubjectAndStatus(subjectId: Long, status: String): Flow<Int>
    
    @Transaction
    @Query("SELECT * FROM tasks WHERE status = :status")
    fun getTasksByStatus(status: String): Flow<List<TaskWithSubject>>
    
    @Transaction
    @Query("SELECT * FROM tasks WHERE deadline = :date")
    fun getTasksForDate(date: LocalDate): Flow<List<TaskWithSubject>>
    
    @Transaction
    @Query("SELECT * FROM tasks WHERE subjectId = :subjectId")
    fun getTasksForSubject(subjectId: Long): Flow<List<TaskWithSubject>>
    
    @Transaction
    @Query("SELECT * FROM tasks WHERE deadline < :currentDate AND status = 'PENDING'")
    fun getOverdueTasks(currentDate: LocalDate): Flow<List<TaskWithSubject>>
    
    @Transaction
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskWithSubjectById(taskId: Long): TaskWithSubject?
    
    @Transaction
    @Query("SELECT * FROM tasks")
    fun getAllTasksWithSubject(): Flow<List<TaskWithSubject>>
} 