package com.example.everytask.models.call

data class GroupInfo(
    val name: String,
    val description: String,
    val picture: String? = null,
    val members: List<Int>? = null,
)