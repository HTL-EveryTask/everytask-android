package com.example.everytask.models.response

import com.example.everytask.models.Task

data class Default(
    val type: String,
    val message: String?,
    val token: String?,
    val tasks: List<Task>?
)