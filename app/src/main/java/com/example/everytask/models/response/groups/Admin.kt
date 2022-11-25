package com.example.everytask.models.response.groups

data class Admin(
    val email: String,
    val id: Int,
    val is_active: Boolean,
    val is_teacher: Boolean,
    val token: String,
    val username: String
)