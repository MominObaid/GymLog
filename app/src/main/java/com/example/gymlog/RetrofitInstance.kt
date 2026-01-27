package com.example.gymlog

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitInstance {

    //The base URL of the API
    private const val BASE_URL = "https://wger.de/api/v2/"

    //Create a Moshi instance with a Kotlin adapter
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    //Create the Retrofit instance, configured with the base URL and Moshi converter.
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    //Lazily create an implementation of the ApiService.
    val api: ApiService by lazy{
        retrofit.create(ApiService::class.java)
    }
}