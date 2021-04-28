package com.dan.dweather

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.squareup.picasso.Picasso
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private val API = "119bee8d190813f3a7ed9520af0e7e7d"
    private lateinit var progressBar: ProgressBar
    private lateinit var mainContainer: RelativeLayout
    private lateinit var tvError: TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var currentBackground: Drawable
    private lateinit var superContainer: RelativeLayout
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val LOCATION_REQUEST_CODE = 10001
    private lateinit var last: LatLng
    private lateinit var ivWeather: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        superContainer = findViewById(R.id.superContainer)

        currentBackground = ContextCompat.getDrawable(this, R.drawable.gradient_bg)!!

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        ivWeather = findViewById(R.id.ivWeather)

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            begin()
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun begin() {
        getLastLocation()
        weatherTask().execute()
    }

    override fun onStart() {
        super.onStart()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) getLastLocation() else askPermission()
        begin()
    }

    inner class weatherTask: AsyncTask<String, Unit, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
            progressBar = findViewById(R.id.loader)
            progressBar.visibility = View.VISIBLE
            mainContainer = findViewById(R.id.mainContainer)
            tvError = findViewById(R.id.tvError)
            tvError.visibility = View.GONE
        }

        override fun doInBackground(vararg params: String?): String? {
            return try {
                URL("https://api.openweathermap.org/data/2.5/weather?lat=${last.latitude}&lon=${last.longitude}&units=metric&appid=$API")
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
                begin()
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
            "Clear" -> {
                ContextCompat.getDrawable(this, R.drawable.clearsky_bg)?.let { makeChanges(it) }
                Picasso.with(this).load("https://cdn0.iconfinder.com/data/icons/weather-web-app-ui/100/weather-22-256.png").into(ivWeather)
            }
            "Clouds" -> {
                ContextCompat.getDrawable(this, R.drawable.clouds_bg)?.let { makeChanges(it) }
                Picasso.with(this).load("https://cdn2.iconfinder.com/data/icons/weather-color-2/500/weather-22-256.png").into(ivWeather)
            }
            "Drizzle" -> {
                ContextCompat.getDrawable(this, R.drawable.drizzle_bg)?.let { makeChanges(it) }
                Picasso.with(this).load("https://cdn3.iconfinder.com/data/icons/weather-ios-11-1/50/Night_Drizzle_Rain_Raindrops_Apple_iOS_Flat_Weather-256.png").into(ivWeather)
            }
            "Rain" -> {
                ContextCompat.getDrawable(this, R.drawable.rain_bg)?.let { makeChanges(it) }
                Picasso.with(this).load("https://cdn1.iconfinder.com/data/icons/interface-travel-and-environment/64/rain-umbrella-forecast-weather-protection-256.png").into(ivWeather)
            }
            "Thunderstorm" -> {
                ContextCompat.getDrawable(this, R.drawable.thunderstorm_bg)?.let { makeChanges(it) }
                Picasso.with(this).load("https://cdn1.iconfinder.com/data/icons/the-port-flat-lighthouse-and-young-sailor/512/thunder_storm-256.png").into(ivWeather)
            }
            "Snow" -> {
                ContextCompat.getDrawable(this, R.drawable.snow_bg)?.let { makeChanges(it) }
                Picasso.with(this).load("https://cdn0.iconfinder.com/data/icons/merry-christmas-41/512/13-snowflake-winter-snow-256.png").into(ivWeather)
            }
            "Mist" -> {
                ContextCompat.getDrawable(this, R.drawable.mist_bg)?.let { makeChanges(it) }
                Picasso.with(this).load("https://cdn4.iconfinder.com/data/icons/the-weather-is-nice-today/64/weather_30-256.png").into(ivWeather)
            }
            else -> {
                ContextCompat.getDrawable(this, R.drawable.gradient_bg)?.let { makeChanges(it) }
                Picasso.with(this).load("https://cdn1.iconfinder.com/data/icons/ui-set-6/100/Question_Mark-256.png").into(ivWeather)
            }
        }
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        val location: Task<Location> = fusedLocationProviderClient.lastLocation
        location.addOnSuccessListener {
            last = LatLng(it.latitude, it.longitude)
        }
        location.addOnFailureListener {
            Toast.makeText(this, "There was a problem with the location", Toast.LENGTH_SHORT).show()
        }
    }

    private fun askPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
            else
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == LOCATION_REQUEST_CODE)
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                getLastLocation()
    }
}