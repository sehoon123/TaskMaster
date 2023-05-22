package com.example.taskmaster

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class BiometricAdapter(
    private val context: Context,
    private val fragment: Fragment,
    private val onAuthenticationSuccess: () -> Unit
) {
    private lateinit var biometricPrompt: BiometricPrompt
    private var isBiometricEnabled: Boolean = false

    fun enableBiometricUnlock() {
        isBiometricEnabled = true
        // Show biometric prompt immediately
        showBiometricPrompt()
    }

    fun disableBiometricUnlock() {
        isBiometricEnabled = false
        Log.d("BiometricAdapter", "Biometric unlock disabled")
    }

    fun showBiometricPrompt() {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Unlock")
            .setSubtitle("Use biometric to unlock")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        biometricPrompt = BiometricPrompt(
            fragment,
            ContextCompat.getMainExecutor(context),
            authenticationCallback
        )

        biometricPrompt.authenticate(promptInfo)
    }

    fun cancelAuthentication() {
        if (::biometricPrompt.isInitialized) {
            biometricPrompt.cancelAuthentication()
        }
    }

    private val authenticationCallback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            Log.d("BiometricAdapter", "Biometric authentication succeeded")
            onAuthenticationSuccess.invoke()
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            Log.e("BiometricAdapter", "Biometric authentication error: $errString")
            if (errorCode == BiometricPrompt.ERROR_USER_CANCELED || errorCode == BiometricPrompt.ERROR_CANCELED) {
                // If the authentication was canceled, show the BiometricPrompt again
                showBiometricPrompt()
            }
        }
    }
}
