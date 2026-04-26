package com.example.gymlog.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ExerciseApiResponse(
    @Json(name = "results")
    val results: List<ApiExercise>?
)
@JsonClass(generateAdapter = true)
data class ApiExercise(
    val id : Int,
    @Json(name = "name")
    val name : String?,
    @Json(name = "description")
    val description: String?,
)


