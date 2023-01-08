package com.example.everytask.models.call

data class AppointmentInfo(
    val title: String,
    val description: String,
    val start_time: String,
    val end_time: String,
    val assigned_users: List<Int>,
    val assigned_groups: List<Int>,
    val tags: List<String>? = listOf(),
    val subject: SubjectInfo? = null
)