package com.example.taskmaster

import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.os.Binder
import android.os.CountDownTimer
import android.os.IBinder
import android.widget.Toast

class TimerService : Service() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var timer: CountDownTimer
    private var isPaused: Boolean = false

    inner class LocalBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences("TimerPrefs", MODE_PRIVATE)
        editor = sharedPreferences.edit()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val hour = intent.getIntExtra(EXTRA_HOUR, 0)
            val minute = intent.getIntExtra(EXTRA_MINUTE, 0)
            val second = intent.getIntExtra(EXTRA_SECOND, 0)

            if (sharedPreferences.contains(EXTRA_HOUR) && sharedPreferences.contains(EXTRA_MINUTE) && sharedPreferences.contains(EXTRA_SECOND)) {
                // Timer was already running, retrieve the saved values
                val savedHour = sharedPreferences.getInt(EXTRA_HOUR, 0)
                val savedMinute = sharedPreferences.getInt(EXTRA_MINUTE, 0)
                val savedSecond = sharedPreferences.getInt(EXTRA_SECOND, 0)

                // Adjust the timer values based on the time elapsed since the previous run
                val elapsedTimeInMillis = System.currentTimeMillis() - sharedPreferences.getLong("StartTime", 0)
                val totalTimeInMillis = ((savedHour * 3600) + (savedMinute * 60) + savedSecond) * 1000L
                val remainingTimeInMillis = totalTimeInMillis - elapsedTimeInMillis

                if (remainingTimeInMillis > 0) {
                    val remainingHour = (remainingTimeInMillis / (1000 * 60 * 60)).toInt()
                    val remainingMinute = (remainingTimeInMillis / (1000 * 60) % 60).toInt()
                    val remainingSecond = (remainingTimeInMillis / 1000 % 60).toInt()

                    // Start the timer with the adjusted values
                    startTimer(remainingHour, remainingMinute, remainingSecond)
                } else {
                    // Timer already completed
                    sendTimerCompleteBroadcast()
                }
            } else {
                // Timer was not running previously, start a new timer
                startTimer(hour, minute, second)
            }
        }

        return START_NOT_STICKY
    }

    private fun startTimer(hour: Int, minute: Int, second: Int) {
        val totalTimeInMillis = ((hour * 3600) + (minute * 60) + second) * 1000L

        timer = object : CountDownTimer(totalTimeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (!isPaused) {
                    val updatedSecond = (millisUntilFinished / 1000).toInt() % 60
                    val updatedMinute = (millisUntilFinished / (1000 * 60) % 60).toInt()
                    val updatedHour = (millisUntilFinished / (1000 * 60 * 60)).toInt()

                    // Broadcast timer values to the TimerFragment
                    val intent = Intent(ACTION_TIMER_UPDATE)
                    intent.putExtra(EXTRA_HOUR, updatedHour)
                    intent.putExtra(EXTRA_MINUTE, updatedMinute)
                    intent.putExtra(EXTRA_SECOND, updatedSecond)
                    sendBroadcast(intent)
                }
            }

            override fun onFinish() {
                Toast.makeText(applicationContext, "타이머가 종료되었습니다", Toast.LENGTH_SHORT).show()
                sendTimerCompleteBroadcast()
            }
        }

        timer.start()
        saveTimerValues(hour, minute, second)
    }

    fun pauseTimer() {
        isPaused = true
    }

    fun resumeTimer() {
        isPaused = false
    }

    fun resetTimer() {
        timer.cancel()
        editor.clear().apply()
    }

    private fun sendTimerCompleteBroadcast() {
        // Broadcast timer completion to the TimerFragment
        val intent = Intent(ACTION_TIMER_COMPLETE)
        sendBroadcast(intent)
    }

    private fun saveTimerValues(hour: Int, minute: Int, second: Int) {
        editor.putInt(EXTRA_HOUR, hour)
        editor.putInt(EXTRA_MINUTE, minute)
        editor.putInt(EXTRA_SECOND, second)
        editor.putLong("StartTime", System.currentTimeMillis())
        editor.apply()
    }

    companion object {
        const val ACTION_TIMER_UPDATE = "com.example.taskmaster.ACTION_TIMER_UPDATE"
        const val ACTION_TIMER_COMPLETE = "com.example.taskmaster.ACTION_TIMER_COMPLETE"
        const val EXTRA_HOUR = "com.example.taskmaster.EXTRA_HOUR"
        const val EXTRA_MINUTE = "com.example.taskmaster.EXTRA_MINUTE"
        const val EXTRA_SECOND = "com.example.taskmaster.EXTRA_SECOND"
    }
}
