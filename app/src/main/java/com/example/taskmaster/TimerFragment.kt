package com.example.taskmaster

import android.os.Bundle
import android.os.Looper
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.taskmaster.databinding.FragmentTimerBinding
import com.google.firebase.database.Transaction
import java.util.Timer
import java.util.TimerTask


class TimerFragment : Fragment() {
    lateinit var binding: FragmentTimerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentTimerBinding.inflate(inflater, container, false)


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
                            finishTV.text = "타이머가 종료되었습니다."
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