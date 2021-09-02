package com.sinbaram.mapgo.API

import com.sinbaram.mapgo.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

/** Retrofit singleton instance for mapgo server post
 * ServerAPI's MoshiConverterFactory was not suitable as response
 * of PostFeedItem and String, so changed to Gson and Scalars each.
 **/
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
