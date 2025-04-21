package com.example.a2_1_second_trial

import java.time.LocalDateTime

import android.content.Intent
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.a2_1_second_trial.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

class FlightDetailsActivity : AppCompatActivity() {

    private lateinit var tvFlightNumber: TextView
    private lateinit var tvDepartureDetails: TextView
    private lateinit var tvArrivalDetails: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTimeLeft: TextView
    private lateinit var tvCurrentLatitude: TextView
    private lateinit var tvCurrentLongitude: TextView
//    private lateinit var btnShowMap: Button

    // Your API access key
    private val ACCESS_KEY = "362eed0c2d233181458ca8d329caa1a1"

    // Handler to update UI and fetch API periodically
    private val updateHandler = Handler(Looper.getMainLooper())
    private lateinit var uiUpdateRunnable: Runnable
    private lateinit var apiFetchRunnable: Runnable

    // Store flight data for updating UI
    private var currentFlightData: FlightData? = null

    // Variables to store geocoded coordinates for departure and arrival
    private var departureCoordinates: Pair<Double, Double>? = null
    private var arrivalCoordinates: Pair<Double, Double>? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flight_details)

        // Initialize UI elements
        tvFlightNumber = findViewById(R.id.tvFlightNumber)
        tvDepartureDetails = findViewById(R.id.tvDepartureDetails)
        tvArrivalDetails = findViewById(R.id.tvArrivalDetails)
        tvStatus = findViewById(R.id.tvStatus)
        tvCurrentTime = findViewById(R.id.tvCurrentTime)
        tvTimeLeft = findViewById(R.id.tvTimeLeft)
        tvCurrentLatitude = findViewById(R.id.tvCurrentLatitude)
        tvCurrentLongitude = findViewById(R.id.tvCurrentLongitude)
//        btnShowMap = findViewById(R.id.btnShowMap)

        // Get flight number from intent
        val flightNumber = intent.getStringExtra("flightNumber") ?: ""
        if (flightNumber.isEmpty()) {
            Toast.makeText(this, "Invalid flight number.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        tvFlightNumber.text = "Flight Number: $flightNumber"

        // Fetch flight details initially and then start periodic updates
        fetchFlightData(flightNumber)

        // Set up button click to launch FlightMapActivity
//        btnShowMap.setOnClickListener {
//            if (currentFlightData != null) {
//                val intent = Intent(this, FlightMapActivity::class.java)
//                // Pass departure coordinates if available
//                departureCoordinates?.let {
//                    intent.putExtra("departureLat", it.first)
//                    intent.putExtra("departureLon", it.second)
//                }
//                // Pass arrival coordinates if available
//                arrivalCoordinates?.let {
//                    intent.putExtra("arrivalLat", it.first)
//                    intent.putExtra("arrivalLon", it.second)
//                }
//                // Pass live coordinates if available
//                currentFlightData?.live?.let { live ->
//                    intent.putExtra("liveLat", live.latitude)
//                    intent.putExtra("liveLon", live.longitude)
//                }
//                startActivity(intent)
//            } else {
//                Toast.makeText(this, "Flight data not available to show map.", Toast.LENGTH_SHORT).show()
//            }
//        }

        // Start periodic UI updates every second
        startUIUpdates()
        // Start periodic API fetch every 10 seconds
        startApiFetchUpdates(flightNumber)
    }

    /**
     * Fetch flight data from the API and update currentFlightData.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchFlightData(flightNumber: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.getFlightDetails(
                    accessKey = ACCESS_KEY,
                    flightNumber = flightNumber,
                    flightStatus = "active"
                )
                if (response.data.isNotEmpty()) {
                    val flightData = response.data[0]
                    currentFlightData = flightData
                    // Geocode departure and arrival airport names
                    departureCoordinates = geocodeLocation(flightData.departure.airport)
                    arrivalCoordinates = geocodeLocation(flightData.arrival.airport)
                    withContext(Dispatchers.Main) {
                        updateUI(flightData)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@FlightDetailsActivity, "No active flight data found.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("FlightDetails", "Error fetching flight details", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@FlightDetailsActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /**
     * Helper function to geocode a location name into latitude and longitude.
     */
    private suspend fun geocodeLocation(locationName: String): Pair<Double, Double>? {
        return try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocationName(locationName, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                Pair(addresses[0].latitude, addresses[0].longitude)
            } else null
        } catch (e: Exception) {
            Log.e("Geocoding", "Error geocoding location: $locationName", e)
            null
        }
    }

    /**
     * Update the UI with flight details, including departure/arrival info,
     * status, current time, time left until arrival, and live coordinates.
     * Also appends the geocoded coordinates for departure and arrival.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateUI(flightData: FlightData) {
        Log.d("FlightDetails", "Fetched live coordinates: lat=${flightData.live?.latitude}, lon=${flightData.live?.longitude}")
        tvFlightNumber.text = "Flight Number: ${flightData.flight.number}"
        tvDepartureDetails.text = if (departureCoordinates != null) {
            "Airport: ${flightData.departure.airport} (Lat: ${"%.4f".format(departureCoordinates!!.first)}, Lon: ${"%.4f".format(departureCoordinates!!.second)})\nScheduled: ${flightData.departure.scheduled}"
        } else {
            "Airport: ${flightData.departure.airport}\nScheduled: ${flightData.departure.scheduled}"
        }
        tvArrivalDetails.text = if (arrivalCoordinates != null) {
            "Airport: ${flightData.arrival.airport} (Lat: ${"%.4f".format(arrivalCoordinates!!.first)}, Lon: ${"%.4f".format(arrivalCoordinates!!.second)})\nScheduled: ${flightData.arrival.scheduled}"
        } else {
            "Airport: ${flightData.arrival.airport}\nScheduled: ${flightData.arrival.scheduled}"
        }
        tvStatus.text = flightData.flight_status.capitalize()

        // Format and update current time to show only date and time
        val now = Instant.now()
        val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm").withZone(ZoneId.systemDefault())
        tvCurrentTime.text = "Current Time: ${formatter.format(now)}"

        try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val arrivalTime = LocalDateTime.parse(flightData.arrival.scheduled, formatter)
            val arrivalInstant = arrivalTime.atZone(ZoneId.of("UTC")).toInstant()
            val duration = Duration.between(now, arrivalInstant)

            tvTimeLeft.text = if (!duration.isNegative) {
                "Time Left: ${duration.toMinutes()} minutes"
            } else {
                "Flight has arrived"
            }
        } catch (e: Exception) {
            tvTimeLeft.text = "Invalid arrival time format"
        }

        // Display the current live latitude and longitude if available
        if (flightData.live?.latitude != null && flightData.live?.longitude != null) {
            tvCurrentLatitude.text = "Current Latitude: ${flightData.live.latitude}"
            tvCurrentLongitude.text = "Current Longitude: ${flightData.live.longitude}"
        } else {
            tvCurrentLatitude.text = "Current Latitude: N/A"
            tvCurrentLongitude.text = "Current Longitude: N/A"
        }
    }

    /**
     * Start periodic UI updates every second.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startUIUpdates() {
        uiUpdateRunnable = object : Runnable {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun run() {
                currentFlightData?.let { updateUI(it) }
                updateHandler.postDelayed(this, 1000)
            }
        }
        updateHandler.post(uiUpdateRunnable)
    }

    /**
     * Start periodic API fetch updates every 10 seconds.
     */
    private fun startApiFetchUpdates(flightNumber: String) {
        apiFetchRunnable = object : Runnable {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun run() {
                fetchFlightData(flightNumber)
                updateHandler.postDelayed(this, 10000)
            }
        }
        updateHandler.post(apiFetchRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        updateHandler.removeCallbacks(uiUpdateRunnable)
        updateHandler.removeCallbacks(apiFetchRunnable)
    }
}