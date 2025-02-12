package com.example.new_assignment_1_version1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StopAdapter(
    private var stops: List<Stop>,
    private var distanceUnit: DistanceUnit,
    private var visitedStates: List<Boolean>
) : RecyclerView.Adapter<StopAdapter.StopViewHolder>() {

    class StopViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvStopTitle: TextView = itemView.findViewById(R.id.tvStopTitle)
        val tvStopDistance: TextView = itemView.findViewById(R.id.tvStopDistance)
        val tvStopVisa: TextView = itemView.findViewById(R.id.tvStopVisa)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_stop, parent, false)
        return StopViewHolder(view)
    }

    override fun onBindViewHolder(holder: StopViewHolder, position: Int) {
        val stop = stops[position]
        val visited = visitedStates[position]
        val titleText = if (visited) "${stop.label}: ${stop.name} (Visited)" else "${stop.label}: ${stop.name}"
        holder.tvStopTitle.text = titleText
        holder.tvStopDistance.text = "Distance: ${formatDistance(stop.distance, distanceUnit)}"
        holder.tvStopVisa.text = "Visa: ${stop.visaRequirement}"
    }

    override fun getItemCount(): Int = stops.size

    fun updateUnit(newUnit: DistanceUnit) {
        distanceUnit = newUnit
        notifyDataSetChanged()
    }

    fun updateVisitedStates(newVisited: List<Boolean>) {
        visitedStates = newVisited
        notifyDataSetChanged()
    }
}
