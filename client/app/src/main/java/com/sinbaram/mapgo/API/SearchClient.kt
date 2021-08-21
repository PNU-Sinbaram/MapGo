package com.sinbaram.mapgo.API

import com.sinbaram.mapgo.Model.BuildingQuery
import com.sinbaram.mapgo.Model.ImageQuery
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query

/** Collections of naver openapi search interface */
interface SearchClient {
    /**
     * Search buildings information with given keyword string
     * @return List of building information
     */
    @GET("/v1/search/local.json")
    fun SearchBuilding(
        @Query("query") query: String,
        @Query("display") displayCount: Int
    ): Call<BuildingQuery>

    /**
     * Search images with given informations
     * @return List of image information
     */
    @Headers("Accept: application/json")
    @GET("/v1/search/image.json")
    fun SearchImage(
        @Query("query") query: String,
        @Query("display") displayCount: Int,
        @Query("filter") filter: String
    ): Call<ImageQuery>
}
