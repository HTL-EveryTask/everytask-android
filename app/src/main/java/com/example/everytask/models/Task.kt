package com.example.everytask.models

data class Task(
    val create_time: String,
    val description: String,
    val due_time: String,
    val is_creator: Int,
    val is_daily: Int,
    val is_done: Int,
    val location: String,
    val note: String,
    val pk_task_id: String,
    val title: String
)