package com.example.taskmaster

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.taskmaster.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Launch the todo fragment
        loadFragment(TodoFragment())

        binding.bottomNavView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.cal_menu_item -> {
                    // Launch the calendar fragment
//                    loadFragment(TestFragment())
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
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
    }
}
