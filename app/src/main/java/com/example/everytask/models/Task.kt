package com.example.everytask.models

import com.example.everytask.models.response.groups.Subtasks

data class Task(
    val title: String,
    val description: String,
    val due_time: String,
    val id: Int? = null,
    val is_daily: Boolean? = null,
    val is_done: Boolean? = null,
    val create_time: String? = null,
    val is_creator: Int? = null,
    val location: Any? = null,
    val note: Any? = null,
    val subtasks: Subtasks? = null,
) : java.io.Serializable