package com.sinbaram.mapgo.API

import com.sinbaram.mapgo.Model.CheckInLog
import com.sinbaram.mapgo.Model.Recommendation
import retrofit2.Call
import retrofit2.http.*

interface ServerClient {
    @Headers("Accept: application/json")
    @POST("Mapgo/checkin/")
    @FormUrlEncoded
    fun CheckIn(
        @Field("User_ID") userID: String,
        @Field("lat") latitude: Float,
        @Field("long") longitude: Float
    ): Call<CheckInLog>

    @Headers("Accept: application/json")
    @GET("Mapgo/checkin/")
    fun GetCheckIn(): Call<List<CheckInLog>>

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