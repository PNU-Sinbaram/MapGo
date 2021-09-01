package com.sinbaram.mapgo.API

import com.sinbaram.mapgo.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object ServerPostAPI {
    var BASE_URL: String = BuildConfig.SERVER_ADDRESS
    var retrofit: Retrofit? = null
    fun GetSnsClient(): Retrofit? {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit
    }
}
