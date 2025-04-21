package com.example.question2

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "flight",
    indices = [Index(value = ["flightNumber", "flightDate"], unique = true)]
)
data class Flight(
    @PrimaryKey(autoGenerate = true)
    val flightId: Int = 0,
    val flightNumber: String,
    val flightDate: String,  // ISO string, e.g. "2025-04-09"
    val origin: String,
    val destination: String,
    val scheduledDeparture: String, // ISO datetime string
    val scheduledArrival: String,
    val actualDeparture: String,
    val actualArrival: String
)
