package com.example.taskmaster

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.taskmaster.databinding.FragmentWeatherBinding
import android.webkit.WebViewClient

class BardFragment : Fragment() {

    private lateinit var binding: FragmentWeatherBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWeatherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Enable JavaScript in the WebView
        binding.webView.settings.javaScriptEnabled = true

        // Set a WebViewClient to handle the webpage loading within the WebView
        binding.webView.webViewClient = WebViewClient()

        // Load the Google Bard webpage
        binding.webView.loadUrl("https://bard.google.com/")
    }
}
