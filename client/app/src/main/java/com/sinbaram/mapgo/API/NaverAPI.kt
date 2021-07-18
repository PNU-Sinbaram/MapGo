package com.sinbaram.mapgo.API

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object NaverAPI {
    var BASE_URL : String = "https://naveropenapi.apigw.ntruss.com/"
    var retrofit : Retrofit? = null
    fun GetClient() : Retrofit? {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
        }
        return retrofit
    }
}