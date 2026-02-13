package com.example.gymlog

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("/exerciseinfo")     //https://wger.de/api/v2/exerciseinfo/?language=2&limit=500
    suspend fun getExercises(
        @Query("/?language") language: Int = 2,
        @Query("&limit") limit : Int = 500
    ): Response<ExerciseApiResponse>
}