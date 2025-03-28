package com.example.studymate.data.source.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.studymate.data.model.StudySession
import com.example.studymate.data.model.Subject
import com.example.studymate.data.model.Task
import com.example.studymate.data.source.local.SubjectDao
import com.example.studymate.data.source.local.TaskDao
import com.example.studymate.data.source.local.StudySessionDao
import com.example.studymate.util.Converters

@Database(
    entities = [Task::class, Subject::class, StudySession::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class StudyMateDatabase : RoomDatabase() {
    
    abstract fun taskDao(): TaskDao
    abstract fun subjectDao(): SubjectDao
    abstract fun studySessionDao(): StudySessionDao
    
    companion object {
        @Volatile
        private var INSTANCE: StudyMateDatabase? = null
        
        fun getDatabase(context: Context): StudyMateDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudyMateDatabase::class.java,
                    "studymate_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
} 