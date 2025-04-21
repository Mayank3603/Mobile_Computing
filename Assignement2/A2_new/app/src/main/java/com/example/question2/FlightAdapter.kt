package com.example.question2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class FlightAdapter(private var flights: List<Flight>) :
    RecyclerView.Adapter<FlightAdapter.FlightViewHolder>() {

    // Formatter for displaying times (HH:mm) and flight dates.
    // Assume the stored date strings conform to ISO_LOCAL_DATE_TIME like "2023-04-08T15:30:00"
    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    inner class FlightViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvFlightNumberAndDate: TextView = itemView.findViewById(R.id.tvFlightNumberAndDate)
        val tvFlightRoute: TextView = itemView.findViewById(R.id.tvFlightRoute)
        val tvFlightTimes: TextView = itemView.findViewById(R.id.tvFlightTimes)
        val tvFlightDuration: TextView = itemView.findViewById(R.id.tvFlightDuration)
        val tvArrivalDelay: TextView = itemView.findViewById(R.id.tvArrivalDelay)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlightViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_flight, parent, false)
        return FlightViewHolder(view)
    }

    override fun onBindViewHolder(holder: FlightViewHolder, position: Int) {
        val flight = flights[position]

        // Parse the flight date and times from their string representations.
        // Adjust the parsing if your stored format is different.
        val flightDate = LocalDateTime.parse(flight.flightDate + "T00:00:00").toLocalDate() // if flightDate is "2023-04-08"
        val actualDeparture = LocalDateTime.parse(flight.actualDeparture)
        val actualArrival = LocalDateTime.parse(flight.actualArrival)
        val scheduledDeparture = LocalDateTime.parse(flight.scheduledDeparture)
        val scheduledArrival = LocalDateTime.parse(flight.scheduledArrival)

        // Bind flight number and date.
        holder.tvFlightNumberAndDate.text = "Flight: ${flight.flightNumber} on ${flightDate.format(dateFormatter)}"

        // Bind route.
        holder.tvFlightRoute.text = "Route: ${flight.origin} -> ${flight.destination}"

        // Bind actual departure and arrival times.
        holder.tvFlightTimes.text = "Actual: ${actualDeparture.format(timeFormatter)} - ${actualArrival.format(timeFormatter)}"

        // Calculate duration using actual times.
        val duration: Duration = Duration.between(actualDeparture, actualArrival)
        holder.tvFlightDuration.text = "Duration: ${formatDuration(duration)}"

        // Calculate arrival delay.
        val arrivalDelay = getArrivalDelay(scheduledArrival, actualArrival)
        holder.tvArrivalDelay.text = if (arrivalDelay == Duration.ZERO) {
            "Delay: No delay"
        } else {
            "Delay: ${formatDuration(arrivalDelay)}"
        }
    }

    override fun getItemCount() = flights.size

    // Update the flight list.
    fun updateFlights(newFlights: List<Flight>) {
        flights = newFlights
        notifyDataSetChanged()
    }

    // Helper: compute arrival delay (only positive delays count).
    // Instead of operating on Flight, we'll pass the parsed scheduled and actual arrival times.
    private fun getArrivalDelay(scheduledArrival: LocalDateTime, actualArrival: LocalDateTime): Duration {
        return if (actualArrival.isAfter(scheduledArrival)) {
            Duration.between(scheduledArrival, actualArrival)
        } else {
            Duration.ZERO
        }
    }

    // Helper: format a Duration as hours and minutes.
    private fun formatDuration(duration: Duration): String {
        val hours = duration.toHours()
        val minutes = duration.minusHours(hours).toMinutes()
        return "${hours}h ${if (minutes < 10) "0$minutes" else "$minutes"}min"
    }
}
