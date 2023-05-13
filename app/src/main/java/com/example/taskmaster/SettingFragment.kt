package com.example.taskmaster

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import com.example.taskmaster.databinding.FragmentSettingBinding
import com.google.firebase.auth.FirebaseAuth

class SettingFragment : Fragment() {

    private lateinit var binding: FragmentSettingBinding
    private lateinit var biometricPrompt: BiometricPrompt
    private var isBiometricEnabled: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Logout button handler
        binding.btnLogout.setOnClickListener {
            Log.d("SettingFragment", "Logout button clicked")
            logout()
        }

        // Biometric unlock switch handler
        binding.switchBiometricUnlock.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                enableBiometricUnlock()
            } else {
                disableBiometricUnlock()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isBiometricEnabled) {
            showBiometricPrompt()
        }
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        val loginActivity = requireActivity()
        val intent = Intent(loginActivity, LoginActivity::class.java)
        startActivity(intent)
        loginActivity.finish()
    }

    private fun enableBiometricUnlock() {
        isBiometricEnabled = true
        // Show biometric prompt immediately
        showBiometricPrompt()
    }

    private fun disableBiometricUnlock() {
        isBiometricEnabled = false
        Log.d("SettingFragment", "Biometric unlock disabled")
    }

    private fun showBiometricPrompt() {
        val promptInfo = PromptInfo.Builder()
            .setTitle("Biometric Unlock")
            .setSubtitle("Use biometric to unlock")
            .setNegativeButtonText("Cancel")
            .build()

        val authenticationCallback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                Log.d("SettingFragment", "Biometric authentication succeeded")
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                Log.e("SettingFragment", "Biometric authentication error: $errString")
            }
        }

        val biometricManager = BiometricManager.from(requireContext())
        if (biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
            biometricPrompt = BiometricPrompt(this, authenticationCallback)

            // Display the biometric prompt for authentication
            biometricPrompt.authenticate(promptInfo)
        } else {
            Log.e("SettingFragment", "Biometric authentication is not available or supported")
        }
    }

    override fun onPause() {
        super.onPause()
        if (isBiometricEnabled) {
            biometricPrompt.cancelAuthentication()
        }
    }

    override fun onStop() {
        super.onStop()
        if (isBiometricEnabled) {
            // Show biometric prompt when the user returns to the app
            showBiometricPrompt()
        }
    }
}
