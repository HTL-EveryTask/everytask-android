package com.example.everytask.models.response.groups

data class Group(
    val description: String,
    val id: Int,
    val name: String,
    val stats: Stats,
    val users: List<GroupUser>,
    val picture: String?
): java.io.Serializable