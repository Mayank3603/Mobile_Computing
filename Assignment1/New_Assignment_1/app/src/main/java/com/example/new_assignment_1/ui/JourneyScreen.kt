package com.example.new_assignment_1

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val SPEED_KMPH = 800.0

data class Stop(
    val label: String,
    val name: String,
    val distance: Int,
    val visaRequirement: String
)

fun loadStops(context: Context): List<Stop> {
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

enum class DistanceUnit {
    Kilometers,
    Miles
}

// ----------------------------------------------------------------------------------
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

// ----------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JourneyApp() {
    val context = LocalContext.current
    // Load stops from the text file.
    val stops = remember { loadStops(context) }
    val totalStops = stops.size

    val totalDistance = if (stops.size > 1) stops.drop(1).sumOf { it.distance } else 0

    var progressDone by remember { mutableIntStateOf(0) }
    var distanceUnit by remember { mutableStateOf(DistanceUnit.Kilometers) }
    var visitedStates by remember { mutableStateOf(List(totalStops) { false }) }

    val visitedDistance = if (stops.size > 1) stops.drop(1).take(progressDone).sumOf { it.distance } else 0
    val distanceLeft = totalDistance - visitedDistance
    val totalTimeLeft = distanceLeft / SPEED_KMPH
    val totalTime = totalDistance / SPEED_KMPH
    val nextLegDistance = if (progressDone < stops.size - 1) stops[progressDone + 1].distance else 0
    val nextLegTime = nextLegDistance / SPEED_KMPH

    val currentStop: Stop? = if (progressDone < stops.size) stops[progressDone] else null

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "Journey Details", color = Color.White)
                },
                navigationIcon = {
                    IconButton(onClick = { /* Navigation action if needed */ }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            HeaderSection(
                totalStops = totalStops,
                progressDone = progressDone,
                distanceUnit = distanceUnit,
                totalDistance = totalDistance,
                totalTime = totalTime,
                distanceLeft = distanceLeft,
                totalTimeLeft = totalTimeLeft,
                nextLegDistance = nextLegDistance,
                nextLegTime = nextLegTime,
                currentStop = currentStop
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (totalStops > 3) {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(stops) { stop ->
                        val index = stops.indexOf(stop)
                        StopItem(
                            stop = stop,
                            distanceUnit = distanceUnit,
                            visited = visitedStates[index]
                        )
                    }
                }
            } else {
                Column(modifier = Modifier.weight(1f)) {
                    stops.forEachIndexed { index, stop ->
                        StopItem(
                            stop = stop,
                            distanceUnit = distanceUnit,
                            visited = visitedStates[index]
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        distanceUnit = when (distanceUnit) {
                            DistanceUnit.Kilometers -> DistanceUnit.Miles
                            DistanceUnit.Miles -> DistanceUnit.Kilometers
                        }
                    }
                ) {
                    Text(text = "Switch Units", maxLines = 1)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (progressDone < stops.size - 1) {
                            visitedStates = visitedStates.toMutableList().also {
                                it[progressDone + 1] = true
                            }
                            progressDone++
                        }
                    }
                ) {
                    Text(text = "Next Stop", maxLines = 1)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        progressDone = 0
                        visitedStates = List(totalStops) { false }
                    }
                ) {
                    Text(text = "Reset", maxLines = 1)
                }
            }
        }
    }
}


@Composable
fun HeaderSection(
    totalStops: Int,
    progressDone: Int,
    distanceUnit: DistanceUnit,
    totalDistance: Int,
    totalTime: Double,
    distanceLeft: Int,
    totalTimeLeft: Double,
    nextLegDistance: Int,
    nextLegTime: Double,
    currentStop: Stop?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF93C7F0) // Light pink header box
        )
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Total Stops: $totalStops",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black
                    )
                    Text(
                        text = "Total Distance: ${formatDistance(totalDistance, distanceUnit)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black
                    )
                    Text(
                        text = "Total Time: ${formatTime(totalTime)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    if (currentStop != null) {
                        Text(
                            text = "Current Stop: ${currentStop.label}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Black
                        )
                        Text(
                            text = "Name: ${currentStop.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Black
                        )
                        Text(
                            text = "Visa: ${currentStop.visaRequirement}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Black
                        )
                    } else {
                        Text(
                            text = "Journey Complete!",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Green
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Dist Left: ${formatDistance(distanceLeft, distanceUnit)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )
                Text(
                    text = "Time Left: ${formatTime(totalTimeLeft)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )
                Text(
                    text = "Next Dist: ${formatDistance(nextLegDistance, distanceUnit)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )
                Text(
                    text = "Next Time: ${formatTime(nextLegTime)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            val progressBarColor = if (currentStop == null) Color(0xFF006400) else Color(0xFFB1B4B2)
            LinearProgressIndicator(
                progress = if (totalStops > 1) progressDone.toFloat() / (totalStops - 1) else 1f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = progressBarColor,
                trackColor = Color.LightGray
            )
        }
    }
}

// ----------------------------------------------------------------------------------

@Composable
fun StopItem(stop: Stop, distanceUnit: DistanceUnit, visited: Boolean) {
    val formattedDistance = when (distanceUnit) {
        DistanceUnit.Kilometers -> "${stop.distance} km"
        DistanceUnit.Miles -> "${(stop.distance * 0.621371).toInt()} mi"
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (visited)
                    "${stop.label}: ${stop.name} (Visited)"
                else
                    "${stop.label}: ${stop.name}",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                color = if (visited) Color(0xFF333333) else Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Distance: $formattedDistance",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Visa: ${stop.visaRequirement}",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                color = Color.Black
            )
        }
    }
}
