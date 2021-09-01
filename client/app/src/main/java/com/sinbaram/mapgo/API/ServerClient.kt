package com.sinbaram.mapgo.API

import com.sinbaram.mapgo.Model.CheckInResponse
import com.sinbaram.mapgo.Model.CheckInRequest
import com.sinbaram.mapgo.Model.Recommendation
import com.sinbaram.mapgo.Model.PostFeedItem
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.Call

/** Collections of naver openapi search interface */
interface ServerClient {
    /** Send checkin post request to mapgo server */
    @Headers("Accept: application/json")
    @POST("Mapgo/checkin/")
    fun CheckIn(
        @Body request: CheckInRequest
    ): Call<CheckInResponse>

    /** Get checkin list for test purpose from mapgo server */
    @Headers("Accept: application/json")
    @GET("Mapgo/checkin/")
    fun GetCheckIn(): Call<List<CheckInResponse>>

    /** Get recommendation list from mapgo server */
    @Headers("Accept: application/json")
    @GET("Mapgo/recommend/")
    fun GetRecommendations(
        @Query("User_ID") userID: String,
        @Query("lat") latitude: Float,
        @Query("long") longitude: Float,
        @Query("epsilon") epsilon: Int,
        @Query("keywords") keywords: String
    ): Call<List<Recommendation>>

    @GET("Mapgo/sns/post/")
    fun GetPosts() : Call<List<PostFeedItem>>
}
