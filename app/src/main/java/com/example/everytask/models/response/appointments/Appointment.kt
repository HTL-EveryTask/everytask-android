package com.example.everytask.models.response.appointments

import com.example.everytask.models.response.tasks.AssignedGroup
import com.example.everytask.models.response.tasks.AssignedUser

data class Appointment(
    val assigned_groups: List<AssignedGroup>,
    val assigned_users: List<AssignedUser>,
    val description: String,
    val end_time: String,
    val id: Int,
    val start_time: String,
    val subject: Any,
    val tags: List<String>,
    val title: String
): java.io.Serializable