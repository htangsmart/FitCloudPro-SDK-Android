package com.topstep.fitcloud.sample2.data.net

import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber

class ApiClient constructor(
    private val moshi: Moshi,
) {

    val apiService: ApiService = createService()

    private fun createService(): ApiService {
        val builder = OkHttpClient.Builder()
        val logging = HttpLoggingInterceptor { message ->
            Timber.tag("HttpLogging").d(message)
        }.setLevel(HttpLoggingInterceptor.Level.BODY)
        logging.redactHeader("Authorization")
        logging.redactHeader("Cookie")
        builder.addInterceptor(logging)
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(builder.build())
            .build()
        return retrofit.create(ApiService::class.java)
    }

    companion object {
        const val BASE_URL = "http://fitcloud.hetangsmart.com"
    }
}
