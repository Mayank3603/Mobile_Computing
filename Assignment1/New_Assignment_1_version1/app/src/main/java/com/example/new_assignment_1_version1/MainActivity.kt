package com.example.new_assignment_1_version1

import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

enum class DistanceUnit {
    Kilometers,
    Miles
}

fun loadStops(context: android.content.Context): List<Stop> {
    val stops = mutableListOf<Stop>()
    val inputStream = context.resources.openRawResource(R.raw.testcase)
    inputStream.bufferedReader().forEachLine { line ->
        val parts = line.split(": ")
        if (parts.size >= 2) {
            val label = parts[0].trim()
            val details = parts[1].split(" - ")
            if (details.size >= 3) {
                val name = details[0].trim()
                val distance = details[1].trim().toIntOrNull() ?: 0
                val visaRequirement = details[2].trim()
                stops.add(Stop(label, name, distance, visaRequirement))
            }
        }
    }
    return stops
}

fun formatDistance(distance: Int, unit: DistanceUnit): String {
    return when (unit) {
        DistanceUnit.Kilometers -> "$distance km"
        DistanceUnit.Miles -> "${(distance * 0.621371).toInt()} mi"
    }
}

fun formatTime(timeInHours: Double): String {
    val hours = timeInHours.toInt()
    val minutes = ((timeInHours - hours) * 60).toInt()
    return "$hours hrs $minutes mins"
}

class MainActivity : AppCompatActivity() {

    private lateinit var tvTotalStops: TextView
    private lateinit var tvTotalDistance: TextView
    private lateinit var tvTotalTime: TextView
    private lateinit var tvCurrentStop: TextView
    private lateinit var tvCurrentName: TextView
    private lateinit var tvCurrentVisa: TextView
    private lateinit var tvDistLeft: TextView
    private lateinit var tvTimeLeft: TextView
    private lateinit var tvNextDist: TextView
    private lateinit var tvNextTime: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var rvStops: RecyclerView
    private lateinit var btnSwitchUnits: Button
    private lateinit var btnNextStop: Button
    private lateinit var btnReset: Button

    private lateinit var stops: List<Stop>
    private var progressDone = 0
    private var distanceUnit = DistanceUnit.Kilometers
    private var visitedStates = mutableListOf<Boolean>()

    private val SPEED_KMPH = 800.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvTotalStops = findViewById(R.id.tvTotalStops)
        tvTotalDistance = findViewById(R.id.tvTotalDistance)
        tvTotalTime = findViewById(R.id.tvTotalTime)
        tvCurrentStop = findViewById(R.id.tvCurrentStop)
        tvCurrentName = findViewById(R.id.tvCurrentName)
        tvCurrentVisa = findViewById(R.id.tvCurrentVisa)
        tvDistLeft = findViewById(R.id.tvDistLeft)
        tvTimeLeft = findViewById(R.id.tvTimeLeft)
        tvNextDist = findViewById(R.id.tvNextDist)
        tvNextTime = findViewById(R.id.tvNextTime)
        progressBar = findViewById(R.id.progressBar)
        rvStops = findViewById(R.id.rvStops)
        btnSwitchUnits = findViewById(R.id.btnSwitchUnits)
        btnNextStop = findViewById(R.id.btnNextStop)
        btnReset = findViewById(R.id.btnReset)

        stops = loadStops(this)
        visitedStates = MutableList(stops.size) { false }

        rvStops.layoutManager = LinearLayoutManager(this)
        val adapter = StopAdapter(stops, distanceUnit, visitedStates)
        rvStops.adapter = adapter

        btnSwitchUnits.setOnClickListener {
            distanceUnit = if (distanceUnit == DistanceUnit.Kilometers) DistanceUnit.Miles else DistanceUnit.Kilometers
            updateUI()
            adapter.updateUnit(distanceUnit)
        }
        btnNextStop.setOnClickListener {
            if (progressDone < stops.size - 1) {
                visitedStates[progressDone + 1] = true
                progressDone++
                updateUI()
                adapter.notifyDataSetChanged()
            }
        }
        btnReset.setOnClickListener {
            progressDone = 0
            visitedStates = MutableList(stops.size) { false }
            updateUI()
            adapter.updateVisitedStates(visitedStates)
            adapter.notifyDataSetChanged()
        }

        updateUI()
    }

    private fun updateUI() {
        val totalDistance = if (stops.size > 1) stops.drop(1).sumOf { it.distance } else 0

        val visitedDistance = if (stops.size > 1) stops.drop(1).take(progressDone).sumOf { it.distance } else 0

        val distanceLeft = totalDistance - visitedDistance
        val totalTimeLeft = distanceLeft / SPEED_KMPH
        val totalTime = totalDistance / SPEED_KMPH
        val nextLegDistance = if (progressDone < stops.size - 1) stops[progressDone + 1].distance else 0
        val nextLegTime = nextLegDistance / SPEED_KMPH

        tvTotalStops.text = "Total Stops: ${stops.size}"
        tvTotalDistance.text = "Total Distance: ${formatDistance(totalDistance, distanceUnit)}"
        tvTotalTime.text = "Total Time: ${formatTime(totalTime)}"

        if (progressDone < stops.size) {
            val current = stops[progressDone]
            tvCurrentStop.text = "Current Stop: ${current.label}"
            tvCurrentName.text = "Name: ${current.name}"
            tvCurrentVisa.text = "Visa: ${current.visaRequirement}"
        } else {
            tvCurrentStop.text = "Journey Complete!"
            tvCurrentName.text = ""
            tvCurrentVisa.text = ""
        }

        tvDistLeft.text = "Dist Left: ${formatDistance(distanceLeft, distanceUnit)}"
        tvTimeLeft.text = "Time Left: ${formatTime(totalTimeLeft)}"
        tvNextDist.text = "Next Dist: ${formatDistance(nextLegDistance, distanceUnit)}"
        tvNextTime.text = "Next Time: ${formatTime(nextLegTime)}"

        val progress = if (stops.size > 1) (progressDone.toFloat() / (stops.size - 1)) else 1f
        progressBar.progress = (progress * 100).toInt()
    }
}
