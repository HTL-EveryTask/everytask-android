package com.example.everytask.models.call

data class RegisterInfo(
    val username: String,
    val password: String,
    val email: String,
    val is_teacher: Boolean
    )