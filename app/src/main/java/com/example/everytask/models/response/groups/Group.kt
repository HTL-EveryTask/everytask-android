package com.example.everytask.models.response.groups

import com.example.everytask.models.Task

data class Group(
    val admins: List<Admin>,
    val description: String,
    val id: Int,
    val name: String,
    val stats: Stats,
    val tasks: List<Task>,
    val users: List<User>
)