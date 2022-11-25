package com.example.everytask.models.response.groups

data class User(
    val email: String,
    val id: Int,
    val is_active: Boolean,
    val is_teacher: Boolean,
    val token: String,
    val username: String
)