package com.sinbaram.mapgo.API

import com.sinbaram.mapgo.Model.BuildingQuery
import com.sinbaram.mapgo.Model.ImageQuery
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query

interface SearchClient {
    /**
     * Search buildings information with given keyword string
     * @return List of building information
     */
    @Headers("Accept: application/json")
    @GET("/v1/search/local.json")
    fun SearchBuilding(
        @Header("X-Naver-Client-Id") id: String,
        @Header("X-Naver-Client-Secret") secret: String,
        @Query("query") query: String,
        @Query("display") displayCount: Int
    ): Call<BuildingQuery>

    /**
     * Search images with given informations
     * @return List of image information
     */
    @Headers("Accept: application/json")
    @GET("/v1/search/local.json")
    fun SearchImage(
        @Header("X-Naver-Client-Id") id: String,
        @Header("X-Naver-Client-Secret") secret: String,
        @Query("query") query: String,
        @Query("display") displayCount: Int,
        @Query("filter") filter: String
    ): Call<ImageQuery>
}
