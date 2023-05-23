package com.example.taskmaster

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
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
}
