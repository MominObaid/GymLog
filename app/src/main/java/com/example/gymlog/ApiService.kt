package com.example.gymlog

import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    @GET("exerciseinfo/?language=2&limit=100")
    suspend fun getExercises(): Response<ExerciseApiResponse>
}