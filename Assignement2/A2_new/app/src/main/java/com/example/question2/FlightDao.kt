package com.example.question2

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FlightDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertFlight(flight: Flight):Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertFlights(flights: List<Flight>):List<Long>

    @Query("SELECT * FROM flight WHERE origin = :origin AND destination = :destination ORDER BY flightDate DESC")
    fun getHistoricalFlights(origin: String, destination: String): List<Flight>

    @Query("SELECT * FROM flight")
    fun getAllFlights(): List<Flight>

    @Query("DELETE FROM flight")
    fun deleteAllFlights()
}
