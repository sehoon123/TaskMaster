package com.example.taskmaster

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentTimerBinding.inflate(inflater, container, false)
        context = requireContext()

//        // 알람매니저 설정
//        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//
//        // 알람리시버 intent 생성
//        val myIntent = Intent(context, AlarmReceiver::class.java)
//
//        // Calendar 객체 생성
//        val calendar = Calendar.getInstance()


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


        startBtn.setOnClickListener{
            timeCountSettingLV.visibility = View.GONE
            timeCountLV.visibility = View.VISIBLE

            if(hourET.text.toString() == ""){
                hourET.setText("0");
            }
            if(minuteET.text.toString() == ""){
                minuteET.setText("0");
            }
            if(secondET.text.toString() == ""){
                secondET.setText("0");
            }


            hourTV.text = hourET.text.toString()
            minuteTV.text = minuteET.text.toString()
            secondTV.text = secondET.text.toString()

            var hour = hourET.text.toString().toInt()
            var minute = minuteET.text.toString().toInt()
            var second = secondET.text.toString().toInt()


            val handler = Handler(Looper.getMainLooper())
            val timer = Timer()
            val timerTask = object : TimerTask() {
                override fun run() {
                    handler.post{
                        // 반복 시행할 구문

                        if (second != 0) {
                            second --

                        } else if (minute != 0) {
                            second = 60
                            second--
                            minute--

                        } else if (hour != 0) {
                            second = 60
                            minute = 60
                            second--
                            minute--
                            hour--
                        }

                        // 시, 분, 초가 10이하 한 자리수면 숫자 앞에 0을 붙임
                        secondTV.text = if (second <= 9) "0$second" else second.toString()
                        minuteTV.text = if (minute <= 9) "0$minute" else minute.toString()
                        hourTV.text = if (hour <= 9) "0$hour" else hour.toString()

                        // 시분초가 다 0이면 toast 띄우고 타이머 종료
                        if (hour == 0 && minute == 0 && second == 0) {
                            timer.cancel()
                            Toast.makeText(context,"타이머가 종료되었습니다",Toast.LENGTH_SHORT).show()

                            // BroadcastReceiver를 호출하여 알람을 울리도록 설정
                            val alarmIntent = Intent(context, AlarmReceiver::class.java)
                            context.sendBroadcast(alarmIntent)
                        }
                    }

                }
            }

            // 타이머 실행
            timer.schedule(timerTask, 0, 1000)

            pauseBtn.setOnClickListener {
                timerTask.cancel()
            }
        }

        return binding.root
    }

}