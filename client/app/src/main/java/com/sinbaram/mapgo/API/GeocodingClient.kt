package com.sinbaram.mapgo.API

import com.sinbaram.mapgo.Model.GeocodingModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface GeocodingClient {
    @Headers("Accept: application/json")
    @GET("/map-geocode/v2/geocode")
    fun GetGeocode(
        @Query("query", encoded=true) query: String,
    ): Call<GeocodingModel>
}
