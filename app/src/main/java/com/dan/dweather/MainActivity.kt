package com.dan.dweather

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Exception

class MainActivity : AppCompatActivity() {
    val CITY_NAME = "Piedras Negras, MX"
    val API = "119bee8d190813f3a7ed9520af0e7e7d"
    lateinit var progressBar: ProgressBar
    lateinit var mainContainer: RelativeLayout
    lateinit var tvError: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        weatherTask().execute()
    }

    inner class weatherTask: AsyncTask<String, Unit, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
            progressBar = findViewById(R.id.loader)
            progressBar.visibility = View.VISIBLE
            mainContainer = findViewById(R.id.mainContainer)
            mainContainer.visibility = View.GONE
            tvError = findViewById(R.id.tvError)
            tvError.visibility = View.GONE
        }

        override fun doInBackground(vararg params: String?): String? {
            var response: String?
            response = try {
                URL("https://api.openweathermap.org/data/2.5/weather?q=$CITY_NAME&units=metric&appid=$API")
                    .readText(Charsets.UTF_8)
            } catch (e: Exception) {
                null
            }
            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                val jsonObject = JSONObject(result)
                val main = jsonObject.getJSONObject("main")
                val sys = jsonObject.getJSONObject("sys")
                val wind = jsonObject.getJSONObject("wind")
                val weather = jsonObject.getJSONArray("weather").getJSONObject(0)
                val updatedAt: Long = jsonObject.getLong("dt")
                val updatedAtText = "Updated at: ${SimpleDateFormat("dd/MM/yyy hh:mm a", Locale.ENGLISH).format(Date(updatedAt*1000))}"
                val temp = "${main.getString("temp").subSequence(0,2)}°C"
                val tempMin = "Min ${main.getString("temp_min").subSequence(0,2)}°C"
                val tempMax = "Max ${main.getString("temp_max").subSequence(0,2)}°C"
                val pressure = main.getString("pressure")
                val humidity = main.getString("humidity")
                val sunrise: Long = sys.getLong("sunrise")
                val sunset: Long = sys.getLong("sunset")
                val windSpeed = wind.getString("speed")
                val weatherDescription = weather.getString("description")
                val address = "${jsonObject.getString("name")} ${sys.getString("country")}"

                findViewById<TextView>(R.id.tvAddress).text = address
                findViewById<TextView>(R.id.tvUpdate).text = updatedAtText
                findViewById<TextView>(R.id.tvStatus).text = weatherDescription.capitalize()
                findViewById<TextView>(R.id.tvTemperature).text = temp
                findViewById<TextView>(R.id.tvMinTemperature).text = tempMin
                findViewById<TextView>(R.id.tvMaxTemperature).text = tempMax
                findViewById<TextView>(R.id.tvSunrise).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunrise*1000))
                findViewById<TextView>(R.id.tvSunset).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunset*1000))
                findViewById<TextView>(R.id.tvWind).text = windSpeed
                findViewById<TextView>(R.id.tvPressure).text = pressure
                findViewById<TextView>(R.id.tvHumidity).text = humidity

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
}