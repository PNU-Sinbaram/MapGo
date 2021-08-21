package com.sinbaram.mapgo.API

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query

interface DirectionClient {
    @Headers("Accept: application/json")
    @GET("/map-direction/v1/driving")
    fun GetDirection(
        @Query("start") start: String,
        @Query("goal") goal: String
    ): Call<Void>
}
