package com.sinbaram.mapgo.API

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query

interface GeocodingClient {
    @Headers("Accept: application/json")
    @GET("/map-geocode/v2/geocode")
    fun GetGeocode(
        @Query("query") query: String,
        @Query("coordinate") coordinate: String
    ): Call<Void>
}
