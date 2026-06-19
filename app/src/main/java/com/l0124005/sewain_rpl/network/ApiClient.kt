package com.l0124005.sewain_rpl.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    // ⚠️ Untuk emulator Android Studio: gunakan 10.0.2.2 (alias localhost host)
    //    Untuk HP fisik: ganti dengan IP laptop di Wi-Fi yang sama (cek dengan `ifconfig`)
    private const val BASE_URL = "http://10.39.247.248:8000/api/"

    // URL tanpa /api/ — untuk load gambar
    const val IMAGE_BASE_URL = "http://10.39.247.248:8000/storage/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
