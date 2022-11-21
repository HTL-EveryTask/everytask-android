package com.example.everytask.models.call

data class TaskInfo (
    val title: String,
    val description: String,
    val due_time: String,
    val id: Int? = null,
    val is_daily: Boolean? = null,
    val is_done: Boolean? = null,
    val note: String? = null,
    val location: String? = null,
)