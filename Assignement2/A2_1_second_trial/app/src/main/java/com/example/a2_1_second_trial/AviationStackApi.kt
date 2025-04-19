package com.example.a2_1_second_trial


import retrofit2.http.GET
import retrofit2.http.Query

interface AviationStackApi {
    @GET("flights")
    suspend fun getFlightDetails(
        @Query("access_key") accessKey: String,
        @Query("flight_number") flightNumber: String,
        @Query("flight_status") flightStatus: String
    ): AviationStackResponse
}