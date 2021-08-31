package com.sinbaram.mapgo.API

import com.sinbaram.mapgo.Model.ReverseGeocodingModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query

interface ReverseGeocodingClient {
    @Headers("Accept: application/json")
    @GET("/map-reversegeocode/v2/gc")
    fun GetReverseGeocode(
        @Query("coords") coords: String
    ): Call<ReverseGeocodingModel>
}
