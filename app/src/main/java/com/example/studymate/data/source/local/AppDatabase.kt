package com.example.studymate.data.source.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.studymate.data.model.Subject
import com.example.studymate.data.model.StudySession
import com.example.studymate.data.model.Task
import com.example.studymate.data.source.local.dao.StudySessionDao
import com.example.studymate.data.source.local.dao.SubjectDao
import com.example.studymate.data.source.local.dao.TaskDao

@Database(
    entities = [Task::class, Subject::class, StudySession::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun subjectDao(): SubjectDao
    abstract fun studySessionDao(): StudySessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
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