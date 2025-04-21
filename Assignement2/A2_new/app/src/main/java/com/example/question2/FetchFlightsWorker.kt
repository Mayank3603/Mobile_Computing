package com.example.question2

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import android.util.Log

class FetchFlightsWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    // Create a simple OkHttp client.
    private val client = OkHttpClient()

    // Define your API access key (replace with your actual key).
    private val accessKey = "d23453db3e137e0ed2c428f3df0933ad"

    // Define a formatter to parse ISO date-time with offset.
    @RequiresApi(Build.VERSION_CODES.O)
    private val offsetFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {

        // Define the 9 route pairs.
        val routes = listOf(
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

        val db = DatabaseProvider.getDatabase(applicationContext)
        val dao = db.flightDao()

        // For each route, fetch 3 flights.
        for ((dep, arr) in routes) {
            // Construct the API URL for the given route.
            // This URL is based on AviationStack API; adjust parameters as needed.
            val url = "https://api.aviationstack.com/v1/flights?access_key=$accessKey&dep_iata=$dep&arr_iata=$arr&limit=3"
            val request = Request.Builder()
                .url(url)
                .build()


            try {

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val bodyString = response.body?.string()

                    if (bodyString != null) {
                        // Parse JSON response.
                        val json = JSONObject(bodyString)
                        val dataArray = json.getJSONArray("data")



                        for (i in 0 until dataArray.length()) {

                            val flightJson = dataArray.getJSONObject(i)

                            // Get basic flight information.
                            val flightDateStr = flightJson.getString("flight_date")  // Format: "YYYY-MM-DD"
                            val flightDate = LocalDate.parse(flightDateStr)
                            val flightObj = flightJson.getJSONObject("flight")
                            val flightNumber = flightObj.getString("number")

                            // Parse departure and arrival times from their respective JSON objects.
                            val departureObj = flightJson.getJSONObject("departure")
                            val arrivalObj = flightJson.getJSONObject("arrival")
                            Log.d("DepartureObject",departureObj.toString())
                            // Use scheduled times as a fallback if actual times are missing.
                            val scheduledDepartureStr = departureObj.getString("scheduled")
                            val scheduledArrivalStr = arrivalObj.getString("scheduled")
                            // Use the actual time if available; otherwise, fallback to the scheduled time.
                            val actualDepartureStr = departureObj.optString("actual", scheduledDepartureStr)
                            var actualArrivalStr = arrivalObj.optString("actual", scheduledArrivalStr)
                            if(actualArrivalStr==null){
                                actualArrivalStr=arrivalObj.optString("estimated",scheduledArrivalStr)
                            }
                            Log.d("Actual Arrival",actualArrivalStr)
                            // Parse the date-time strings using the offset formatter.
                            val scheduledDeparture = LocalDateTime.parse(scheduledDepartureStr, offsetFormatter)
                            val scheduledArrival = LocalDateTime.parse(scheduledArrivalStr, offsetFormatter)
                            val actualDeparture = try {
                                LocalDateTime.parse(actualDepartureStr, offsetFormatter)
                            } catch (e: Exception) {
                                scheduledDeparture // fallback if parsing fails
                            }
                            val actualArrival = try {
                                LocalDateTime.parse(actualArrivalStr, offsetFormatter)
                            } catch (e: Exception) {
                                scheduledArrival
                            }
                            Log.d("DataX","All data fetched using API")

                            // Create a Flight object (assuming your Flight entity has the fields as defined).
                            val flight = Flight(
                                flightNumber = flightNumber,
                                flightDate = flightDate.toString(),
                                origin = dep,
                                destination = arr,
                                scheduledDeparture = scheduledDeparture.toString(),
                                scheduledArrival = scheduledArrival.toString(),
                                actualDeparture = actualDeparture.toString(),
                                actualArrival = actualArrival.toString()
                            )
                            Log.d("Flight",flight.toString())

                            // Insert the flight into the database.
                            dao.insertFlight(flight)
                        }
                    }
                } else {
                    // Log or handle API error as needed.
                    return Result.failure()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return Result.failure()
            }
        }
        return Result.success()
    }
}
