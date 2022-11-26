package com.example.everytask.models.response.groups

data class Stats(
    val total_admins: Int,
    val total_tasks: Int,
    val total_users: Int
) : java.io.Serializable