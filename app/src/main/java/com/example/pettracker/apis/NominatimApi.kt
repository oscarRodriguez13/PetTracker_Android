package com.example.pettracker.apis

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NominatimService {
    @GET("reverse")
    fun reverseGeocode(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("format") format: String = "json"
    ): Call<String> // Usamos String en lugar de NominatimResponse
}

