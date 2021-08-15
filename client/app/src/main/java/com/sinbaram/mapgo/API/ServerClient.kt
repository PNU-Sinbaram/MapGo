package com.sinbaram.mapgo.API

import com.sinbaram.mapgo.Model.CheckInResponse
import com.sinbaram.mapgo.Model.CheckInRequest
import com.sinbaram.mapgo.Model.Recommendation
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.Call

interface ServerClient {
    @Headers("Accept: application/json")
    @POST("Mapgo/checkin/")
    fun CheckIn(
        @Body request: CheckInRequest
    ): Call<CheckInResponse>

    @Headers("Accept: application/json")
    @GET("Mapgo/checkin/")
    fun GetCheckIn(): Call<List<CheckInResponse>>

    @Headers("Accept: application/json")
    @GET("Mapgo/recommend/")
    fun GetRecommendations(
        @Query("User_ID") userID: String,
        @Query("lat") latitude: Float,
        @Query("long") longitude: Float,
        @Query("epsilon") epsilon: Int,
        @Query("keywords") keywords: String
    ): Call<List<Recommendation>>
}
