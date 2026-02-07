package com.example.gymlog

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ExerciseApiResponse(
    @Json(name = "results")
    val results: List<ApiExercise>
)
@JsonClass(generateAdapter = true)
data class ApiExercise(
//    val id : Int,
    @Json(name = "name")
    val exerciseName: String,
    val description: String,
    val category: ApiCategory
)


