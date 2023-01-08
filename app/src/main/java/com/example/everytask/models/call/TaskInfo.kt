package com.example.everytask.models.call

import com.example.everytask.models.response.tasks.AssignedGroup
import com.example.everytask.models.response.tasks.AssignedUser

data class TaskInfo(
    val title: String,
    val description: String,
    val due_time: String,
    val assigned_users: List<Int>,
    val assigned_groups: List<Int>,
    val tags: List<String> = listOf(),
    val subject: SubjectInfo? = null,
)