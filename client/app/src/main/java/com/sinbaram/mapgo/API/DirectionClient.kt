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
        @Header("X-NCP-APIGW-API-KEY-ID") id: String,
        @Header("X-NCP-APIGW-API-KEY") secret: String,
        @Query("start") start: String,
        @Query("goal") goal: String
    ): Call<Void>
}
