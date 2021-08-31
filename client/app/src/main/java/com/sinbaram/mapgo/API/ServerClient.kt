package com.sinbaram.mapgo.API

import com.sinbaram.mapgo.Model.CheckInResponse
import com.sinbaram.mapgo.Model.CheckInRequest
import com.sinbaram.mapgo.Model.PostFeedItem
import com.sinbaram.mapgo.Model.Comment
import com.sinbaram.mapgo.Model.RecommendationModel
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Path
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.HTTP
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
    ): Call<List<RecommendationModel>>
    /** Get posts from mapgo server */
    @GET("Mapgo/sns/post/")
    fun GetPosts() : Call<List<PostFeedItem>>

    /** Get comments from post with {id} */
    @GET("Mapgo/sns/post/{id}/comment/")
    fun GetComments(
        @Path("id") id: Int
    ) : Call<List<Comment>>

    /** Post comment to post with {id} */
    @Multipart
    @POST("Mapgo/sns/post/{id}/comment/")
    fun PostComment(
        @Path("id") id: Int,
        @Part("writer") writer: Int,
        @Part("contents") contents: String
    ) : Call<String>

    /** add like to post with {id}, as userID */
    @Multipart
    @POST("Mapgo/sns/post/{id}/like/")
    fun AddLike(
        @Path("id") id: Int,
        @Part("userID") userID: Int
    ) : Call<String>

    /** remove like to post with {id}, as userID */
    @Multipart
    @HTTP(method="DELETE", hasBody=true, path="Mapgo/sns/post/{id}/like/")
    fun DeleteLike(
        @Path("id") id: Int,
        @Part("userID") userID: Int
    ) : Call<String>

}
