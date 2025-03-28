package com.example.studymate.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.studymate.MainActivity
import com.example.studymate.R
import com.example.studymate.StudyMateApp
import com.example.studymate.data.model.SessionType
import com.example.studymate.receiver.NotificationReceiver
import java.time.LocalDateTime

/**
 * Foreground service to handle timing sessions even when the app is in the background
 */
class TimerService : Service() {
    
    private val binder = TimerBinder()
    private var countDownTimer: CountDownTimer? = null
    private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
    
    // Timer state
    private var isRunning = false
    private var isPaused = false
    private var timeLeftInMillis: Long = 0
    private var originalTimeInMillis: Long = 0
    private var subjectId: Long = 0
    private var sessionType: SessionType = SessionType.STUDY
    private var timerStartTime: LocalDateTime? = null
    
    // Callbacks for UI updates
    private var timerTickCallback: ((Long) -> Unit)? = null
    private var timerFinishCallback: (() -> Unit)? = null
    
    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }
    
    override fun onBind(intent: Intent): IBinder {
        return binder
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TIMER -> {
                val timeInMinutes = intent.getIntExtra(EXTRA_TIME_MINUTES, 25)
                val subjectIdExtra = intent.getLongExtra(EXTRA_SUBJECT_ID, 0)
                val sessionTypeOrdinal = intent.getIntExtra(EXTRA_SESSION_TYPE, SessionType.STUDY.ordinal)
                val sessionTypeValue = SessionType.values()[sessionTypeOrdinal]
                
                startTimer(timeInMinutes * 60 * 1000L, subjectIdExtra, sessionTypeValue)
            }
            ACTION_PAUSE_TIMER -> pauseTimer()
            ACTION_RESUME_TIMER -> resumeTimer()
            ACTION_STOP_TIMER -> stopTimer()
        }
        return START_STICKY
    }
    
    fun startTimer(timeInMillis: Long, subjectId: Long, sessionType: SessionType) {
        this.originalTimeInMillis = timeInMillis
        this.timeLeftInMillis = timeInMillis
        this.subjectId = subjectId
        this.sessionType = sessionType
        this.timerStartTime = LocalDateTime.now()
        
        startForeground(NOTIFICATION_ID, createNotification())
        
        countDownTimer = object : CountDownTimer(timeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                timerTickCallback?.invoke(millisUntilFinished)
                updateNotification()
            }
            
            override fun onFinish() {
                isRunning = false
                timerFinishCallback?.invoke()
                handleTimerCompletion()
            }
        }.start()
        
        isRunning = true
        isPaused = false
    }
    
    fun pauseTimer() {
        countDownTimer?.cancel()
        isPaused = true
        isRunning = false
        updateNotification()
    }
    
    fun resumeTimer() {
        startTimer(timeLeftInMillis, subjectId, sessionType)
    }
    
    fun stopTimer() {
        countDownTimer?.cancel()
        isRunning = false
        isPaused = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    fun setTimerTickCallback(callback: (Long) -> Unit) {
        timerTickCallback = callback
    }
    
    fun setTimerFinishCallback(callback: () -> Unit) {
        timerFinishCallback = callback
    }
    
    fun getTimeLeftInMillis(): Long = timeLeftInMillis
    
    fun isTimerRunning(): Boolean = isRunning
    
    fun isTimerPaused(): Boolean = isPaused
    
    private fun createNotification(): Notification {
        // Create notification channel
        val title = if (sessionType == SessionType.STUDY) 
            getString(R.string.timer_study) else getString(R.string.timer_break)
        
        // Intent to open the app when notification is clicked
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        
        // Action intents
        val pauseIntent = Intent(this, TimerService::class.java).apply {
            action = if (isPaused) ACTION_RESUME_TIMER else ACTION_PAUSE_TIMER
        }
        val pausePendingIntent = PendingIntent.getService(
            this, 
            1, 
            pauseIntent, 
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val stopIntent = Intent(this, TimerService::class.java).apply {
            action = ACTION_STOP_TIMER
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 
            2, 
            stopIntent, 
            PendingIntent.FLAG_IMMUTABLE
        )
        
        // Create notification
        return NotificationCompat.Builder(this, StudyMateApp.TIMER_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(formatTimeLeft(timeLeftInMillis))
            .setSmallIcon(R.drawable.ic_splash)
            .setContentIntent(contentIntent)
            .addAction(
                android.R.drawable.ic_media_pause,
                if (isPaused) getString(R.string.timer_resume) else getString(R.string.timer_pause),
                pausePendingIntent
            )
            .addAction(
                android.R.drawable.ic_media_pause,
                getString(R.string.timer_reset),
                stopPendingIntent
            )
            .setOngoing(true)
            .build()
    }
    
    private fun updateNotification() {
        notificationManager.notify(NOTIFICATION_ID, createNotification())
    }
    
    private fun handleTimerCompletion() {
        // Create and record the study session
        val endTime = LocalDateTime.now()
        val startTime = timerStartTime ?: endTime.minusSeconds(originalTimeInMillis / 1000)
        
        // Create a notification to alert the user that timer is finished
        val intent = Intent(this, NotificationReceiver::class.java).apply {
            action = NotificationReceiver.ACTION_TIMER_FINISHED
            putExtra(NotificationReceiver.EXTRA_SESSION_TYPE, sessionType.ordinal)
            putExtra(NotificationReceiver.EXTRA_SUBJECT_ID, subjectId)
            putExtra(NotificationReceiver.EXTRA_START_TIME, startTime.toString())
            putExtra(NotificationReceiver.EXTRA_END_TIME, endTime.toString())
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        // Show a notification
        val notificationTitle = getString(R.string.timer_notification_title)
        val notificationText = if (sessionType == SessionType.STUDY) 
            getString(R.string.timer_notification_study_message)
        else 
            getString(R.string.timer_notification_break_message)
        
        val notification = NotificationCompat.Builder(this, StudyMateApp.TIMER_CHANNEL_ID)
            .setContentTitle(notificationTitle)
            .setContentText(notificationText)
            .setSmallIcon(R.drawable.ic_splash)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(TIMER_FINISHED_NOTIFICATION_ID, notification)
        
        // Stop foreground service
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    private fun formatTimeLeft(timeInMillis: Long): String {
        val minutes = (timeInMillis / 1000) / 60
        val seconds = (timeInMillis / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
    
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val TIMER_FINISHED_NOTIFICATION_ID = 2
        
        const val ACTION_START_TIMER = "com.example.studymate.action.START_TIMER"
        const val ACTION_PAUSE_TIMER = "com.example.studymate.action.PAUSE_TIMER"
        const val ACTION_RESUME_TIMER = "com.example.studymate.action.RESUME_TIMER"
        const val ACTION_STOP_TIMER = "com.example.studymate.action.STOP_TIMER"
        
        const val EXTRA_TIME_MINUTES = "com.example.studymate.extra.TIME_MINUTES"
        const val EXTRA_SUBJECT_ID = "com.example.studymate.extra.SUBJECT_ID"
        const val EXTRA_SESSION_TYPE = "com.example.studymate.extra.SESSION_TYPE"
    }
} 