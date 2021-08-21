package com.sinbaram.mapgo.API

import com.sinbaram.mapgo.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/** Retrofit2 singleton instance for naver api */
object NaverAPI {
    var NCLOUD_BASE_URL: String = "https://naveropenapi.apigw.ntruss.com/"
    var nCloudRetrofit: Retrofit? = null
    var OPENAPI_BASE_URL: String = "https://openapi.naver.com/"
    var openApiRetrofit: Retrofit? = null

    // Return ncloud naver api client
    fun GetClient(): Retrofit? {
        if (nCloudRetrofit == null) {
            val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val headerInterceptor = Interceptor {
                val request = it.request()
                    .newBuilder()
                    .addHeader("X-NCP-APIGW-API-KEY-ID", BuildConfig.NAVER_KEY_ID)
                    .addHeader("X-NCP-APIGW-API-KEY", BuildConfig.NAVER_SECRET_KEY)
                    .build()
                return@Interceptor it.proceed(request)
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(headerInterceptor)
                .addInterceptor(httpLoggingInterceptor)
                .build()

            nCloudRetrofit = Retrofit.Builder()
                .baseUrl(NCLOUD_BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
        }
        return nCloudRetrofit
    }

    // Return naver open api client
    fun GetOpenAPIClient(): Retrofit? {
        if (openApiRetrofit == null) {
            val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val headerInterceptor = Interceptor {
                val request = it.request()
                    .newBuilder()
                    .addHeader("X-Naver-Client-Id", BuildConfig.NAVER_OPEN_KEY_ID)
                    .addHeader("X-Naver-Client-Secret", BuildConfig.NAVER_OPEN_SECRET_KEY)
                    .build()
                return@Interceptor it.proceed(request)
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(headerInterceptor)
                .addInterceptor(httpLoggingInterceptor)
                .build()

            openApiRetrofit = Retrofit.Builder()
                .baseUrl(OPENAPI_BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
        }
        return openApiRetrofit
    }
}
