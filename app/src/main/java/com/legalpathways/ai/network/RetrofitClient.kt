package com.legalpathways.ai.network

import com.legalpathways.ai.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Change this in BuildConfig or directly for your device/emulator
    // 10.0.2.2 is the localhost alias for Android emulator
    private val BASE_URL: String get() = BuildConfig.BASE_URL

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        // Increased timeouts for slow backends or network latency
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)     // 3 minutes for counselor
        .writeTimeout(60, TimeUnit.SECONDS)
        .callTimeout(240, TimeUnit.SECONDS)      // 4 minutes overall   // Added: overall call timeout
        // Disable connection pooling reuse which can cause timeout issues
        .connectionPool(okhttp3.ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}