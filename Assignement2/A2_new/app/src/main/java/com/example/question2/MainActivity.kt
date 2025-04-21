package com.example.question2

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var flightAdapter: FlightAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvLongestFlight: TextView
    private lateinit var tvShortestFlight: TextView
    private lateinit var tvOverallAverage: TextView
    private lateinit var tvFlightNumberAverages: TextView
    private lateinit var spinnerSource: Spinner
    private lateinit var spinnerDestination: Spinner

    // Currently selected source and destination for filtering.
    private var selectedSource: String = ""
    private var selectedDestination: String = ""

    // Spinner values.
    private val sources = listOf("JFK", "SFO", "ATL")
    private val destinations = listOf("LAX", "SEA", "ORD")

    // Mapping for prepopulation: Each route has 3 flight numbers.
    private val routeFlightNumbers = mapOf(
        Pair("JFK", "LAX") to listOf("1496", "5759", "2311"),
        Pair("JFK", "SEA") to listOf("23", "21", "4749"),
        Pair("JFK", "ORD") to listOf("7550", "2829", "532"),
        Pair("SFO", "LAX") to listOf("2487", "2319", "2163"),
        Pair("SFO", "SEA") to listOf("1225", "1059", "4205"),
        Pair("SFO", "ORD") to listOf("1837", "2614", "2418"),
        Pair("ATL", "LAX") to listOf("301", "1219", "4485"),
        Pair("ATL", "SEA") to listOf("481", "6839", "444"),
        Pair("ATL", "ORD") to listOf("4485", "4453", "6106")
    )

    // Define a formatter for date–time strings (always include seconds).
    val dtFormatter: DateTimeFormatter = DateTimeFormatterBuilder()
        .appendPattern("yyyy-MM-dd'T'HH:mm")
        .optionalStart()
        .appendPattern(":ss")
        .optionalEnd()
        .toFormatter()
    // Formatter for displaying times (HH:mm) and flight dates.
    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //testFlightFetchJob(applicationContext)
        setContentView(R.layout.activity_main)

        // Initialize views.
        spinnerSource = findViewById(R.id.spinnerSource)
        spinnerDestination = findViewById(R.id.spinnerDestination)
        tvLongestFlight = findViewById(R.id.tvLongestFlight)
        tvShortestFlight = findViewById(R.id.tvShortestFlight)
        tvOverallAverage = findViewById(R.id.tvOverallAverage)
        tvFlightNumberAverages = findViewById(R.id.tvFlightNumberAverages)
        recyclerView = findViewById(R.id.rvFlights)

        // Set up the RecyclerView.
        flightAdapter = FlightAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = flightAdapter

        // Setup spinners.
        val sourceAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sources)
        sourceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSource.adapter = sourceAdapter

        val destinationAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, destinations)
        destinationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDestination.adapter = destinationAdapter

        // Set spinner listeners to update filtering.
        spinnerSource.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                selectedSource = sources[position]
                queryHistoricalFlights()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        spinnerDestination.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                selectedDestination = destinations[position]
                queryHistoricalFlights()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Set default selections.
        selectedSource = sources.first()
        selectedDestination = destinations.first()

        // Prepopulate the database (clear and insert new flights) and then query.
        prepopulateFlightsIfNeeded {
            queryHistoricalFlights()
        }

        // Schedule background job for daily fetching.
        scheduleDailyFlightFetchJob(applicationContext)
        logAllFlightsFromDb()


    }

    fun testFlightFetchJob(context: Context) {
        Log.d("TestJob","Running test on Background Job")
        val testWorkRequest = OneTimeWorkRequestBuilder<FetchFlightsWorker>().build()
        WorkManager.getInstance(context).enqueue(testWorkRequest)

    }



    // Schedules a periodic work request to fetch flights daily.
    private fun scheduleDailyFlightFetchJob(context: Context) {
        val fetchFlightsWorkRequest = PeriodicWorkRequestBuilder<FetchFlightsWorker>(1, TimeUnit.DAYS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "FetchFlightsJob",
            ExistingPeriodicWorkPolicy.KEEP,
            fetchFlightsWorkRequest
        )
    }

    // Query the database for flights matching the selected source and destination.
    private fun queryHistoricalFlights() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = DatabaseProvider.getDatabase(applicationContext)
            val dao = db.flightDao()
            val flights = dao.getHistoricalFlights(selectedSource, selectedDestination)
            withContext(Dispatchers.Main) {
                flightAdapter.updateFlights(flights)
                updateFlightStatistics(flights)
            }
        }
    }

    // Update UI statistics: overall average, per-flight number averages, longest, and shortest durations.
    private fun updateFlightStatistics(flights: List<Flight>) {
        if (flights.isEmpty()) {
            tvLongestFlight.text = "Longest Flight: 0h 0min"
            tvShortestFlight.text = "Shortest Flight: 0h 0min"
            tvOverallAverage.text = "Overall Avg: 0h 0min"
            tvFlightNumberAverages.text = "Flight Number Averages:"
            return
        }

        // Compute overall durations.
        val durations = flights.map { flight ->
            val dep = LocalDateTime.parse(flight.actualDeparture, dtFormatter)
            val arr = LocalDateTime.parse(flight.actualArrival, dtFormatter)
            Duration.between(dep, arr)
        }
        val overallTotalSeconds = durations.fold(0L) { acc, d -> acc + d.seconds }
        val overallAverage = Duration.ofSeconds(overallTotalSeconds / flights.size.toLong())
        tvOverallAverage.text = "Overall Avg: ${formatDuration(overallAverage)}"

        // Group by flightNumber and compute average per flight number.
        val grouped = flights.groupBy { it.flightNumber }
        val averageMap = grouped.mapValues { entry ->
            val durs = entry.value.map { flight ->
                val dep = LocalDateTime.parse(flight.actualDeparture, dtFormatter)
                val arr = LocalDateTime.parse(flight.actualArrival, dtFormatter)
                Duration.between(dep, arr)
            }
            val totalSec = durs.fold(0L) { acc, dur -> acc + dur.seconds }
            Duration.ofSeconds(totalSec / durs.size.toLong())
        }
        val averagesText = averageMap.entries.joinToString("\n") { (num, dur) ->
            val hrs = dur.toHours()
            val mins = dur.minusHours(hrs).toMinutes()
            "Flight $num Avg: ${hrs}h ${if (mins < 10) "0$mins" else "$mins"}min"
        }
        tvFlightNumberAverages.text = "Flight Number Averages:\n$averagesText"

        // Determine longest and shortest durations.
        val longest = durations.maxOrNull() ?: Duration.ZERO
        val shortest = durations.minOrNull() ?: Duration.ZERO
        tvLongestFlight.text = "Longest Flight: ${formatDuration(longest)}"
        tvShortestFlight.text = "Shortest Flight: ${formatDuration(shortest)}"
    }

    // Helper function to format a Duration into a string.
    private fun formatDuration(duration: Duration): String {
        val hrs = duration.toHours()
        val mins = duration.minusHours(hrs).toMinutes()
        return "${hrs}h ${if (mins < 10) "0$mins" else "$mins"}min"
    }

    // Log all flights from the database (for debugging).
    private fun logAllFlightsFromDb() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Log.d("DB_LOG", "Logging all flights in database:")
                val db = DatabaseProvider.getDatabase(applicationContext)
                val flights = db.flightDao().getAllFlights()
                for (flight in flights) {
                    Log.d("DB_FLIGHTS", flight.toString())
                }
            } catch (e: Exception) {
                Log.e("logAllFlightsFromDb", "Error: ${e.message}", e)
            }
        }
    }

    // Prepopulate the database:
    // For each of the 9 routes, for each flight number (3 per route), insert 2 flights (total 6 flights per route).
    private fun prepopulateFlightsIfNeeded(onComplete: () -> Unit) {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = DatabaseProvider.getDatabase(applicationContext)
            val dao = db.flightDao()
            // Clear previous data.
            //dao.deleteAllFlights()

            val flightsToInsert = mutableListOf<Flight>()
            // Define 9 source-destination pairs.
            val pairs = listOf(
                Pair("JFK", "LAX"),
                Pair("JFK", "SEA"),
                Pair("JFK", "ORD"),
                Pair("SFO", "LAX"),
                Pair("SFO", "SEA"),
                Pair("SFO", "ORD"),
                Pair("ATL", "LAX"),
                Pair("ATL", "SEA"),
                Pair("ATL", "ORD")
            )
            // For each pair, get the list of flight numbers.
            for ((origin, destination) in pairs) {
                val flightNumbers = routeFlightNumbers[Pair(origin, destination)]
                if (flightNumbers != null) {
                    // For each flight number, insert 2 flights.
                    for (flightNumber in flightNumbers) {
                        repeat(2) {
                            // For simplicity, use today's date.
                            val flightDate = LocalDate.now().toString() // e.g., "2025-04-09"
                            val scheduledDeparture = "${flightDate}T10:00:00"
                            val scheduledArrival = "${flightDate}T13:00:00"  // 3-hour scheduled flight

                            // Random delays for departure and arrival (0 to 30 minutes).
                            val delayDep = (0..30).random().toLong()
                            val delayArr = (0..30).random().toLong()

                            // Compute actual times using our formatter to ensure seconds are included.
                            val actualDeparture = LocalDateTime.parse(scheduledDeparture, dtFormatter).plusMinutes(delayDep)
                            val actualArrival = LocalDateTime.parse(scheduledArrival, dtFormatter).plusMinutes(delayArr)

                            // Create a Flight object; format actual times using dtFormatter.
                            val flight = Flight(
                                flightNumber = flightNumber,
                                flightDate = flightDate,
                                origin = origin,
                                destination = destination,
                                scheduledDeparture = scheduledDeparture,
                                scheduledArrival = scheduledArrival,
                                actualDeparture = actualDeparture.format(dtFormatter),
                                actualArrival = actualArrival.format(dtFormatter)
                            )
                            flightsToInsert.add(flight)
                        }
                    }
                }
            }
            dao.insertFlights(flightsToInsert)
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }
}
