package com.example.everytask.models

data class Task(
    val title: String,
    val description: String,
    val due_time: String,
    val id: Int? = null,
    val is_daily: Int? = null,
    val is_done: Int? = null,
    val create_time: String? = null,
    val is_creator: Int? = null,
    val location: Any? = null,
    val note: Any? = null,
) : java.io.Serializable