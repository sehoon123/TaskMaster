package com.example.taskmaster

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class SidePanelFragment : Fragment() {

    private lateinit var rootView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment_side_panel, container, false)

        // Set click listeners for the buttons
        rootView.findViewById<View>(R.id.btnBard).setOnClickListener {
            loadBardFragment()
        }

        rootView.findViewById<View>(R.id.btnWeather).setOnClickListener {
            loadWeatherFragment()
        }

        rootView.findViewById<View>(R.id.btnGallery).setOnClickListener {
            loadGalleryFragment()
        }

        rootView.findViewById<View>(R.id.btnMap).setOnClickListener {
            loadMapFragment()
        }

        return rootView
    }

    private fun loadBardFragment() {
        val fragment = BardFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun loadWeatherFragment() {
        val fragment = Weather2Fragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun loadGalleryFragment() {
        val fragment = GalleryFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun loadMapFragment() {
        val fragment = MapFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
