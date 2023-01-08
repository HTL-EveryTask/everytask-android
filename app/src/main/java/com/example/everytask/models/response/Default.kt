package com.example.everytask.models.response

import com.example.everytask.models.response.appointments.Appointment
import com.example.everytask.models.response.tasks.Task
import com.example.everytask.models.response.groups.Group

data class Default(
    val type: String,
    val message: String?,
    val token: String?,
    val tasks: List<Task>?,
    val task: Task?,
    val user: User?,
    val group: Group?,
    val groups: List<Group>?,
    val key: String?,
    val appointments: List<Appointment>?,
)