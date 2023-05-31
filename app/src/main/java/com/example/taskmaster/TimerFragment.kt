package com.example.taskmaster

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Looper
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.taskmaster.databinding.FragmentTimerBinding
import com.google.firebase.database.Transaction
import java.util.Calendar
import java.util.Timer
import java.util.TimerTask

class TimerFragment : Fragment() {
    private lateinit var binding: FragmentTimerBinding
    private lateinit var alarmManager: AlarmManager
    private lateinit var context: Context
    private var timer: Timer? = null
    private var timerTask: TimerTask? = null
    private var isRunning = false
    private var isPaused = false
    private var hour = 0
    private var minute = 0
    private var second = 0

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private var savedHour: Int = 0
    private var savedMinute: Int = 0
    private var savedSecond: Int = 0
    private var startTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = requireContext().getSharedPreferences("TimerPrefs", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        if (sharedPreferences.contains(TIMER_IS_RUNNING)) {
            isRunning = sharedPreferences.getBoolean(TIMER_IS_RUNNING, false)
            if (isRunning) {
                savedHour = sharedPreferences.getInt(TIMER_HOUR, 0)
                savedMinute = sharedPreferences.getInt(TIMER_MINUTE, 0)
                savedSecond = sharedPreferences.getInt(TIMER_SECOND, 0)
                startTime = sharedPreferences.getLong(TIMER_START_TIME, 0)

                val elapsedTimeInMillis = System.currentTimeMillis() - startTime
                val totalTimeInMillis = ((savedHour * 3600) + (savedMinute * 60) + savedSecond) * 1000L
                val remainingTimeInMillis = totalTimeInMillis - elapsedTimeInMillis

                if (remainingTimeInMillis > 0) {
                    hour = (remainingTimeInMillis / (1000 * 60 * 60)).toInt()
                    minute = (remainingTimeInMillis / (1000 * 60) % 60).toInt()
                    second = (remainingTimeInMillis / 1000 % 60).toInt()
                } else {
                    isRunning = false
                    isPaused = false
                    hour = savedHour
                    minute = savedMinute
                    second = savedSecond
                    editor.clear().apply()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentTimerBinding.inflate(inflater, container, false)
        context = requireContext()

        val timeCountSettingLV = binding.timeCountSettingLV
        val timeCountLV = binding.timeCountLV

        val hourET = binding.hourET
        val minuteET = binding.minuteET
        val secondET = binding.secondET

        val hourTV = binding.hourTV
        val minuteTV = binding.minuteTV
        val secondTV = binding.secondTV

        val finishTV = binding.finishTV
        val startBtn = binding.startBtn
        val pauseBtn = binding.pauseBtn

        if (isRunning) {
            timeCountSettingLV.visibility = View.GONE
            timeCountLV.visibility = View.VISIBLE

            hourET.setText(hour.toString())
            minuteET.setText(minute.toString())
            secondET.setText(second.toString())

            hourTV.text = hour.toString().padStart(2, '0')
            minuteTV.text = minute.toString().padStart(2, '0')
            secondTV.text = second.toString().padStart(2, '0')

            val handler = Handler(Looper.getMainLooper())
            timer = Timer()
            timerTask = object : TimerTask() {
                override fun run() {
                    handler.post {
                        // Perform the repetitive task

                        if (second != 0) {
                            second--
                        } else if (minute != 0) {
                            second = 59
                            minute--
                        } else if (hour != 0) {
                            second = 59
                            minute = 59
                            hour--
                        }

                        // Append leading zeros if the hour, minute, or second is a single digit
                        secondTV.text = second.toString().padStart(2, '0')
                        minuteTV.text = minute.toString().padStart(2, '0')
                        hourTV.text = hour.toString().padStart(2, '0')

                        // Display a toast and stop the timer if the hour, minute, and second are all zero
                        if (hour == 0 && minute == 0 && second == 0) {
                            timer?.cancel()
                            Toast.makeText(context, "타이머가 종료되었습니다", Toast.LENGTH_SHORT).show()

                            // Send a broadcast to trigger an alarm
                            val alarmIntent = Intent(context, AlarmReceiver::class.java)
                            context.sendBroadcast(alarmIntent)
                        }
                    }
                }
            }

            // Start the timer
            timer?.schedule(timerTask, 0, 1000)

            // Change button text to "Reset"
            startBtn.text = "Reset"
            isRunning = true
            isPaused = false
        }

        startBtn.setOnClickListener {
            if (!isRunning) {
                // Start the timer
                timeCountSettingLV.visibility = View.GONE
                timeCountLV.visibility = View.VISIBLE

                if (hourET.text.toString().isNotBlank()) {
                    hour = hourET.text.toString().toInt()
                }
                if (minuteET.text.toString().isNotBlank()) {
                    minute = minuteET.text.toString().toInt()
                }
                if (secondET.text.toString().isNotBlank()) {
                    second = secondET.text.toString().toInt()
                }

                hourTV.text = hour.toString().padStart(2, '0')
                minuteTV.text = minute.toString().padStart(2, '0')
                secondTV.text = second.toString().padStart(2, '0')

                val handler = Handler(Looper.getMainLooper())
                timer = Timer()
                timerTask = object : TimerTask() {
                    override fun run() {
                        handler.post {
                            // Perform the repetitive task

                            if (second != 0) {
                                second--
                            } else if (minute != 0) {
                                second = 59
                                minute--
                            } else if (hour != 0) {
                                second = 59
                                minute = 59
                                hour--
                            }

                            // Append leading zeros if the hour, minute, or second is a single digit
                            secondTV.text = second.toString().padStart(2, '0')
                            minuteTV.text = minute.toString().padStart(2, '0')
                            hourTV.text = hour.toString().padStart(2, '0')

                            // Display a toast and stop the timer if the hour, minute, and second are all zero
                            if (hour == 0 && minute == 0 && second == 0) {
                                timer?.cancel()
                                Toast.makeText(context, "타이머가 종료되었습니다", Toast.LENGTH_SHORT).show()

                                // Send a broadcast to trigger an alarm
                                val alarmIntent = Intent(context, AlarmReceiver::class.java)
                                context.sendBroadcast(alarmIntent)
                            }
                        }
                    }
                }

                // Start the timer
                timer?.schedule(timerTask, 0, 1000)

                // Change button text to "Reset"
                startBtn.text = "Reset"
                isRunning = true
                isPaused = false
            } else {
                // Reset the timer
                timerTask?.cancel()
                timer?.cancel()
                hour = 0
                minute = 0
                second = 0
                hourTV.text = "00"
                minuteTV.text = "00"
                secondTV.text = "00"
                hourET.text = null
                minuteET.text = null
                secondET.text = null
                timeCountSettingLV.visibility = View.VISIBLE
                timeCountLV.visibility = View.GONE

                // Change button text back to "Start"
                startBtn.text = "Start"
                isRunning = false
                isPaused = false
            }
        }

        pauseBtn.setOnClickListener {
            if (isRunning && !isPaused) {
                // Pause the timer
                timerTask?.cancel()

                // Change button text to "Resume"
                pauseBtn.text = "Resume"
                isPaused = true
            } else if (isRunning && isPaused) {
                // Resume the timer
                val handler = Handler(Looper.getMainLooper())
                timer = Timer()
                timerTask = object : TimerTask() {
                    override fun run() {
                        handler.post {
                            // Perform the repetitive task

                            if (second != 0) {
                                second--
                            } else if (minute != 0) {
                                second = 59
                                minute--
                            } else if (hour != 0) {
                                second = 59
                                minute = 59
                                hour--
                            }

                            // Append leading zeros if the hour, minute, or second is a single digit
                            secondTV.text = second.toString().padStart(2, '0')
                            minuteTV.text = minute.toString().padStart(2, '0')
                            hourTV.text = hour.toString().padStart(2, '0')

                            // Display a toast and stop the timer if the hour, minute, and second are all zero
                            if (hour == 0 && minute == 0 && second == 0) {
                                timer?.cancel()
                                Toast.makeText(context, "타이머가 종료되었습니다", Toast.LENGTH_SHORT).show()

                                // Send a broadcast to trigger an alarm
                                val alarmIntent = Intent(context, AlarmReceiver::class.java)
                                context.sendBroadcast(alarmIntent)
                            }
                        }
                    }
                }

                // Resume the timer
                timer?.schedule(timerTask, 0, 1000)

                // Change button text back to "Pause"
                pauseBtn.text = "Pause"
                isPaused = false
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        saveTimerState()
    }

    private fun saveTimerState() {
        editor.putBoolean(TIMER_IS_RUNNING, isRunning)
        if (isRunning) {
            editor.putInt(TIMER_HOUR, hour)
            editor.putInt(TIMER_MINUTE, minute)
            editor.putInt(TIMER_SECOND, second)
            editor.putLong(TIMER_START_TIME, System.currentTimeMillis())
        }
        editor.apply()
    }

    companion object {
        private const val TIMER_IS_RUNNING = "timer_is_running"
        private const val TIMER_HOUR = "timer_hour"
        private const val TIMER_MINUTE = "timer_minute"
        private const val TIMER_SECOND = "timer_second"
        private const val TIMER_START_TIME = "timer_start_time"
    }
}
