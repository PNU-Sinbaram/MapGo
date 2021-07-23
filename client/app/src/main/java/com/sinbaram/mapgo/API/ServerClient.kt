package com.sinbaram.mapgo.API

import com.sinbaram.mapgo.Model.Recommendation
import retrofit2.Call
import retrofit2.http.*

interface ServerClient {
    @Headers("Accept: application/json")
    @POST("/MapGo/checkin")
    fun CheckIn(
        @Field("User_ID") userID: String,
        @Field("lat") latitude: Float,
        @Field("long") longitude: Float
    ): Call<Void>

    @Headers("Accept: application/json")
    @GET("/MapGo/checkin")
    fun GetCheckIn(
        @Query("User_ID") userID: String,
        @Query("lat") latitude: Float,
        @Query("long") longitude: Float,
        @Query("timeStamp") timeStamp: String
    ): Call<Void>

    @Headers("Accept: application/json")
    @GET("/MapGo/recommend")
    fun GetRecommendations(
        @Query("User_ID") userID: String,
        @Query("lat") latitude: Float,
        @Query("long") longitude: Float,
        @Query("epsilon") epsilon: Int,
        @Query("keywords") keywords: String
    ): Call<List<Recommendation>>
}