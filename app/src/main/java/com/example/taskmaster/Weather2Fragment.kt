package com.example.taskmaster

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
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
        val city = "Seoul" // Replace with the desired city name

        val apiUrl =
            "https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apiKey&units=metric"

        GlobalScope.launch(Dispatchers.Main) {
            progressBar.visibility = View.VISIBLE
            val result = withContext(Dispatchers.IO) { URL(apiUrl).readText() }
            progressBar.visibility = View.GONE

            parseWeatherData(result)
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
}
