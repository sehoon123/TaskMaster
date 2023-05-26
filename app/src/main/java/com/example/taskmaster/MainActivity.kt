package com.example.taskmaster

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.taskmaster.databinding.ActivityMainBinding
import java.util.Calendar
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var sidePanel: LinearLayout
    private lateinit var overlayView: View
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private var isSidePanelOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeApp()

        // Check if the app has notification authority
        if (!hasNotificationAuthority()) {
            requestNotificationAuthority()
        }

        scheduleNotificationCheck()

        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                    if (errorCode == BiometricPrompt.ERROR_USER_CANCELED || errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        // User canceled the login prompt, so just show the password prompt
                        showBiometricPrompt()
                    } else {
                        // Call showBiometricPrompt() again to show the prompt
                        showBiometricPrompt()
                    }
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(applicationContext, "Authentication succeeded!", Toast.LENGTH_SHORT).show()
                    loadFragment(TodoFragment())
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Authentication failed", Toast.LENGTH_SHORT).show()
                    showBiometricPrompt()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for my app")
            .setSubtitle("Log in using your biometric credential")
            .setConfirmationRequired(false)
            .setNegativeButtonText("Use account password")
            .build()

        // Check if biometric authentication is available
        val biometricManager = BiometricManager.from(this)
        if (biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
            // Biometric authentication is available, request authentication
            biometricPrompt.authenticate(promptInfo)
        } else {
            // Biometric authentication is not available or not enabled
            Toast.makeText(applicationContext, "Biometric authentication is not available", Toast.LENGTH_SHORT).show()
            // You can handle this case based on your app's requirements
        }
    }

    private fun showBiometricPrompt() {

        biometricPrompt.authenticate(promptInfo)
    }
    private fun initializeApp() {
        Log.d("MainActivity", "Initializing app")
        drawerLayout = binding.drawerLayout
        sidePanel = binding.sidePanel
        overlayView = binding.overlayView

        // Set up the side panel fragment
        val sidePanelFragment = SidePanelFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.side_panel, sidePanelFragment)
            .commit()

        binding.bottomNavView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.cal_menu_item -> {
                    // Launch the calendar fragment
                    loadFragment(CalendarFragment())
                    true
                }
                R.id.list_menu_item -> {
                    // Launch the todo fragment
                    loadFragment(TodoFragment())
                    true
                }
                R.id.etc_menu_item -> {
                    // Open the side panel
                    drawerLayout.openDrawer(sidePanel)
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

        // Disable touch events for the main content area when the side panel is open
        drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                isSidePanelOpen = slideOffset == 1.0f
                overlayView.isVisible = isSidePanelOpen
            }
        })

        overlayView.setOnClickListener {
            // Handle clicks on the overlay view
            // This prevents interaction with the fragments behind the side panel
        }

        binding.fragmentContainer.setOnTouchListener { _, _ ->
            isSidePanelOpen
        }

    }

    // Handle the back button press to close the side panel if it's open
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(sidePanel)) {
            drawerLayout.closeDrawer(sidePanel)
        } else {
            super.onBackPressed()
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
    }

    override fun onResume() {
        super.onResume()
        // Check if biometric authentication is available
        val biometricManager = BiometricManager.from(this)
        if (biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
            // Biometric authentication is available, request authentication
            biometricPrompt.authenticate(promptInfo)
        } else {
            // Biometric authentication is not available or not enabled
            Toast.makeText(applicationContext, "Biometric authentication is not available", Toast.LENGTH_SHORT).show()
            // You can handle this case based on your app's requirements
        }
    }

    private fun scheduleNotificationCheck() {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val notificationIntent = Intent(this, TodoNotificationUtils::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // Set the time when you want the notification check to be triggered
//        val notificationCheckTimeMillis = calculateNotificationCheckTimeMillis()

        // Set the alarm to trigger the BroadcastReceiver at the specified time
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
//            notificationCheckTimeMillis,
            System.currentTimeMillis(),
            10 * 1000, // 10 seconds
            pendingIntent
        )
    }

    private fun calculateNotificationCheckTimeMillis(): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        // If the current time is already past the notification check time for today,
        // schedule it for the same time on the next day
        if (calendar.timeInMillis < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return calendar.timeInMillis
    }

    private fun hasNotificationAuthority(): Boolean {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.notificationChannels.isNotEmpty()
        } else {
            // On older Android versions, assume the app has notification authority
            true
        }
    }

    private fun requestNotificationAuthority() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create a notification channel with the desired settings
            val channelId = "todo_channel"
            val channelName = "Todo Channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance)

            // Register the channel with the system
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
