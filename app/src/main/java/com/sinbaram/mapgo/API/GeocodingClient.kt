package com.sinbaram.mapgo.API

import retrofit2.Call
import retrofit2.http.*

interface GeocodingClient {
    @Headers("Accept: application/json")
    @GET("/map-geocode/v2/geocode")
    fun GetGeocode(
        @Header("X-NCP-APIGW-API-KEY-ID") id: String,
        @Header("X-NCP-APIGW-API-KEY") secret: String,
        @Query("query") query : String,
        @Query("coordinate") coordinate : String
    ): Call<Void>
}