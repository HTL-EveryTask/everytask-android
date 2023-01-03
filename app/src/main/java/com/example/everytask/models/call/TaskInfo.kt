package com.example.everytask.models.call

import com.example.everytask.models.response.tasks.AssignedGroup
import com.example.everytask.models.response.tasks.AssignedUser

data class TaskInfo(
    val title: String,
    val description: String,
    val due_time: String,
    val subject: String = "",
    val assigned_users: List<AssignedUser>? = null,
    val assigned_groups: List<AssignedGroup>? = null,
)