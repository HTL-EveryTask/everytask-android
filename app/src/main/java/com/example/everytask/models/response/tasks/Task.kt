package com.example.everytask.models.response.tasks

data class Task(
    val assigned_groups: List<AssignedGroup>,
    val assigned_users: List<AssignedUser>,
    val create_time: String,
    val description: String,
    val due_time: String,
    val id: Int,
    val is_daily: Boolean,
    val is_done: Boolean,
    val note: String,
    val title: String,
    val type: List<String>
): java.io.Serializable