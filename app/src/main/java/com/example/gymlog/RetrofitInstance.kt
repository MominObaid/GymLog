package com.example.gymlog

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitInstance {

    //The base URL of the API
    private const val BASE_URL = "https://wger.de/api/v2/"
//
//    //Create a Moshi instance with a Kotlin adapter
//    private val moshi = Moshi.Builder()
//        .add(KotlinJsonAdapterFactory())
//        .build()

    val api : ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    //Lazily create an implementation of the ApiService.
//    val api: ApiService by lazy{
//        retrofit.create(ApiService::class.java)
//    }
}