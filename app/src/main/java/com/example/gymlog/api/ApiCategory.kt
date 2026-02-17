package com.example.gymlog.api

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiCategory(
    val id: Int,
    val name: String
)