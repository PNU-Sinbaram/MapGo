package com.sinbaram.mapgo.API

import com.sinbaram.mapgo.BuildConfig
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.Retrofit
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit


/** Retrofit2 singleton instance for mapgo server */
object ServerAPI {
    var BASE_URL: String = BuildConfig.SERVER_ADDRESS
    var retrofit: Retrofit? = null
    fun GetClient(): Retrofit? {
        if (retrofit == null) {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build()
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create())
                .client(okHttpClient)
                .build()
        }
        return retrofit
    }
}
