package com.example.taskmaster

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.taskmaster.databinding.FragmentSettingBinding
import com.google.firebase.auth.FirebaseAuth

class SettingFragment : Fragment() {

    private lateinit var binding: FragmentSettingBinding
    private lateinit var biometricAdapter: BiometricAdapter

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

        biometricAdapter = BiometricAdapter(requireContext(), this) {
            // Biometric authentication succeeded callback
        }

        // Logout button handler
        binding.btnLogout.setOnClickListener {
            Log.d("SettingFragment", "Logout button clicked")
            logout()
        }

        // Biometric unlock switch handler
        binding.switchBiometricUnlock.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                biometricAdapter.enableBiometricUnlock()
            } else {
                biometricAdapter.disableBiometricUnlock()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Show biometric prompt if enabled and initialized
        if (::biometricAdapter.isInitialized) {
            biometricAdapter.showBiometricPrompt()
        }
    }

    override fun onPause() {
        super.onPause()
        // Cancel biometric authentication if enabled and initialized
        if (::biometricAdapter.isInitialized) {
            biometricAdapter.cancelAuthentication()
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
