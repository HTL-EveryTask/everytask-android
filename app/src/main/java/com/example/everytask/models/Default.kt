package com.example.everytask.models

data class Default(
    val type: String,
    val message: String?,
    val token: String?,
    val data: List<Task>?
)