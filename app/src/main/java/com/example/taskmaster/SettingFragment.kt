package com.example.taskmaster

import android.content.DialogInterface
import android.content.Intent
import android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG
import android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import com.example.taskmaster.databinding.FragmentSettingBinding
import com.google.firebase.auth.FirebaseAuth

class SettingFragment : Fragment() {

    private lateinit var binding: FragmentSettingBinding

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
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        val loginActivity = requireActivity()
        val intent = Intent(loginActivity, LoginActivity::class.java)
        startActivity(intent)
        loginActivity.finish()
    }
}
