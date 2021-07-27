package com.sinbaram.mapgo.API

import com.sinbaram.mapgo.BuildConfig
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.Retrofit

object ServerAPI {
    var BASE_URL: String = BuildConfig.SERVER_ADDRESS
    var retrofit: Retrofit? = null
    fun GetClient(): Retrofit? {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
        }
        return retrofit
    }
}