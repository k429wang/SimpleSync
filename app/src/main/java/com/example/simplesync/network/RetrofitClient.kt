package com.example.simplesync.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object RetrofitClient {
    private const val BASE_URL = "https://example.com/"

    private fun getInstance(): Retrofit {
        val httpClient = OkHttpClient()
        val interceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        val clientBuilder: OkHttpClient.Builder =
            httpClient.newBuilder().addInterceptor(interceptor)


        return Retrofit.Builder()
           .baseUrl(BASE_URL)
           .addConverterFactory(GsonConverterFactory.create())
            .client(clientBuilder.build())
           .build()
    }

    fun getApiService(): ApiService {
        return getInstance().create(ApiService::class.java)
    }
}
