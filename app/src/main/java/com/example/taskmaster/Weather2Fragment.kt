package com.example.taskmaster

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class Weather2Fragment : Fragment() {

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }

    private lateinit var progressBar: ProgressBar
    private lateinit var weatherTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_weather2, container, false)

        progressBar = view.findViewById(R.id.progress_bar)
        weatherTextView = view.findViewById(R.id.weather_text)

        // Fetch weather data
        fetchWeatherData()

        return view
    }

    private fun fetchWeatherData() {
        val apiKey = "4964a25342d6c470365520fa7b81a53c"

        val locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Check for location permission
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            // Request location permission if not granted
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
            return
        }

        // Get the last known location
        val lastKnownLocation =
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if (lastKnownLocation != null) {
            // Fetch weather data using GPS coordinates
            val latitude = lastKnownLocation.latitude
            val longitude = lastKnownLocation.longitude

            val apiUrl =
                "https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&appid=$apiKey&units=metric"

            GlobalScope.launch(Dispatchers.Main) {
                progressBar.visibility = View.VISIBLE
                val result = withContext(Dispatchers.IO) { URL(apiUrl).readText() }
                progressBar.visibility = View.GONE

                parseWeatherData(result)
            }
        } else {
            // If last known location is not available, request location updates
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                0f,
                object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        // Fetch weather data using GPS coordinates
                        val latitude = location.latitude
                        val longitude = location.longitude

                        val apiUrl =
                            "https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&appid=$apiKey&units=metric"

                        GlobalScope.launch(Dispatchers.Main) {
                            progressBar.visibility = View.VISIBLE
                            val result = withContext(Dispatchers.IO) { URL(apiUrl).readText() }
                            progressBar.visibility = View.GONE

                            parseWeatherData(result)
                        }

                        // Stop further location updates
                        locationManager.removeUpdates(this)
                    }

                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                }
            )
        }
    }

    private fun parseWeatherData(data: String) {
        val jsonObject = JSONObject(data)
        val main = jsonObject.getJSONObject("main")
        val temperature = main.getDouble("temp")
        val humidity = main.getInt("humidity")

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dateTime = dateFormat.format(Date())

        val weatherInfo = "Temperature: $temperature°C\nHumidity: $humidity%\nUpdated at: $dateTime"
        weatherTextView.text = weatherInfo
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, fetch weather data
                fetchWeatherData()
            }
        }
    }
}
