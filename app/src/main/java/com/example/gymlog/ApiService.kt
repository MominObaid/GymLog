package com.example.gymlog

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("/exerciseinfo")
    suspend fun getExercises(
        @Query("/?limit") limit : Int = 500,
        @Query("&language") language: Int = 2
    ): Response<ExerciseApiResponse>
}