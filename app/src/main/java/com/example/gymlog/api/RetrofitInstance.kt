package com.example.gymlog.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "https://wger.de/api/v2/"

    val api : ApiService by lazy {
     val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
     val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()


    Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ApiService::class.java)
    }
}








//object RetrofitInstance {
//
//    //The base URL of the API
//    private const val BASE_URL = "https://wger.de/api/v2"

////"https://wger.de/api/v2/exerciseinfo/?limit=500&language=2"
//https://wger.de/api/v2/exerciseinfo/?language=2&limit=500
