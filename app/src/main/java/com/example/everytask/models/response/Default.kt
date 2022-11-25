package com.example.everytask.models.response

import com.example.everytask.models.Task
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
)