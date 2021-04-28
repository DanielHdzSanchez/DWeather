package com.dan.dweather

import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private val CITY_NAME = "Piedras Negras, MX"
    private val API = "119bee8d190813f3a7ed9520af0e7e7d"
    private lateinit var progressBar: ProgressBar
    private lateinit var mainContainer: RelativeLayout
    private lateinit var tvError: TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var currentBackground: Drawable
    private lateinit var superContainer: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        weatherTask().execute()

        superContainer = findViewById(R.id.superContainer)

        currentBackground = ContextCompat.getDrawable(this, R.drawable.gradient_bg)!!


        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            weatherTask().execute()
            swipeRefreshLayout.isRefreshing = false
        }
    }

    inner class weatherTask: AsyncTask<String, Unit, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
            progressBar = findViewById(R.id.loader)
            progressBar.visibility = View.VISIBLE
            mainContainer = findViewById(R.id.mainContainer)
            //mainContainer.visibility = View.GONE
            tvError = findViewById(R.id.tvError)
            tvError.visibility = View.GONE


        }

        override fun doInBackground(vararg params: String?): String? {
            return try {
                URL("https://api.openweathermap.org/data/2.5/weather?q=$CITY_NAME&units=metric&appid=$API")
                    .readText(Charsets.UTF_8)
            } catch (e: Exception) {
                null
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                val jsonObject = JSONObject(result)
                val main = jsonObject.getJSONObject("main")
                val sys = jsonObject.getJSONObject("sys")
                val wind = jsonObject.getJSONObject("wind")
                val weather = jsonObject.getJSONArray("weather").getJSONObject(0)
                val weatherDescription = weather.getString("description")
                val updatedAt: Long = jsonObject.getLong("dt")
                val updatedAtText = "Updated at: ${SimpleDateFormat("dd/MM/yyy hh:mm a", Locale.ENGLISH).format(Date(updatedAt * 1000))}"
                val temp = "${main.getString("temp").subSequence(0, 2)}°C"
                val tempMin = "Min ${main.getString("temp_min").subSequence(0, 2)}°C"
                val tempMax = "Max ${main.getString("temp_max").subSequence(0, 2)}°C"
                val pressure = main.getString("pressure")
                val humidity = main.getString("humidity")
                val sunrise: Long = sys.getLong("sunrise")
                val sunset: Long = sys.getLong("sunset")
                val windSpeed = wind.getString("speed")
                val address = "${jsonObject.getString("name")} ${sys.getString("country")}"

                findViewById<TextView>(R.id.tvAddress).text = address
                findViewById<TextView>(R.id.tvUpdate).text = updatedAtText
                findViewById<TextView>(R.id.tvStatus).text = weatherDescription.capitalize()
                changeBackground(weather.getString("main"))
                println("This is main: ${weather.getString("main")}")
                findViewById<TextView>(R.id.tvTemperature).text = temp
                findViewById<TextView>(R.id.tvMinTemperature).text = tempMin
                findViewById<TextView>(R.id.tvMaxTemperature).text = tempMax
                findViewById<TextView>(R.id.tvSunrise).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunrise * 1000))
                findViewById<TextView>(R.id.tvSunset).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunset * 1000))
                findViewById<TextView>(R.id.tvWind).text = "$windSpeed m/s"
                findViewById<TextView>(R.id.tvPressure).text = "$pressure hPa"
                findViewById<TextView>(R.id.tvHumidity).text = "$humidity%"

                progressBar.visibility = View.GONE
                mainContainer.visibility = View.VISIBLE
                tvError.visibility = View.GONE
            }
            catch (e: Exception) {
                progressBar.visibility = View.GONE
                tvError.visibility = View.VISIBLE
            }
        }
    }

    private fun changeBackground(description: String) {
        val makeChanges: (Drawable) -> Unit = {
            val changes = arrayOf(currentBackground, it)
            val transitionDrawable = TransitionDrawable(changes)
            superContainer.background = transitionDrawable
            transitionDrawable.startTransition(2000)
            currentBackground = it
        }
        when(description) {
            "Clear" -> ContextCompat.getDrawable(this, R.drawable.clearsky_bg)?.let { makeChanges(it) }
            "Clouds" -> ContextCompat.getDrawable(this, R.drawable.clouds_bg)?.let { makeChanges(it) }
            "Drizzle" -> ContextCompat.getDrawable(this, R.drawable.drizzle_bg)?.let { makeChanges(it) }
            "Rain" -> ContextCompat.getDrawable(this, R.drawable.rain_bg)?.let { makeChanges(it) }
            "Thunderstorm" -> ContextCompat.getDrawable(this, R.drawable.thunderstorm_bg)?.let { makeChanges(it) }
            "Snow" -> ContextCompat.getDrawable(this, R.drawable.snow_bg)?.let { makeChanges(it) }
            "Mist" -> ContextCompat.getDrawable(this, R.drawable.mist_bg)?.let { makeChanges(it) }
            else -> ContextCompat.getDrawable(this, R.drawable.gradient_bg)?.let { makeChanges(it) }
        }
    }
}