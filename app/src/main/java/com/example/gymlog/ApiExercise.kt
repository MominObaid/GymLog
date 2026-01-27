package com.example.gymlog

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ExerciseApiResponse(
    val results: List<ApiExercise>
)

@JsonClass(generateAdapter = true)
data class ApiExercise(
    val id : Int,

    @Json(name = "name")
    val exerciseName: String,
    val description: String,

    @Json(name = "category")
    val categoryName: String,

    @Json(name = "category")
    val category: ApiCategory
)

