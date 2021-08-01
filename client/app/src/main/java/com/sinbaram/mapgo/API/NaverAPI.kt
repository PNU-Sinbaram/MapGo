package com.sinbaram.mapgo.API

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object NaverAPI {
    var NCLOUD_BASE_URL: String = "https://naveropenapi.apigw.ntruss.com/"
    var nCloudRetrofit: Retrofit? = null
    var OPENAPI_BASE_URL: String = "https://openapi.naver.com/"
    var openApiRetrofit: Retrofit? = null

    // Return ncloud naver api client
    fun GetClient(): Retrofit? {
        if (nCloudRetrofit == null) {
            nCloudRetrofit = Retrofit.Builder()
                .baseUrl(NCLOUD_BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
        }
        return nCloudRetrofit
    }

    // Return naver open api client
    fun GetOpenAPIClient(): Retrofit? {
        if (openApiRetrofit == null) {
            openApiRetrofit = Retrofit.Builder()
                .baseUrl(OPENAPI_BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
        }
        return openApiRetrofit
    }
}
