package com.example.taskmaster

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.taskmaster.databinding.ActivityMainBinding
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var todoNotificationUtils: TodoNotificationUtils
    private lateinit var alarmManager: AlarmManager
    private lateinit var alarmIntent: PendingIntent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize TodoNotificationUtils
        todoNotificationUtils = TodoNotificationUtils()

        // Set up alarm for periodic notification checks
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, TodoNotificationUtils::class.java)
        alarmIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
//        val interval = TimeUnit.HOURS.toMillis(1) // Adjust the interval as needed
        val interval = TimeUnit.SECONDS.toMillis(5)
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + interval,
            interval,
            alarmIntent
        )

        // Launch the todo fragment
        loadFragment(TodoFragment())

        binding.bottomNavView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.cal_menu_item -> {
                    // Launch the calendar fragment
                    val calendarFragment = CalendarFragment()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, calendarFragment)
                        .commit()
                    true
                }
                R.id.list_menu_item -> {
                    // Launch the todo fragment
                    loadFragment(TodoFragment())
                    true
                }
                R.id.etc_menu_item -> {
                    // Launch the etc fragment
                    loadFragment(GalleryFragment())
                    true
                }
                R.id.camera_menu_item -> {
                    // Launch the camera fragment
                    loadFragment(CameraFragment())
                    true
                }
                else -> false
            }
        }
        binding.bottomNavView.menu.findItem(R.id.list_menu_item).isChecked = true

        // Set onClickListener for the settings button
        binding.btnSetting.setOnClickListener {
            val settingFragment = SettingFragment()
            loadFragment(settingFragment)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel the alarm when the activity is destroyed
        alarmManager.cancel(alarmIntent)
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
    }
}
